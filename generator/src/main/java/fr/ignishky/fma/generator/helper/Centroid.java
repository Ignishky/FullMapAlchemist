package fr.ignishky.fma.generator.helper;

import com.vividsolutions.jts.geom.Point;
import fr.ignishky.fma.generator.reader.Feature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

@Data
@Wither
@AllArgsConstructor
public class Centroid {

    private final Long id;
    private final String name;
    private final String postcode;
    private final Integer adminclass;
    private final Integer citytyp;
    private final Integer dispclass;
    private final Point point;

    public Centroid() {
        this(null, null, null, null, null, null, null);
    }

    public static Centroid from(Feature feature) {
        return new Centroid()
                .withName(feature.getString("NAME"))
                .withPostcode(feature.getString("POSTCODE"))
                .withAdminclass(feature.getInteger("ADMINCLASS"))
                .withCitytyp(feature.getInteger("CITYTYP"))
                .withDispclass(feature.getInteger("DISPCLASS"))
                .withPoint(feature.getPoint());
    }

    public String getPlace() {
        return CityType.getOsmValue(citytyp, dispclass);
    }
}
