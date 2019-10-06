package fr.ignishky.fma.generator.merger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class MergingOsmPbfIterator implements Iterator<EntityContainer> {

    private final Iterator<EntityContainer> it1;
    private final Iterator<EntityContainer> it2;

    private EntityMergeResult next = new EntityMergeResult();

    MergingOsmPbfIterator(Iterator<EntityContainer> it1, Iterator<EntityContainer> it2) {
        this.it1 = it1;
        this.it2 = it2;
    }

    static Iterator<EntityContainer> init(List<Iterator<EntityContainer>> iterators) {
        Iterator<EntityContainer> result = iterators.get(0);
        for (Iterator<EntityContainer> iter : iterators.subList(1, iterators.size())) {
            result = new MergingOsmPbfIterator(iter, result);
        }
        return result;
    }

    @Override
    public boolean hasNext() {
        if (next.getResult() != null) {
            return true;
        }
        if (next.getEc1() == null) {
            next.setEc1(it1.hasNext() ? it1.next() : null);
        }
        if (next.getEc2() == null) {
            next.setEc2(it2.hasNext() ? it2.next() : null);
        }
        next = next.merge();
        return next.getResult() != null;
    }

    @Override
    public EntityContainer next() {
        if (next.getResult() == null && !hasNext()) {
            throw new NoSuchElementException("Cannot call next, no more elements in iterator.");
        }
        EntityContainer result = next.getResult();
        next.setResult(null);
        return result;
    }
}
