package fr.ignishky.fma.preparator.extractor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

import static fr.ignishky.fma.preparator.extractor.TomtomFile.allFilesFrom;
import static fr.ignishky.fma.preparator.utils.Constants.OUTPUT_FOLDER;
import static fr.ignishky.fma.preparator.utils.Constants.PATTERN_7ZIP_FILE;
import static java.nio.file.Files.delete;

@Slf4j
public class ShapefileExtractor {

    private final File outputFolder;

    @Inject
    ShapefileExtractor(@Named(OUTPUT_FOLDER) File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void decompress(File file) {

        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exists : " + file.getAbsolutePath());
        }

        Matcher matcher = PATTERN_7ZIP_FILE.matcher(file.getName());
        if(!matcher.matches()) {
            throw new IllegalArgumentException("File does not match 7z pattern (" + PATTERN_7ZIP_FILE + ") : " + file.getAbsolutePath());
        }
        String product = matcher.group(3);
        String countryCode = matcher.group(4);
        String zone = matcher.group(5);

        try (SevenZFile archive = new SevenZFile(file)) {

            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                String filename = Paths.get(entry.getName()).getFileName().toString();

                if (allFilesFrom(product).anyMatch(filename::contains)) {
                    log.info("Extracting {}", filename);
                    byte[] content = new byte[(int) entry.getSize()];
                    archive.read(content, 0, content.length);
                    File zoneDirectory = Paths.get(outputFolder.getAbsolutePath(), countryCode, zone).toFile();
                    zoneDirectory.mkdirs();
                    File outputFile = Paths.get(zoneDirectory.getAbsolutePath(), filename.replace(".gz", "")).toFile();
                    try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(content));
                         FileOutputStream output = new FileOutputStream(outputFile)) {
                        IOUtils.copy(input, output);
                    }
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Something went wrong while extracting " + file.getName(), e);

        } finally {
            try {
                delete(file.toPath());
            } catch (IOException e) {
                log.warn("Unable to delete file {}", file.getName());
            }
        }
    }
}
