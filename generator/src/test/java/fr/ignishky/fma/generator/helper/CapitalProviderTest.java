package fr.ignishky.fma.generator.helper;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapitalProviderTest {

    CapitalProvider capitalProvider = new CapitalProvider(new File("src/test/resources/input"));

    @Test
    public void should_load_capital_from_zones() {
        capitalProvider.init("and");

        assertEquals(capitalProvider.get(0).size(), 1);
        assertEquals(capitalProvider.get(0).get(0).getName(), "Andorra la Vella");
    }
}