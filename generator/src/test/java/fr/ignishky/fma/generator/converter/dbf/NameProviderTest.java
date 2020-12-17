package fr.ignishky.fma.generator.converter.dbf;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NameProviderTest {

    private final NameProvider nameProvider = new NameProvider();

    @Test
    void should_add_alternative_names() {
        nameProvider.loadAlternateNames(Path.of("src/test/resources/input/and/and/andand___________an.dbf"));

        Map<String, String> tags = nameProvider.getAlternateNames(10200000000008L);

        assertThat(tags).containsAllEntriesOf(Map.of(
                "name:ca", "Andorra_cat",
                "name:fr", "Andorre",
                "alt_name:fr", "Principauté d'andorre",
                "name:de", "Andorra_ger",
                "name:en", "Andorra_eng",
                "name:es", "Andorra_spa"
        ));
    }
}
