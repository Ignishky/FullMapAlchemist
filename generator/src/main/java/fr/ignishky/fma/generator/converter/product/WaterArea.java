package fr.ignishky.fma.generator.converter.product;

import com.google.inject.name.Named;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.reader.Feature;
import fr.ignishky.fma.generator.writer.OsmosisSerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static fr.ignishky.fma.generator.reader.Feature.Attribute.NAME;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.TYP;
import static fr.ignishky.fma.generator.utils.Constants.INPUT_FOLDER;
import static fr.ignishky.fma.generator.utils.Constants.OUTPUT_FOLDER;
import static fr.ignishky.fma.generator.utils.Constants.ROLE_INNER;
import static fr.ignishky.fma.generator.utils.Constants.ROLE_OUTER;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class WaterArea extends Shapefile {

    @Inject
    WaterArea(@Named(INPUT_FOLDER) File inputFolder, NameProvider nameProvider, @Named(OUTPUT_FOLDER) File outputFolder) {
        super(inputFolder, nameProvider, outputFolder);
    }

    @Override
    protected String getProductName() {
        return "wa";
    }

    @Override
    protected String getInputFile(String countryCode, String zoneCode) {
        return countryCode + zoneCode + "___________wa.shp";
    }

    @Override
    protected String getNameFile(String countryCode) {
        return null;
    }

    @Override
    protected String getOutputFileName() {
        return "wa.osm.pbf";
    }

    @Override
    protected void serialize(OsmosisSerializer serializer, Feature feature, CapitalProvider capitalProvider) {

        if (feature.getInt(TYP) != 1) {
            Map<String, String> tags = new HashMap<>();
            tags.put("natural", "water");
            if (feature.getString(NAME) != null) {
                tags.put("name", feature.getString(NAME));
            }

            List<RelationMember> members = new ArrayList<>(5);
            MultiPolygon multiPolygon = feature.getMultiPolygon();

            IntStream.range(0, multiPolygon.getNumGeometries()).forEach(i -> {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

                IntStream.range(0, polygon.getNumInteriorRing()).forEach(j ->
                        serializer.write(polygon.getInteriorRingN(j), tags)
                                .map(aLong -> new RelationMember(aLong, Way, ROLE_INNER))
                                .ifPresent(members::add));

                serializer.write(polygon.getExteriorRing(), tags)
                        .map(aLong -> new RelationMember(aLong, Way, ROLE_OUTER))
                        .ifPresent(members::add);
            });

            serializer.write(members, tags);
        }
    }
}
