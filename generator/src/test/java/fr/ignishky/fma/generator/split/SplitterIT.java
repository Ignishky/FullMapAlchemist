package fr.ignishky.fma.generator.split;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class SplitterIT {

    private final Splitter splitter = new Splitter(new SplitterSerializers(Paths.get("target", "generator").toFile()));

    @Test
    void should_split_file_into_smaller_ones() {
        splitter.split(Paths.get("src", "test", "resources", "output", "a0.osm.pbf"));

        assertThat(Paths.get("target", "generator", "splitter").toFile()).exists();
    }
}