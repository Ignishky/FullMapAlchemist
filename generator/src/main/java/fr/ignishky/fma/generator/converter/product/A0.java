package fr.ignishky.fma.generator.converter.product;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.reader.Feature;
import fr.ignishky.fma.generator.writer.OsmosisSerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.vividsolutions.jts.algorithm.Centroid.getCentroid;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.ID;
import static fr.ignishky.fma.generator.reader.Feature.Attribute.NAME;
import static fr.ignishky.fma.generator.utils.Constants.BOUNDARY_ADMINISTRATIVE;
import static fr.ignishky.fma.generator.utils.Constants.INPUT_FOLDER;
import static fr.ignishky.fma.generator.utils.Constants.OUTPUT_FOLDER;
import static fr.ignishky.fma.generator.utils.Constants.ROLE_ADMIN_CENTRE;
import static fr.ignishky.fma.generator.utils.Constants.ROLE_INNER;
import static fr.ignishky.fma.generator.utils.Constants.ROLE_LABEL;
import static fr.ignishky.fma.generator.utils.Constants.ROLE_OUTER;
import static fr.ignishky.fma.generator.utils.Constants.TAG_ADMIN_LEVEL;
import static fr.ignishky.fma.generator.utils.Constants.TAG_BOUNDARY;
import static fr.ignishky.fma.generator.utils.Constants.TAG_LAYER;
import static fr.ignishky.fma.generator.utils.Constants.TAG_NAME;
import static fr.ignishky.fma.generator.utils.Constants.TAG_TYPE;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class A0 extends Shapefile {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Inject
    A0(@Named(INPUT_FOLDER) File inputFolder, NameProvider nameProvider, @Named(OUTPUT_FOLDER) File outputFolder) {
        super(inputFolder, nameProvider, outputFolder);
    }

    @Override
    protected String getProductName() {
        return "a0";
    }

    @Override
    protected String getInputFile(String countryCode, String zoneCode) {
        return countryCode + "______________a0.shp";
    }

    @Override
    protected String getNameFile(String countryCode) {
        return countryCode + "______________an.dbf";
    }

    @Override
    protected String getOutputFileName() {
        return "a0.osm.pbf";
    }

    @Override
    protected void serialize(OsmosisSerializer serializer, Feature feature, CapitalProvider capitalProvider) {
        String name = feature.getString(NAME);

        if (name == null) {
            return;
        }

        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_NAME, name);
        tags.putAll(nameProvider.getAlternateNames(feature.getLong(ID)));
        MultiPolygon multiPolygon = feature.getMultiPolygon();

        List<RelationMember> members = new ArrayList<>();
        addLabel(serializer, multiPolygon, tags).ifPresent(members::add);
        addAdminCenter(serializer, capitalProvider).ifPresent(members::add);

        tags.putAll(Map.of(TAG_BOUNDARY, BOUNDARY_ADMINISTRATIVE, TAG_ADMIN_LEVEL, "2"));
        members.addAll(addBoundaries(serializer, multiPolygon, tags));

        tags.put(TAG_TYPE, TAG_BOUNDARY);
        tags.put(TAG_LAYER, "0");

        serializer.write(members, tags);
    }

    private static Optional<RelationMember> addLabel(OsmosisSerializer serializer, MultiPolygon multiPolygon, Map<String, String> tags) {
        return serializer.write(GEOMETRY_FACTORY.createPoint(getCentroid(multiPolygon)), tags)
                .map(nodeId -> new RelationMember(nodeId, Node, ROLE_LABEL));
    }

    private static Optional<RelationMember> addAdminCenter(OsmosisSerializer serializer, CapitalProvider capitalProvider) {
        return capitalProvider.forLevel(0)
                .findFirst() // There is only one country capital
                .flatMap(city -> serializer.write(city.getPoint(), Map.of(TAG_NAME, city.getName(), "place", city.getPlace(), "capital", "yes")))
                .map(nodeId -> new RelationMember(nodeId, Node, ROLE_ADMIN_CENTRE));
    }

    private static List<RelationMember> addBoundaries(OsmosisSerializer serializer, MultiPolygon multiPolygon, Map<String, String> tags) {

        List<RelationMember> members = new ArrayList<>();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

            for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                serializer.write(polygon.getInteriorRingN(j), tags)
                        .map(id -> new RelationMember(id, Way, ROLE_INNER))
                        .ifPresent(members::add);
            }

            serializer.write(polygon.getExteriorRing(), tags)
                    .map(id -> new RelationMember(id, Way, ROLE_OUTER))
                    .ifPresent(members::add);
        }
        return members;
    }
}
