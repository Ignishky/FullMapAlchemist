package fr.ignishky.fma.generator.converter.dbf;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
 class NameProviderTest {

    private final NameProvider nameProvider = new NameProvider();

    @Test
    void should_add_alternative_names() {
        nameProvider.loadAlternateNames(new File("src/test/resources/input/and/and/andand___________an.dbf"));
        Map<String, String> tags = nameProvider.getAlternateNames(10200000000008L);
        assertThat(tags).hasSize(6);
        assertThat(tags.get("name:ca")).isEqualTo("Andorra_cat");
        assertThat(tags.get("name:fr")).isEqualTo("Andorre");
        assertThat(tags.get("alt_name:fr")).isEqualTo("Principauté d'andorre");
        assertThat(tags.get("name:de")).isEqualTo("Andorra_ger");
        assertThat(tags.get("name:en")).isEqualTo("Andorra_eng");
        assertThat(tags.get("name:es")).isEqualTo("Andorra_spa");
    }
}
