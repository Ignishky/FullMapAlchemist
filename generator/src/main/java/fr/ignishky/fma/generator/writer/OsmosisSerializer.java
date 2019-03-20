package fr.ignishky.fma.generator.writer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;
import static fr.ignishky.fma.generator.helper.Geohash.encodeGeohash;
import static fr.ignishky.fma.generator.helper.Layers.layer;
import static java.time.Instant.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

@Slf4j
public class OsmosisSerializer implements GeometrySerializer {

    private final Date date;
    private final Sink sink;
    private final OsmUser user;
    private final Set<Long> pointTracker = new HashSet<>();
    private final Set<Long> wayTracker = new HashSet<>();
    private final Set<Long> relationTracker = new HashSet<>();

    public OsmosisSerializer(Sink sink) {
        this.sink = sink;
        date = Date.from(now());
        user = new OsmUser(1, "Tomtom");
    }

    @Override
    public Optional<Node> writePoint(Point point, Map<String, String> tags) {
        long id = encodeGeohash(0, point);

        if (pointTracker.contains(id)) {
            log.warn("Rejecting point {} with tags {} because already present.", id, tags);
            return empty();
        }

        pointTracker.add(id);
        Node node = new Node(ced(id, tags), point.getY(), point.getX());
        sink.process(new NodeContainer(node));

        return of(node);
    }

    @Override
    public Optional<Long> writeBoundary(LineString line, Map<String, String> tags) {
        long id = encodeGeohash(7, line.getCentroid());

        if (!wayTracker.contains(id)) {
            wayTracker.add(id);
            Way way = new Way(ced(id, tags), getWayNodes(line, tags));
            sink.process(new WayContainer(way));
        }

        return of(id);
    }

    private CommonEntityData ced(long id, Map<String, String> tags) {
        return new CommonEntityData(id, 1, date, user, 1L, tags.entrySet().stream().map(en -> new Tag(en.getKey(), en.getValue())).collect(toList()));
    }

    private List<WayNode> getWayNodes(LineString line, Map<String, String> tags) {
        Coordinate[] coordinates = line.getCoordinates();
        List<WayNode> wayNodes = new ArrayList<>(coordinates.length + 1);
        IntStream.range(0, coordinates.length).forEach(i -> {
            Coordinate coordinate = coordinates[i];
            boolean start = i == 0;
            boolean end = i == coordinates.length - 1;
            int layer = getLayer(tags, coordinate, start, end);
            long id = writePointAndGetId(layer, coordinate);
            int size = wayNodes.size();
            if (size == 0 || wayNodes.get(size - 1).getNodeId() != id) {
                wayNodes.add(new WayNode(id));
            }
        });
        return wayNodes;
    }

    private int getLayer(Map<String, String> tags, Coordinate coordinate, boolean start, boolean end) {
        int layer = layer(tags, start, end);
        if ("ferry".equals(tags.get("route")) && !start && !end) {
            while (exists(layer, coordinate)) {
                layer++;
            }
        }
        return layer;
    }

    private boolean exists(int layer, Coordinate coordinate) {
        return pointTracker.contains(encodeGeohash(layer, coordinate.x, coordinate.y));
    }

    private long writePointAndGetId(int layer, Coordinate coordinate) {
        long id = encodeGeohash(layer, coordinate.x, coordinate.y);
        if (!pointTracker.contains(id)) {
            pointTracker.add(id);
            sink.process(new NodeContainer(new Node(new CommonEntityData(id, 1, date, user, 1L), coordinate.y, coordinate.x)));
        }
        return id;
    }








    @Override
    public void write(List<RelationMember> members, Map<String, String> tags) {
        for (RelationMember member : members) {
            if (member.getMemberType() == Node) {
                checkState(pointTracker.contains(member.getMemberId()), "Adding relation on missing node");
            } else if (member.getMemberType() == Way) {
                checkState(wayTracker.contains(member.getMemberId()), "Adding relation on missing way");
            }
        }

        int layer = layer(tags.get("layer"));

        long id = relationId(members.get(0).getMemberId(), layer);
        sink.process(new RelationContainer(new Relation(ced(id, tags), members)));
    }

    @Override
    public void close() {
        sink.complete();
        sink.release();
    }

    private long relationId(long memberId, int layer) {
        long id = memberId + layer;
        while (relationTracker.contains(id)) {
            log.debug("Collision on relation with {}", id);
            id++;
        }
        relationTracker.add(id);
        return id;
    }
}
