package fr.ignishky.fma.pbf2api.split;

import com.google.common.collect.Iterators;
import fr.ignishky.fma.pbf2api.api.BoundingBox;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.Iterator;

public class MultiSplitFile implements SplitFile {

    private final SplitFile file1;
    private final SplitFile file2;

    public MultiSplitFile(SplitFile file1, SplitFile file2) {
        this.file1 = file1;
        this.file2 = file2;
    }

    @Override
    public Iterator<Relation> getRelations() {
        return Iterators.concat(file1.getRelations(), file2.getRelations());
    }

    @Override
    public Iterator<Node> nodesWithin(BoundingBox bbox) {
        return Iterators.concat(file1.nodesWithin(bbox), file2.nodesWithin(bbox));
    }

    @Override
    public Iterator<Way> getWays() {
        return Iterators.concat(file1.getWays(), file2.getWays());
    }

    @Override
    public Node getNodeById(Long id) {
        Node first = file1.getNodeById(id);
        if (first != null) {
            return first;
        }
        return file2.getNodeById(id);
    }
}
