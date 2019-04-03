package fr.ignishky.fma.generator.writer;

import com.google.inject.ImplementedBy;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ImplementedBy(OsmosisSerializer.class)
public interface GeometrySerializer extends Closeable {

    Optional<Long> write(Point point, Map<String, String> tags);

    Optional<Long> write(LineString line, Map<String, String> tags);

    void write(List<RelationMember> members, Map<String, String> tags);
}
