package fr.ignishky.fma.generator.converter.product;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vividsolutions.jts.geom.LineString;
import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.reader.Feature;
import fr.ignishky.fma.generator.writer.GeometrySerializer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static fr.ignishky.fma.generator.reader.Feature.Attribute.PARTSTRUC;
import static fr.ignishky.fma.generator.utils.Constants.INPUT_FOLDER;
import static fr.ignishky.fma.generator.utils.Constants.OUTPUT_FOLDER;

public class RailRoadShapefile extends Shapefile {

    private static final int TUNNEL = 1;
    private static final int BRIDGE = 2;

    @Inject
    RailRoadShapefile(@Named(INPUT_FOLDER) File inputFolder, NameProvider nameProvider, @Named(OUTPUT_FOLDER) File outputFolder) {
        super(inputFolder, nameProvider, outputFolder);
    }

    @Override
    protected String getProductName() {
        return "rr";
    }

    @Override
    protected String getInputFile(String countryCode, String zoneCode) {
        return countryCode + zoneCode + "___________rr.shp";
    }

    @Override
    protected String getNameFile(String countryCode) {
        return null;
    }

    @Override
    protected String getOutputFileName() {
        return "rr.osm.pbf";
    }

    @Override
    protected void serialize(GeometrySerializer serializer, Feature feature, CapitalProvider capitalProvider) {
        Map<String, String> tags = new HashMap<>(2);
        tags.put("railway", "rail");
        if (feature.getInt(PARTSTRUC) == TUNNEL) {
            tags.put("tunnel", "yes");
        }
        if (feature.getInt(PARTSTRUC) == BRIDGE) {
            tags.put("bridge", "yes");
        }

        IntStream.range(0, feature.getGeometry().getNumGeometries())
                .forEach(i -> serializer.write((LineString) feature.getGeometry().getGeometryN(i), tags));
    }
}
