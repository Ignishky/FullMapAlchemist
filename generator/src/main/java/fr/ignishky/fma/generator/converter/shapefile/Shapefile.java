package fr.ignishky.fma.generator.converter.shapefile;

import fr.ignishky.fma.generator.converter.dbf.NameProvider;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.reader.Feature;
import fr.ignishky.fma.generator.reader.ShapefileIterator;
import fr.ignishky.fma.generator.writer.GeometrySerializer;
import fr.ignishky.fma.generator.writer.OsmosisSerializer;
import fr.ignishky.fma.generator.writer.PbfSink;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;

@Slf4j
public abstract class Shapefile {

    private final File inputFolder;
    protected final NameProvider nameProvider;
    private final File outputFolder;

    protected Shapefile(File inputFolder, NameProvider nameProvider, File outputFolder) {
        this.inputFolder = inputFolder;
        this.nameProvider = nameProvider;
        this.outputFolder = outputFolder;
    }

    public String convert(String countryCode, String zoneCode, CapitalProvider capitalProvider) {

        Path inputShapefile = Paths.get(inputFolder.getPath(), countryCode, zoneCode, getInputFile(countryCode));
        Path outputZoneFolder = Paths.get(outputFolder.getPath(), countryCode, zoneCode, "products");
        Path outputZoneFile = outputZoneFolder.resolve(getOutputFileName());

        nameProvider.loadAlternateNames(Paths.get(inputFolder.getPath(), countryCode, zoneCode, getNameFile(countryCode)).toFile());

        try (GeometrySerializer serializer = getSerializer(outputZoneFolder);
             ShapefileIterator iterator = getIterator(inputShapefile)) {

            while (iterator.hasNext()) {
                Feature next = iterator.next();
                if (next.getString("NAME") != null) {
                    serialize(serializer, next, capitalProvider);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(format("Unable to correctly close %s", outputZoneFile), e);
        }

        return outputZoneFile.toString();
    }

    private ShapefileIterator getIterator(Path inputShapefile) {

        if (!inputShapefile.toFile().exists()) {
            throw new IllegalStateException("Missing file " + inputShapefile);
        }
        log.info("Opening {}", inputShapefile);

        return new ShapefileIterator(inputShapefile);
    }

    private OsmosisSerializer getSerializer(Path outputFolder) {

        outputFolder.toFile().mkdirs();
        Path outputFile = outputFolder.resolve(getOutputFileName());

        return new OsmosisSerializer(new PbfSink(outputFile));
    }

    protected abstract String getInputFile(String countryCode);

    protected abstract String getNameFile(String countryCode);

    protected abstract String getOutputFileName();

    protected abstract void serialize(GeometrySerializer serializer, Feature feature, CapitalProvider capitalProvider);
}
