package fr.ignishky.fma.pbf2api.split;

import fr.ignishky.fma.pbf2api.api.BoundingBox;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.Iterator;

public interface SplitFile {

    Iterator<Relation> getRelations();

    Iterator<Node> nodesWithin(BoundingBox boundingBox);

    Iterator<Way> getWays();

    Node getNodeById(Long id);
}
