package fr.ignishky.fma.generator.converter.shapefile;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.helper.Centroid;
import fr.ignishky.fma.generator.utils.PbfContent;
import org.geotools.geometry.jts.LiteCoordinateSequence;
import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static fr.ignishky.fma.generator.utils.PbfUtils.read;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class A0ShapefileTest {

    private final NameProvider nameProvider = mock(NameProvider.class);
    private final CapitalProvider capitalProvider = mock(CapitalProvider.class);

    private final A0Shapefile a0Shapefile = new A0Shapefile(new File("src/test/resources/input"), nameProvider, new File("target/generator"));

    @Test
    void should_throw_IllegalStateException_when_a0_shapefile_missing_in_ax_folder() {
        assertThrows(IllegalStateException.class, () -> a0Shapefile.convert("and", "ax", capitalProvider));
    }

    @Test
    void should_convert_a0_shapefile_to_OSM_format() {
        when(nameProvider.getAlternateNames(anyLong())).thenReturn(ImmutableMap.of("name:fr", "Luxembourg"));
        when(capitalProvider.get(0)).thenReturn(singletonList(new Centroid()
                .withName("Luxembourg")
                .withCitytyp(9)
                .withDispclass(7)
                .withPoint(new Point(new LiteCoordinateSequence(6.1311,49.6045), new GeometryFactory()))));

        String convert = a0Shapefile.convert("lux", "ax", capitalProvider);

        PbfContent pbfContent = read(convert);

        assertThat(pbfContent.getRelations()).hasSize(1);

        Relation relation = pbfContent.getRelations().get(0);
        Collection<Tag> relationTags = relation.getTags();
        assertThat(relationTags).hasSize(6);
        assertTag(relationTags, "admin_level", "2");
        assertTag(relationTags, "type", "boundary");
        assertTag(relationTags, "boundary", "administrative");
        assertTag(relationTags, "layer", "0");
        assertTag(relationTags, "name", "Luxembourg");
        assertTag(relationTags, "name:fr", "Luxembourg");

        List<RelationMember> members = relation.getMembers();
        assertThat(members.stream().map(RelationMember::getMemberRole)).containsOnly("label", "outer", "admin_centre");
    }

    private static void assertTag(Collection<Tag> tags, String key, String value) {
        assertThat(tags.stream().filter(tag -> key.equals(tag.getKey())).map(Tag::getValue)).containsOnly(value);
    }
}
