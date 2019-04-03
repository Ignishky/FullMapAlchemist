package fr.ignishky.fma.generator.converter.product;

import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.reader.Feature;
import fr.ignishky.fma.generator.reader.ShapefileIterator;
import fr.ignishky.fma.generator.writer.GeometrySerializer;
import fr.ignishky.fma.generator.writer.OsmosisSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.ignishky.fma.generator.reader.Feature.Attribute.NAME;
import static java.lang.String.format;

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

    public String convert(String countryCode, String zoneCode, CapitalProvider capitalProvider) {

        nameProvider.loadAlternateNames(Paths.get(inputFolder.getPath(), countryCode, zoneCode, getNameFile(countryCode)));

        Path outputZoneFile = getOutputFile(countryCode, zoneCode);

        try (GeometrySerializer serializer = new OsmosisSerializer(outputZoneFile);
             ShapefileIterator iterator = getShapefileIterator(countryCode, zoneCode)) {

            while (iterator.hasNext()) {
                Feature next = iterator.next();
                if (next.getString(NAME) != null) {
                    serialize(serializer, next, capitalProvider);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(format("Unable to correctly close %s", outputZoneFile), e);
        }

        return outputZoneFile.toString();
    }

    private Path getOutputFile(String countryCode, String zoneCode) {
        Path outputZoneFolder = Paths.get(outputFolder.getPath(), countryCode, zoneCode, "products");
        if(!outputZoneFolder.toFile().exists() && !outputZoneFolder.toFile().mkdirs()) {
            throw new IllegalStateException("Unable to create folder " + outputZoneFolder);
        }
        return outputZoneFolder.resolve(getOutputFileName());
    }

    private ShapefileIterator getShapefileIterator(String countryCode, String zoneCode) {

        Path inputShapefile = Paths.get(inputFolder.getPath(), countryCode, zoneCode, getInputFile(countryCode));
        if (!inputShapefile.toFile().exists()) {
            throw new IllegalStateException("Missing file " + inputShapefile);
        }
        log.info("Opening {}", inputShapefile);

        return new ShapefileIterator(inputShapefile);
    }

    protected abstract String getInputFile(String countryCode);

    protected abstract String getNameFile(String countryCode);

    protected abstract String getOutputFileName();

    protected abstract void serialize(GeometrySerializer serializer, Feature feature, CapitalProvider capitalProvider);
}
