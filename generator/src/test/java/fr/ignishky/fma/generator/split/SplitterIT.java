package fr.ignishky.fma.generator.split;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SplitterIT {

    private final Splitter splitter = new Splitter(new SplitterSerializers(Path.of("target", "generator").toFile()));

    @Test
    void should_split_file_into_smaller_ones() {
        splitter.split(Path.of("src", "test", "resources", "output", "a0.osm.pbf"));

        assertThat(Path.of("target", "generator", "splitter").toFile()).exists();
    }
}