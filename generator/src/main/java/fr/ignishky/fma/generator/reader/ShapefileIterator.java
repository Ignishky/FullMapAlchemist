package fr.ignishky.fma.generator.reader;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import static org.opengis.filter.Filter.INCLUDE;

public class ShapefileIterator implements Iterator<Feature>, Closeable {

    private final FeatureIterator<SimpleFeature> features;
    private final ShapefileDataStore dataStore;

    public ShapefileIterator(Path path) {
        try {
            dataStore = new ShapefileDataStore(path.toFile().toURI().toURL());
            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(INCLUDE);
            features = collection.features();
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read shapefile.", e);
        }
    }

    @Override
    public boolean hasNext() {
        return features.hasNext();
    }

    @Override
    public Feature next() {
        return new ShapefileFeature(features.next());
    }

    @Override
    public void close() {
        features.close();
        dataStore.dispose();
    }
}
