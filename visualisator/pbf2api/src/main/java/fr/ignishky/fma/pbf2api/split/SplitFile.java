package fr.ignishky.fma.pbf2api.split;

import fr.ignishky.fma.pbf2api.api.BoundingBox;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.Collection;
import java.util.Iterator;

public interface SplitFile {

    Collection<Node> getNodes(BoundingBox boundingBox);

    Collection<Way> getWays();

    Iterator<Relation> getRelations();

    Node getNodeById(Long id);
}
