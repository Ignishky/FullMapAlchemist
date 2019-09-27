package fr.ignishky.fma.generator.converter.product;

import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.utils.PbfContent;
import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.io.File;

import static fr.ignishky.fma.generator.utils.PbfUtils.read;
import static fr.ignishky.fma.generator.utils.TestConstants.RESOURCES_INPUT;
import static fr.ignishky.fma.generator.utils.TestConstants.TARGET_GENERATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RailRoadShapefileTest {

    private final NameProvider nameProvider = mock(NameProvider.class);

    private final RailRoadShapefile railRoadShapefile = new RailRoadShapefile(new File(RESOURCES_INPUT), nameProvider, new File(TARGET_GENERATOR));

    @Test
    void should_convert_rr_shapefile_to_OSM_format() {

        String convert = railRoadShapefile.convert("lux", "lux", null);

        PbfContent pbfContent = read(convert);

        assertThat(pbfContent.getWays()).hasSize(49);
        Way tunnel = pbfContent.getWays().stream().filter(way -> way.getId() == 266113603127785981L).findFirst().get();
        assertThat(tunnel.getTags()).contains(new Tag("tunnel", "yes"));

        Way bridge = pbfContent.getWays().stream().filter(way -> way.getId() == 266113500902300323L).findFirst().get();
        assertThat(bridge.getTags()).contains(new Tag("bridge", "yes"));
    }
}