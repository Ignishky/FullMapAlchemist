package fr.ignishky.fma.generator.converter;

import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static fr.ignishky.fma.generator.utils.TestConstants.RESOURCES_INPUT;
import static fr.ignishky.fma.generator.utils.TestConstants.TARGET_GENERATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class CountryConverterTest {

    private static final String LUX_OSM_PBF = "target/generator/lux/lux.osm.pbf";

    @Mock
    private CapitalProvider capitalProvider;
    @Mock
    private ZoneConverter zoneConverter;
    @Mock
    private OsmMerger osmMerger;
    @Captor
    private ArgumentCaptor<List<String>> argumentCaptor;

    private CountryConverter countryConverter;

    @BeforeEach
    public void init(){
        initMocks(this);

        countryConverter = new CountryConverter(new File(RESOURCES_INPUT), new File(TARGET_GENERATOR), capitalProvider, zoneConverter, osmMerger);
    }

    @Test
    void should_throw_IllegalArgumentException_when_country_folder_is_not_a_valid_directory() {
        assertThrows(IllegalArgumentException.class, () -> countryConverter.convert("fake"));
        verifyZeroInteractions(capitalProvider, zoneConverter);
    }

    @Test
    void should_generate_zones_and_merge_them_into_country_file() {

        when(zoneConverter.convert("lux", "ax", capitalProvider)).thenReturn("lux-ax.osm.pbf");
        when(zoneConverter.convert("lux", "lux", capitalProvider)).thenReturn("lux-lux.osm.pbf");

        String generate = countryConverter.convert("lux");

        verify(capitalProvider).init("lux");

        verify(osmMerger).merge(argumentCaptor.capture(), eq(Paths.get(LUX_OSM_PBF)));
        assertThat(argumentCaptor.getValue()).containsOnly("lux-lux.osm.pbf", "lux-ax.osm.pbf");

        assertThat(generate).isEqualTo(LUX_OSM_PBF);
    }
}
