package fr.ignishky.fma.generator.split;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SplitAreas {

    private static final GeometryFactory gf = new GeometryFactory();

    private final STRtree tree;

    SplitAreas() {
        try {
            tree = new STRtree();
            int i = 1;
            WKTReader reader = new WKTReader();
            for (String line : IOUtils.readLines(getClass().getResourceAsStream("/split.csv"), UTF_8)) {
                tree.insert(reader.read(line).getEnvelopeInternal(), String.valueOf(i));
                i++;
            }
        }
        catch (IOException | ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> files(Envelope envelope) {
        return tree.query(envelope);
    }

    @SuppressWarnings("unchecked")
    public String file(double x, double y) {
        List<String> query = tree.query(point(x, y));
        return query.isEmpty() ? null : query.get(0);
    }

    private static Envelope point(double x, double y) {
        return gf.createPoint(new Coordinate(x, y)).getEnvelopeInternal();
    }
}
