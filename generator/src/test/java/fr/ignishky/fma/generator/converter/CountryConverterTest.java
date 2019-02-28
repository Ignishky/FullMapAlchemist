package fr.ignishky.fma.generator.converter;

import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class CountryConverterTest {

    private final CapitalProvider capitalProvider = mock(CapitalProvider.class);
    private final ZoneConverter zoneConverter = mock(ZoneConverter.class);
    private final OsmMerger osmMerger = mock(OsmMerger.class);

    private final CountryConverter countryConverter = new CountryConverter(new File("src/test/resources/input"), new File("target/generator"),
            capitalProvider, zoneConverter, osmMerger);

    @Test
    public void should_throw_IllegalArgumentException_when_inputFolder_is_not_a_valid_directory() {
        assertThrows(IllegalArgumentException.class, () -> countryConverter.generate("fake"));
        verifyZeroInteractions(capitalProvider, zoneConverter);
    }

    @Test
    public void should_generate_zones_and_merge_them_into_country_file() {

        when(zoneConverter.generate("lux", "ax", capitalProvider)).thenReturn("lux-ax.osm.pbf");
        when(zoneConverter.generate("lux", "lux", capitalProvider)).thenReturn("lux-lux.osm.pbf");

        String generate = countryConverter.generate("lux");

        verify(capitalProvider).init("lux");
        verify(osmMerger).merge(newArrayList("lux-lux.osm.pbf", "lux-ax.osm.pbf"), Paths.get("target/generator/lux/lux.osm.pbf"));
        assertEquals("target/generator/lux/lux.osm.pbf", generate);
    }
}
