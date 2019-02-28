package fr.ignishky.fma.generator.reader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public interface Feature {

    String getString(String attr);

    Long getLong(String attr);

    Integer getInteger(String attr);

    Point getPoint();

    MultiPolygon getMultiPolygon();

    Geometry getGeometry();
}
