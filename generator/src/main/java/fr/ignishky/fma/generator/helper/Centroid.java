package fr.ignishky.fma.generator.helper;

import com.vividsolutions.jts.geom.Point;
import fr.ignishky.fma.generator.reader.Feature;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;

import static fr.ignishky.fma.generator.reader.Feature.Attribute.ADMINCLASS;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.CITYTYP;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.DISPCLASS;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.NAME;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.POSTCODE;

@Value
@With
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Centroid {

    Long id;
    String name;
    String postcode;
    Integer adminClass;
    Integer cityTyp;
    Integer displayClass;
    Point point;

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
