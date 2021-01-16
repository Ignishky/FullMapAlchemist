package fr.ignishky.fma.pbf2api.split;

import com.vividsolutions.jts.geom.Coordinate;
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
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@Slf4j
public class SingleSplitFile implements SplitFile {

    private static final GeometryFactory gf = new GeometryFactory();

    private final Map<Long, Node> nodes = new HashMap<>();
    private final List<Way> ways = new ArrayList<>();
    private final List<Relation> relations = new ArrayList<>();
    private final STRtree tree = new STRtree();

    public SingleSplitFile(String filename) {
        try {
            log.info("Loading OSM {}", filename);
            Path path = Path.of(filename);
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
                public void process(NodeContainer nodeContainer) {
                    Node node = nodeContainer.getEntity();
                    tree.insert(gf.createPoint(new Coordinate(node.getLongitude(), node.getLatitude())).getEnvelopeInternal(), node.getId());
                    nodes.put(node.getId(), node);
                }
            });
            reader.run();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

        log.info("Nodes : {} - Ways : {} - Relations : {}", nodes.size(), ways.size(), relations.size());
    }

    @SuppressWarnings("unchecked")
    public Collection<Node> getNodes(BoundingBox boundingBox) {
        List<Long> ids = tree.query(boundingBox.envelope());
        return ids.stream().map(nodes::get).collect(toList());
    }

    @Override
    public Collection<Way> getWays() {
        return unmodifiableList(ways);
    }

    @Override
    public Node getNodeById(Long id) {
        return nodes.get(id);
    }

    @Override
    public Iterator<Relation> getRelations() {
        return relations.iterator();
    }
}
