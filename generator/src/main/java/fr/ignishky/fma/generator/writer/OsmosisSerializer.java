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

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static fr.ignishky.fma.generator.helper.Geohash.encodeGeohash;
import static fr.ignishky.fma.generator.helper.Layers.layer;
import static fr.ignishky.fma.generator.utils.Constants.TAG_LAYER;
import static java.time.Instant.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

@Slf4j
public class OsmosisSerializer implements GeometrySerializer {

    private final Instant date;
    private final Sink sink;
    private final OsmUser user;
    private final Collection<Long> pointTracker = new HashSet<>(10);
    private final Collection<Long> wayTracker = new HashSet<>(10);
    private final Collection<Long> relationTracker = new HashSet<>(10);

    public OsmosisSerializer(Path path) {
        sink = new PbfSink(path);
        date = now();
        user = new OsmUser(1, "Tomtom");
    }

    @Override
    public Optional<Long> write(Point point, Map<String, String> tags) {
        long id = encodeGeohash(0, point.getCoordinate());

        if (pointTracker.contains(id)) {
            log.warn("Rejecting point {} with tags {} because already present.", id, tags);
            return empty();
        }

        pointTracker.add(id);
        sink.process(new NodeContainer(new Node(ced(id, tags), point.getY(), point.getX())));

        return of(id);
    }

    @Override
    public Optional<Long> write(LineString line, Map<String, String> tags) {
        long id = encodeGeohash(7, line.getCentroid().getCoordinate());

        if (!wayTracker.contains(id)) {
            wayTracker.add(id);
            sink.process(new WayContainer(new Way(ced(id, tags), getLineNodes(line, tags))));
        }

        return of(id);
    }

    @Override
    public void write(List<RelationMember> members, Map<String, String> tags) {
        members.stream()
                .filter(member -> isUnknownNode(member) || isUnknownWay(member))
                .findFirst()
                .ifPresent(member -> {
                    throw new IllegalStateException("Could not add relation with missing member " + member.getMemberId());
                });

        long id = relationId(members.get(0).getMemberId(), layer(tags.get(TAG_LAYER)));
        sink.process(new RelationContainer(new Relation(ced(id, tags), members)));
    }

    @Override
    public void close() {
        sink.complete();
        sink.release();
    }

    private CommonEntityData ced(long id, Map<String, String> tags) {
        return new CommonEntityData(id, 1, Date.from(date), user, 1L,
                tags.entrySet().stream().map(en -> new Tag(en.getKey(), en.getValue())).collect(toList()));
    }

    private List<WayNode> getLineNodes(LineString line, Map<String, String> tags) {
        Coordinate[] coordinates = line.getCoordinates();
        List<WayNode> wayNodes = new ArrayList<>(coordinates.length + 1);
        IntStream.range(0, coordinates.length).forEach(i -> {
            int layer = getLayer(tags, coordinates[i], i == 0, i == coordinates.length - 1);
            if (wayNodes.isEmpty() || wayNodes.get(wayNodes.size() - 1).getNodeId() != writePointAndGetId(layer, coordinates[i])) {
                wayNodes.add(new WayNode(writePointAndGetId(layer, coordinates[i])));
            }
        });
        return wayNodes;
    }

    private int getLayer(Map<String, String> tags, Coordinate coordinate, boolean start, boolean end) {
        int layer = layer(tags, start, end);
        if ("ferry".equals(tags.get("route")) && !start && !end) {
            while (pointTracker.contains(encodeGeohash(layer, coordinate))) {
                layer++;
            }
        }
        return layer;
    }

    private long writePointAndGetId(int layer, Coordinate coordinate) {
        long id = encodeGeohash(layer, coordinate);
        if (!pointTracker.contains(id)) {
            pointTracker.add(id);
            sink.process(new NodeContainer(new Node(new CommonEntityData(id, 1, Date.from(date), user, 1L), coordinate.y, coordinate.x)));
        }
        return id;
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

    private boolean isUnknownNode(RelationMember member) {
        return member.getMemberType() == Node && !pointTracker.contains(member.getMemberId());
    }

    private boolean isUnknownWay(RelationMember member) {
        return member.getMemberType() == Way && !wayTracker.contains(member.getMemberId());
    }
}
