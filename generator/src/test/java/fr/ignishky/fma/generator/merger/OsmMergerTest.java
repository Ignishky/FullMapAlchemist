package fr.ignishky.fma.generator.merger;

import com.google.common.io.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OsmMergerTest {

    private static final String PRODUCT_PDF = "src/test/resources/output/a0.osm.pbf";
    private static final String OUTPUT_PBF = "target/output.osm.pbf";

    private final OsmMerger osmMerger = new OsmMerger();

    @BeforeEach
    void clean() {
        new File(OUTPUT_PBF).delete();
    }

    @Test
    void should_copy_the_only_input_file() throws IOException {

        osmMerger.merge(List.of(PRODUCT_PDF), Path.of(OUTPUT_PBF));

        assertThat(Files.equal(new File(OUTPUT_PBF), new File(PRODUCT_PDF))).isTrue();
    }

    @Test
    void should_merge_all_input_files() throws IOException {

        osmMerger.merge(List.of("src/test/resources/output/bel.osm.pbf", "src/test/resources/output/lux.osm.pbf", "src/test/resources/output/nld.osm.pbf"), Path.of(OUTPUT_PBF));

        assertThat(Files.equal(new File(OUTPUT_PBF), new File("src/test/resources/output/merged.osm.pbf"))).isTrue();
    }
}
