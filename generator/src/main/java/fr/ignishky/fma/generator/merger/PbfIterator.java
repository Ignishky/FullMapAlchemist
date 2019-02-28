package fr.ignishky.fma.generator.merger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfStreamSplitter;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import static fr.ignishky.fma.generator.utils.CollectionUtils.streamIterator;
import static fr.ignishky.fma.generator.utils.PbfDecoder.decode;

public class PbfIterator implements Iterator<EntityContainer> {

    private final Iterator<EntityContainer> iterator;

    public PbfIterator(String pbfFile) {
        try {
            PbfStreamSplitter splitter = new PbfStreamSplitter(new DataInputStream(new FileInputStream(pbfFile)));
            iterator = streamIterator(splitter).flatMap(blob -> decode(blob).stream()).iterator();
        }
        catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public EntityContainer next() {
        return iterator.next();
    }
}
