package fr.ignishky.fma.pbf2api.split;

import com.google.common.collect.Iterators;
import fr.ignishky.fma.pbf2api.api.BoundingBox;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MultiSplitFile implements SplitFile {

    private final SplitFile file1;
    private final SplitFile file2;

    public MultiSplitFile(SplitFile file1, SplitFile file2) {
        this.file1 = file1;
        this.file2 = file2;
    }

    @Override
    public Collection<Node> getNodes(BoundingBox boundingBox) {
        Set<Node> result = new HashSet<>();
        result.addAll(file1.getNodes(boundingBox));
        result.addAll(file2.getNodes(boundingBox));
        return result;
    }

    @Override
    public Collection<Way> getWays() {
        Set<Way> result = new HashSet<>();
        result.addAll(file1.getWays());
        result.addAll(file2.getWays());
        return result;
    }

    @Override
    public Iterator<Relation> getRelations() {
        return Iterators.concat(file1.getRelations(), file2.getRelations());
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
