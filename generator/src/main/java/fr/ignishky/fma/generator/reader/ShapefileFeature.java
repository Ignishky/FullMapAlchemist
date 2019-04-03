package fr.ignishky.fma.generator.reader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import lombok.Value;
import org.opengis.feature.simple.SimpleFeature;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Value
public class ShapefileFeature implements Feature {

    private final SimpleFeature feature;

    public String getString(Attribute attr) {
        String attribute = (String) feature.getAttribute(attr.name());
        return isBlank(attribute) ? null : attribute;
    }

    public Long getLong(Attribute attr) {
        return (Long) feature.getAttribute(attr.name());
    }

    public int getInt(Attribute attr) {
        return (int) feature.getAttribute(attr.name());
    }

    public Point getPoint() {
        return (Point) feature.getDefaultGeometryProperty().getValue();
    }

    public MultiPolygon getMultiPolygon() {
        return (MultiPolygon) feature.getDefaultGeometryProperty().getValue();
    }

    public Geometry getGeometry() {
        return (Geometry) feature.getDefaultGeometryProperty().getValue();
    }
}
