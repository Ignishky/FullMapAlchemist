package fr.ignishky.fma.generator.converter;

import fr.ignishky.fma.generator.converter.product.A0;
import fr.ignishky.fma.generator.converter.product.RailRoad;
import fr.ignishky.fma.generator.converter.product.WaterArea;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import fr.ignishky.fma.generator.split.Splitter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static fr.ignishky.fma.generator.utils.TestConstants.RESOURCES_INPUT;
import static fr.ignishky.fma.generator.utils.TestConstants.TARGET_GENERATOR;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class ZoneConverterTest {

    private final A0 a0 = mock(A0.class);
    private final RailRoad railRoad = mock(RailRoad.class);
    private final WaterArea waterArea = mock(WaterArea.class);
    private final OsmMerger osmMerger = mock(OsmMerger.class);
    private final Splitter splitter = mock(Splitter.class);
    private final CapitalProvider capitalProvider = mock(CapitalProvider.class);

    private final ZoneConverter zoneConverter = new ZoneConverter(new File(RESOURCES_INPUT), new File(TARGET_GENERATOR),
            a0, railRoad, waterArea, osmMerger, splitter);

    @Test
    void should_throw_IllegalArgumentException_when_inputFolder_is_not_a_valid_directory() {
        assertThrows(IllegalArgumentException.class, () -> zoneConverter.convert("lux", "fake", capitalProvider));
        verifyZeroInteractions(a0, splitter, capitalProvider);
    }

    @Test
    void should_only_call_a0_with_ax_zone() {
        zoneConverter.convert("lux", "lux", capitalProvider);

        verifyZeroInteractions(a0, capitalProvider);
    }

    @Test
    void should_convert_and_split_a0_shapefile_for_ax_zone() {
        String productPbfFileName = "convertLuxAx.osm.pbf";
        when(a0.convert("lux", "ax", capitalProvider)).thenReturn(productPbfFileName);

        String generate = zoneConverter.convert("lux", "ax", capitalProvider);

        String zonePbfFileName = "target/generator/lux/ax/ax.osm.pbf";
        verify(splitter).split(Paths.get(zonePbfFileName));
        verify(osmMerger).merge(asList(productPbfFileName), Paths.get(zonePbfFileName));

        assertThat(generate).isEqualTo(zonePbfFileName);
    }

    @Test
    void should_convert_and_split_railroad_shapefile_for_none_ax_zone() {
        String rrPbfFileName = "convertRrLux.osm.pbf";
        when(railRoad.convert("lux", "lux")).thenReturn(rrPbfFileName);
        String waPbfFileName = "convertWaLux.osm.pbf";
        when(waterArea.convert("lux", "lux")).thenReturn(waPbfFileName);

        String generate = zoneConverter.convert("lux", "lux", capitalProvider);

        String zonePbfFileName = "target/generator/lux/lux/lux.osm.pbf";
        verify(splitter).split(Paths.get(zonePbfFileName));
        verify(osmMerger).merge(asList(rrPbfFileName, waPbfFileName), Paths.get(zonePbfFileName));

        assertThat(generate).isEqualTo(zonePbfFileName);
    }
}
