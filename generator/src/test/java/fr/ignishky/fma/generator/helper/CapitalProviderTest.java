package fr.ignishky.fma.generator.helper;

import org.junit.jupiter.api.Test;

import java.io.File;

import static fr.ignishky.fma.generator.utils.TestConstants.RESOURCES_INPUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CapitalProviderTest {

    private final CapitalProvider capitalProvider = new CapitalProvider(new File(RESOURCES_INPUT));

    @Test
    void should_throw_IllegalArgumentException_when_country_file_does_not_exists() {
        assertThrows(IllegalArgumentException.class, () -> capitalProvider.init("fake"));
    }

    @Test
    void should_load_capital_from_zones() {
        capitalProvider.init("and");

        assertThat(capitalProvider.forLevel(0)).hasSize(1);
        assertThat(capitalProvider.forLevel(0).map(Centroid::getName)).containsOnly("Andorra la Vella");
    }
}
