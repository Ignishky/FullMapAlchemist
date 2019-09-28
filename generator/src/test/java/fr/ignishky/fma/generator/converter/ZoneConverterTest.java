package fr.ignishky.fma.generator.converter;

import fr.ignishky.fma.generator.converter.product.A0Shapefile;
import fr.ignishky.fma.generator.converter.product.RailRoadShapefile;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import fr.ignishky.fma.generator.split.Splitter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static fr.ignishky.fma.generator.utils.TestConstants.RESOURCES_INPUT;
import static fr.ignishky.fma.generator.utils.TestConstants.TARGET_GENERATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class ZoneConverterTest {

    private final A0Shapefile a0Shapefile = mock(A0Shapefile.class);
    private final RailRoadShapefile railRoadShapefile = mock(RailRoadShapefile.class);
    private final OsmMerger osmMerger = mock(OsmMerger.class);
    private final Splitter splitter = mock(Splitter.class);
    private final CapitalProvider capitalProvider = mock(CapitalProvider.class);
    private final ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

    private final ZoneConverter zoneConverter = new ZoneConverter(new File(RESOURCES_INPUT), new File(TARGET_GENERATOR), a0Shapefile,
            railRoadShapefile, osmMerger, splitter);

    @Test
    void should_throw_IllegalArgumentException_when_inputFolder_is_not_a_valid_directory() {
        assertThrows(IllegalArgumentException.class, () -> zoneConverter.convert("lux", "fake", capitalProvider));
        verifyZeroInteractions(a0Shapefile, splitter, capitalProvider);
    }

    @Test
    void should_only_call_a0_with_ax_zone() {
        zoneConverter.convert("lux", "lux", capitalProvider);

        verifyZeroInteractions(a0Shapefile, capitalProvider);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_convert_and_split_a0_shapefile_for_ax_zone() {
        String productPbfFileName = "convertLuxAx.osm.pbf";
        when(a0Shapefile.convert("lux", "ax", capitalProvider)).thenReturn(productPbfFileName);

        String generate = zoneConverter.convert("lux", "ax", capitalProvider);

        String zonePbfFileName = "target/generator/lux/ax/ax.osm.pbf";
        verify(splitter).split(Paths.get(zonePbfFileName));
        verify(osmMerger).merge(argumentCaptor.capture(), eq(Paths.get(zonePbfFileName)));
        assertThat(argumentCaptor.getValue()).containsOnly(productPbfFileName);

        assertThat(generate).isEqualTo(zonePbfFileName);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_convert_and_split_railroad_shapefile_for_none_ax_zone() {
        String productPbfFileName = "convertLuxLux.osm.pbf";
        when(railRoadShapefile.convert("lux", "lux")).thenReturn(productPbfFileName);

        String generate = zoneConverter.convert("lux", "lux", capitalProvider);

        String zonePbfFileName = "target/generator/lux/lux/lux.osm.pbf";
        verify(splitter).split(Paths.get(zonePbfFileName));
        verify(osmMerger).merge(argumentCaptor.capture(), eq(Paths.get(zonePbfFileName)));
        assertThat(argumentCaptor.getValue()).containsOnly(productPbfFileName);

        assertThat(generate).isEqualTo(zonePbfFileName);
    }
}
