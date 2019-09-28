package fr.ignishky.fma.generator;

import fr.ignishky.fma.generator.converter.CountryConverter;
import fr.ignishky.fma.generator.merger.OsmMerger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static fr.ignishky.fma.generator.utils.TestConstants.RESOURCES_INPUT;
import static fr.ignishky.fma.generator.utils.TestConstants.TARGET_GENERATOR;
import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MainTest {

    private static final File INPUT_FOLDER = new File(RESOURCES_INPUT);
    private static final File OUTPUT_FOLDER = new File(TARGET_GENERATOR);
    private final ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

    private final CountryConverter countryConverter = mock(CountryConverter.class);
    private final OsmMerger osmMerger = mock(OsmMerger.class);

    @Test
    void should_throw_IllegalArgumentException_when_inputFolder_is_not_a_valid_directory() throws Exception {

        Main main = new Main(createTempDirectory("").toFile(), OUTPUT_FOLDER, countryConverter, osmMerger);

        assertThrows(IllegalArgumentException.class, main::run);
    }

    @Test
    void should_throw_IllegalArgumentException_when_inputFolder_is_empty() throws Exception {

        Main main = new Main(createTempDirectory("").toFile(), OUTPUT_FOLDER, countryConverter, osmMerger);

        assertThrows(IllegalArgumentException.class, main::run);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_delegate_to_country_generator() {

        when(countryConverter.convert("and")).thenReturn("and.osm.pbf");
        when(countryConverter.convert("lux")).thenReturn("lux.osm.pbf");

        Main main = new Main(INPUT_FOLDER, OUTPUT_FOLDER, countryConverter, osmMerger);

        main.run();

        verify(countryConverter, times(2)).convert(any(String.class));

        verify(osmMerger).merge(argumentCaptor.capture(), eq(Paths.get("target/generator/Europe.osm.pbf")));
        assertThat(argumentCaptor.getValue()).containsOnly("lux.osm.pbf", "and.osm.pbf");
    }
}
