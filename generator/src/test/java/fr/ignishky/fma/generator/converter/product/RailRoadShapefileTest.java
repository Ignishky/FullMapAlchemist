package fr.ignishky.fma.generator.converter.product;

import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.io.File;
import java.util.List;

import static fr.ignishky.fma.generator.utils.PbfUtils.read;
import static fr.ignishky.fma.generator.utils.ShapefileUtils.assertTag;
import static fr.ignishky.fma.generator.utils.TestConstants.RESOURCES_INPUT;
import static fr.ignishky.fma.generator.utils.TestConstants.TARGET_GENERATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class RailRoadShapefileTest {

    private final NameProvider nameProvider = mock(NameProvider.class);

    private final RailRoadShapefile railRoadShapefile = new RailRoadShapefile(new File(RESOURCES_INPUT), nameProvider, new File(TARGET_GENERATOR));

    @Test
    void should_throw_IllegalStateException_when_rail_road_shapefile_missing_in_ax_folder() {
        assertThrows(IllegalStateException.class, () -> railRoadShapefile.convert("and", "and"));
    }

    @Test
    void should_convert_rr_shapefile_to_OSM_format() {

        String convert = railRoadShapefile.convert("lux", "lux", null);

        List<Way> ways = read(convert).getWays();

        assertThat(ways).hasSize(49);
        assertTag(ways.get(5).getTags(), "tunnel", "yes");
        assertTag(ways.get(10).getTags(), "bridge", "yes");
    }
}