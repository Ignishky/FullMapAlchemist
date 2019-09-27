package fr.ignishky.fma.generator.reader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public interface Feature {

    enum Attribute {
        ID, NAME, POSTCODE, ADMINCLASS, CITYTYP, DISPCLASS, PARTSTRUC
    }

    String getString(Attribute attr);

    Long getLong(Attribute attr);

    int getInt(Attribute attr);

    Point getPoint();

    MultiPolygon getMultiPolygon();

    Geometry getGeometry();
}
