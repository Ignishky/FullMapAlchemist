package fr.ignishky.fma.generator.merger;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsmMergerTest {

    @Test
    void should_copy_the_only_input_file() throws IOException {
        new File("target/output.osm.pbf").delete();

        new OsmMerger().merge(newArrayList("src/test/resources/output/a0.osm.pbf"), Paths.get("target/output.osm.pbf"));

        assertTrue(Files.equal(new File("target/output.osm.pbf"), new File("src/test/resources/output/a0.osm.pbf")));
    }

    @Test
    void should_ignore_non_existing_file() throws IOException {
        new File("target/output.osm.pbf").delete();

        new OsmMerger().merge(
                newArrayList("src/test/resources/output/fake.osm.pbf", "src/test/resources/output/a0.osm.pbf"),
                Paths.get("target/output.osm.pbf"));

        assertTrue(Files.equal(new File("target/output.osm.pbf"), new File("src/test/resources/output/a0.osm.pbf")));
    }
}
