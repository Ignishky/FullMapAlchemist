package fr.ignishky.fma.generator.converter;

import fr.ignishky.fma.generator.converter.shapefile.A0Shapefile;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class ZoneConverterTest {

    private final A0Shapefile a0Shapefile = mock(A0Shapefile.class);
    private final OsmMerger osmMerger = mock(OsmMerger.class);
    private final CapitalProvider capitalProvider = mock(CapitalProvider.class);
    private final ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

    private final ZoneConverter zoneConverter = new ZoneConverter(new File("src/test/resources/input"), new File("target/generator"), a0Shapefile, osmMerger);

    @Test
    public void should_throw_IllegalArgumentException_when_inputFolder_is_not_a_valid_directory() {
        assertThrows(IllegalArgumentException.class, () -> zoneConverter.generate("lux", "fake", null));
        verifyZeroInteractions(a0Shapefile, capitalProvider);
    }

    @Test
    public void should_only_call_a0_with_ax_zone() {
        zoneConverter.generate("lux", "lux", capitalProvider);

        verifyZeroInteractions(a0Shapefile, capitalProvider);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_convert_all_shapefile_into_OSM_format() {
        when(a0Shapefile.convert("lux", "ax", capitalProvider)).thenReturn("convertLuxAx.osm.pbf");

        String generate = zoneConverter.generate("lux", "ax", capitalProvider);

        verify(osmMerger).merge(argumentCaptor.capture(), eq(Paths.get("target/generator/lux/ax/ax.osm.pbf")));
        assertThat(argumentCaptor.getValue()).containsOnly("convertLuxAx.osm.pbf");

        assertEquals("target/generator/lux/ax/ax.osm.pbf", generate);
    }
}
