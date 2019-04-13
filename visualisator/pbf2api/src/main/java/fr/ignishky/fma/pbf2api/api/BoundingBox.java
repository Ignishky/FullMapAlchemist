package fr.ignishky.fma.pbf2api.api;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import lombok.Data;

@Data
public class BoundingBox {

    private static final GeometryFactory gf = new GeometryFactory();

    private final double minLat;
    private final double minLong;
    private final double maxLat;
    private final double maxLong;

    public Envelope envelope() {
        return gf.createLineString(new Coordinate[] { new Coordinate(minLong, minLat), new Coordinate(maxLong, maxLat) }).getEnvelopeInternal();
    }
}
