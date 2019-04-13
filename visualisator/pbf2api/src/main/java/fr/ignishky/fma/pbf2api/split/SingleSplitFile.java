package fr.ignishky.fma.pbf2api.split;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.strtree.STRtree;
import crosby.binary.osmosis.OsmosisReader;
import fr.ignishky.fma.pbf2api.api.BoundingBox;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
public class SingleSplitFile implements SplitFile {

    private static final GeometryFactory gf = new GeometryFactory();

    private final Map<Long, Node> nodes = new HashMap<>(100);
    private final List<Way> ways = new ArrayList<>(100);
    private final List<Relation> relations = new ArrayList<>(100);
    private final STRtree tree = new STRtree();

    @SuppressWarnings("unchecked")
    public Iterator<Node> nodesWithin(BoundingBox boundingBox) {
        List<Long> ids = tree.query(boundingBox.envelope());
        return ids.stream().map(nodes::get).iterator();
    }

    public SingleSplitFile(String filename) {
        try {
            log.info("Loading OSM {}", filename);
            for (Path path : Files.list(Paths.get(filename)).collect(toList())) {
                OsmosisReader reader = new OsmosisReader(new FileInputStream(path.toString()));
                reader.setSink(new SplitterSink(path.toString()) {
                    @Override
                    public void process(RelationContainer rel) {
                        relations.add(rel.getEntity());
                    }

                    @Override
                    public void process(WayContainer way) {
                        ways.add(way.getEntity());
                    }

                    @Override
                    public void process(NodeContainer node) {
                        tree.insert(point(node.getEntity()), node.getEntity().getId());
                        nodes.put(node.getEntity().getId(), node.getEntity());
                    }
                });
                reader.run();
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        log.info("Nodes: {} - Ways : {}", nodes.size(), ways.size());
    }

    private static Envelope point(Node node) {
        return gf.createPoint(new Coordinate(node.getLongitude(), node.getLatitude())).getEnvelopeInternal();
    }

    @Override
    public Node getNodeById(Long id) {
        return nodes.get(id);
    }

    @Override
    public Iterator<Relation> getRelations() {
        return relations.iterator();
    }

    @Override
    public Iterator<Way> getWays() {
        return ways.iterator();
    }
}
