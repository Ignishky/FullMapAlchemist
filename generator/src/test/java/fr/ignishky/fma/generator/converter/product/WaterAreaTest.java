package fr.ignishky.fma.generator.converter.product;

import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.utils.PbfContent;
import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

import java.io.File;

import static fr.ignishky.fma.generator.utils.PbfUtils.read;
import static fr.ignishky.fma.generator.utils.ShapefileUtils.assertTag;
import static fr.ignishky.fma.generator.utils.TestConstants.RESOURCES_INPUT;
import static fr.ignishky.fma.generator.utils.TestConstants.TARGET_GENERATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class WaterAreaTest {

    private final NameProvider nameProvider = mock(NameProvider.class);

    private final WaterArea waterArea = new WaterArea(new File(RESOURCES_INPUT), nameProvider, new File(TARGET_GENERATOR));

    @Test
    void should_throw_IllegalStateException_when_water_area_shapefile_missing_in_ax_folder() {
        assertThrows(IllegalStateException.class, () -> waterArea.convert("and", "and"));
    }

    @Test
    void should_convert_wa_shapefile_to_OSM_format() {

        String convert = waterArea.convert("lux", "lux");

        PbfContent pbfContent = read(convert);

        assertThat(pbfContent.getRelations()).hasSize(139);

        Relation relation = pbfContent.getRelations().get(0);
        assertTag(relation.getTags(), "natural", "water");
        assertTag(relation.getTags(), "name", "Meer van Heist");
    }
}
