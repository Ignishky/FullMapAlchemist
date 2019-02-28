package fr.ignishky.fma.generator;

import fr.ignishky.fma.generator.converter.CountryConverter;
import fr.ignishky.fma.generator.merger.OsmMerger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MainTest {

    private static final File INPUT_FOLDER = new File("src/test/resources/input");
    private static final File OUTPUT_FOLDER = new File("target/generator");

    private final CountryConverter countryConverter = mock(CountryConverter.class);
    private final OsmMerger osmMerger = mock(OsmMerger.class);

    @Test
    public void should_throw_IllegalArgumentException_when_inputFolder_is_not_a_valid_directory() {

        Main main = new Main(new File("pom.xml"), OUTPUT_FOLDER, countryConverter, osmMerger);

        assertThrows(IllegalArgumentException.class, main::run);
    }

    @Test
    public void should_delegate_to_country_generator() {

        when(countryConverter.generate("and")).thenReturn("and.osm.pbf");
        when(countryConverter.generate("lux")).thenReturn("lux.osm.pbf");

        Main main = new Main(INPUT_FOLDER, OUTPUT_FOLDER, countryConverter, osmMerger);

        main.run();

        verify(countryConverter, times(2)).generate(any(String.class));
        verify(countryConverter).generate("and");
        verify(countryConverter).generate("lux");
        verify(osmMerger).merge(newArrayList("lux.osm.pbf", "and.osm.pbf"), Paths.get("target/generator/Europe.osm.pbf"));
    }
}
