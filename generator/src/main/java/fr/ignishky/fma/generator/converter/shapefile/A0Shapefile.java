package fr.ignishky.fma.generator.converter.shapefile;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.helper.PolygonBoundaryBuilder;
import fr.ignishky.fma.generator.reader.Feature;
import fr.ignishky.fma.generator.writer.GeometrySerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.vividsolutions.jts.algorithm.Centroid.getCentroid;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;

public class A0Shapefile extends Shapefile {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Inject
    public A0Shapefile(@Named("inputFolder") File inputFolder, NameProvider nameProvider, @Named("outputFolder") File outputFolder) {
        super(inputFolder, nameProvider, outputFolder);
    }

    @Override
    protected String getInputFile(String countryCode) {
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
    protected void serialize(GeometrySerializer serializer, Feature feature, CapitalProvider capitalProvider) {
        String name = feature.getString("NAME");

        Map<String, String> tags = new HashMap<>();
        tags.put("name", name);
        tags.putAll(nameProvider.getAlternateNames(feature.getLong("ID")));
        MultiPolygon multiPolygon = feature.getMultiPolygon();

        List<RelationMember> members = new ArrayList<>();

        serializer.writePoint(GEOMETRY_FACTORY.createPoint(getCentroid(multiPolygon)), tags)
                .map(node -> new RelationMember(node.getId(), Node, "label"))
                .ifPresent(members::add);

        capitalProvider.get(0).stream()
                .filter(city -> feature.getGeometry().contains(city.getPoint()))
                .findFirst()
                .flatMap(city -> serializer.writePoint(city.getPoint(), of("name", city.getName(), "place", city.getPlace(), "capital", "yes")))
                .map(city -> new RelationMember(city.getId(), Node, "admin_centre"))
                .ifPresent(members::add);

        tags.putAll(of("boundary", "administrative", "admin_level", "2"));

        PolygonBoundaryBuilder.addPolygons(serializer, members, multiPolygon, tags);

        tags.put("type", "boundary");
        tags.put("layer", "0");

        serializer.write(members, tags);
    }
}
