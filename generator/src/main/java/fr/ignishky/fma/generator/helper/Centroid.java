package fr.ignishky.fma.generator.helper;

import com.vividsolutions.jts.geom.Point;
import fr.ignishky.fma.generator.reader.Feature;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import static fr.ignishky.fma.generator.reader.Feature.Attribute.ADMINCLASS;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.CITYTYP;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.DISPCLASS;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.NAME;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.POSTCODE;

@Value
@Wither
@AllArgsConstructor
public class Centroid {

    private final Long id;
    private final String name;
    private final String postcode;
    private final int adminClass;
    private final int cityTyp;
    private final int displayClass;
    private final Point point;

    public Centroid() {
        this(null, null, null, -1, -1, -1, null);
    }

    static Centroid from(Feature feature) {
        return new Centroid()
                .withName(feature.getString(NAME))
                .withPostcode(feature.getString(POSTCODE))
                .withAdminClass(feature.getInt(ADMINCLASS))
                .withCityTyp(feature.getInt(CITYTYP))
                .withDisplayClass(feature.getInt(DISPCLASS))
                .withPoint(feature.getPoint());
    }

    public String getPlace() {
        return CityType.getOsmValue(cityTyp, displayClass);
    }
}
