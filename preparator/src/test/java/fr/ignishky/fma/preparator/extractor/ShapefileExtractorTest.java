package fr.ignishky.fma.preparator.extractor;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShapefileExtractorTest {

    private final ShapefileExtractor shapefileExtractor = new ShapefileExtractor(new File("target/preparator/extractor"));

    @Test
    void should_throw_IllegalArgumentException_when_file_dont_not_exist() {
        assertThrows(IllegalArgumentException.class, () -> shapefileExtractor.decompress(new File("fakeFile.7z")));
    }

    @Test
    void should_throw_IllegalArgumentException_when_file_is_not_7zip() throws Exception {

        Path tempFile = Files.createTempFile("extractor", "");
        Files.copy(Paths.get("src/test/resources/downloader/malformed_archive_name.7z.001"), tempFile, REPLACE_EXISTING);

        assertThrows(IllegalArgumentException.class, () -> shapefileExtractor.decompress(tempFile.toFile()));
        assertThat(tempFile.toFile().exists()).isTrue();
    }

    @Test
    void should_throw_IllegalArgumentException_when_file_is_not_a_valid_7zip() throws Exception {

        Path tempFile = Files.createTempFile("", "-shp-empty-unvalid-archive.7z.001");
        Files.copy(Paths.get("src/test/resources/downloader/eur2018_09-shp-empty-unvalid-archive.7z.001"), tempFile, REPLACE_EXISTING);

        assertThrows(IllegalArgumentException.class, () -> shapefileExtractor.decompress(tempFile.toFile()));
    }

    @Test
    void should_extract_files_from_valid_7zip() throws Exception {

        Path tempFile = Files.createTempFile("", "-shpd-mn-lux-ax.7z.001");
        Files.copy(Paths.get("src/test/resources/downloader/eur2018_09-shpd-mn-lux-ax.7z.001"), tempFile, REPLACE_EXISTING);

        shapefileExtractor.decompress(tempFile.toFile());

        assertThat(new File("target/preparator/extractor/lux")).exists();
        assertThat(new File("target/preparator/extractor/lux/ax")).exists();
        assertThat(new File("target/preparator/extractor/lux/ax/lux______________a0.shp")).exists();
    }
}