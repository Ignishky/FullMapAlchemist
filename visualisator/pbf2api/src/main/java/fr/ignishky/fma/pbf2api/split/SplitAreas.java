package fr.ignishky.fma.pbf2api.split;

import com.github.davidmoten.geo.LatLong;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import fr.ignishky.fma.pbf2api.api.BoundingBox;

import java.io.IOException;
import java.util.List;

import static fr.ignishky.fma.pbf2api.utils.Geohash.decodeGeohash;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.readLines;

public class SplitAreas {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final STRtree tree;

    public SplitAreas() {
        try {
            tree = new STRtree();
            int i = 1;
            WKTReader reader = new WKTReader();
            for (String line : readLines(getClass().getResourceAsStream("/split.csv"), UTF_8)) {
                tree.insert(reader.read(line).getEnvelopeInternal(), String.valueOf(i));
                i++;
            }

        } catch (IOException | ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getAreas(BoundingBox boundingBox) {
        return tree.query(boundingBox.envelope());
    }

    @SuppressWarnings("unchecked")
    public String getArea(long geohash) {
        LatLong decodeGeohash = decodeGeohash(geohash);
        List<String> query = tree.query(point(decodeGeohash.getLon(), decodeGeohash.getLat()));
        return query.isEmpty() ? null : query.get(0);
    }

    private static Envelope point(double x, double y) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(x, y)).getEnvelopeInternal();
    }
}
