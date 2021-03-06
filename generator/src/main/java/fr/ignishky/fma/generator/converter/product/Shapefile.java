package fr.ignishky.fma.generator.converter.product;

import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.reader.Feature;
import fr.ignishky.fma.generator.reader.ShapefileIterator;
import fr.ignishky.fma.generator.writer.OsmosisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

@Slf4j
public abstract class Shapefile {

    private final File inputFolder;
    private final File outputFolder;
    protected final NameProvider nameProvider;

    Shapefile(File inputFolder, NameProvider nameProvider, File outputFolder) {
        this.inputFolder = inputFolder;
        this.nameProvider = nameProvider;
        this.outputFolder = outputFolder;
    }

    public String convert(String countryCode, String zoneCode) {
        return convert(countryCode, zoneCode, null);
    }

    public String convert(String countryCode, String zoneCode, CapitalProvider capitalProvider) {

        Path outputZoneFile = getOutputFile(countryCode, zoneCode);
        if (exists(outputZoneFile)) {
            log.info("File {} already present.", outputZoneFile);
            return outputZoneFile.toString();
        }

        log.info("Generate product '{}' for zone '{}-{}'", getProductName(), countryCode, zoneCode);
        StopWatch watch = StopWatch.createStarted();

        if (getNameFile(countryCode) != null) {
            nameProvider.loadAlternateNames(Path.of(inputFolder.getPath(), countryCode, zoneCode, getNameFile(countryCode)));
        }

        try (OsmosisSerializer serializer = new OsmosisSerializer(outputZoneFile);
             ShapefileIterator iterator = getShapefileIterator(countryCode, zoneCode)) {

            while (iterator.hasNext()) {
                serialize(serializer, iterator.next(), capitalProvider);
            }
        }

        log.info("Product {}-{}-{} generated in {} ms", countryCode, zoneCode, getProductName(), watch.getTime());
        return outputZoneFile.toString();
    }

    private Path getOutputFile(String countryCode, String zoneCode) {
        Path outputZoneFolder = Path.of(outputFolder.getPath(), countryCode, zoneCode, "products");
        try {
            createDirectories(outputZoneFolder);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create folder " + outputZoneFolder, e);
        }
        if (!exists(outputZoneFolder)) {
            throw new IllegalStateException("Unable to create folder " + outputZoneFolder);
        }
        return outputZoneFolder.resolve(getOutputFileName());
    }

    private ShapefileIterator getShapefileIterator(String countryCode, String zoneCode) {

        Path inputShapefile = Path.of(inputFolder.getPath(), countryCode, zoneCode, getInputFile(countryCode, zoneCode));
        if (!exists(inputShapefile)) {
            throw new IllegalStateException("Missing file " + inputShapefile);
        }
        log.info("Reading SHP {}", inputShapefile);

        return new ShapefileIterator(inputShapefile);
    }

    protected abstract String getProductName();

    protected abstract String getInputFile(String countryCode, String zoneCode);

    protected abstract String getNameFile(String countryCode);

    protected abstract String getOutputFileName();

    protected abstract void serialize(OsmosisSerializer serializer, Feature feature, CapitalProvider capitalProvider);
}
