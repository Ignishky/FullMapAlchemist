package fr.ignishky.fma.generator.helper;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import fr.ignishky.fma.generator.writer.GeometrySerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class PolygonBoundaryBuilder {

    private PolygonBoundaryBuilder() {
    }

    public static void addPolygons(GeometrySerializer serializer, List<RelationMember> members, MultiPolygon multiPolygon, Map<String, String> wayTags) {
        IntStream.range(0, multiPolygon.getNumGeometries()).forEach(i -> {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

            IntStream.range(0, polygon.getNumInteriorRing()).forEach(j -> addRelationMember(serializer, wayTags, polygon.getInteriorRingN(j), "inner").ifPresent(members::add));

            addRelationMember(serializer, wayTags, polygon.getExteriorRing(), "outer").ifPresent(members::add);
        });
    }

    private static Optional<RelationMember> addRelationMember(GeometrySerializer serializer, Map<String, String> wayTags, LineString geom, String memberRole) {
        Optional<Long> wayId = serializer.writeBoundary(geom, wayTags);
        return wayId.map(aLong -> new RelationMember(aLong, Way, memberRole));
    }
}
