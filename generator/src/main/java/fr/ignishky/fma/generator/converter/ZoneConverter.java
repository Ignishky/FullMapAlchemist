package fr.ignishky.fma.generator.converter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.generator.converter.shapefile.A0Shapefile;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

@Slf4j
public class ZoneConverter {

    private final File inputFolder;
    private final File outputFolder;
    private final A0Shapefile a0Shapefile;
    private final OsmMerger osmMerger;

    @Inject
    public ZoneConverter(@Named("inputFolder") File inputFolder, @Named("outputFolder") File outputFolder,
                         A0Shapefile a0Shapefile , OsmMerger osmMerger) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.a0Shapefile = a0Shapefile;
        this.osmMerger = osmMerger;
    }

    public String generate(String countryCode, String zoneCode, CapitalProvider capitalProvider) {
        String[] products = Paths.get(inputFolder.getPath(), countryCode, zoneCode).toFile().list();

        if (products == null) {
            throw new IllegalArgumentException(format("<inputFolder>/%s/%s must be a valid directory.", countryCode, zoneCode));
        }

        log.info("Generate zone '{}-{}' with products : {}", countryCode, zoneCode, of(products).map(s -> s.substring(17, 19)).collect(toSet()));

        List<String> convertFiles = new ArrayList<>(1);

        if ("ax".equals(zoneCode)) {
            convertFiles.add(a0Shapefile.convert(countryCode, zoneCode, capitalProvider));
        }

        Path outputFile = Paths.get(outputFolder.getPath(), countryCode, zoneCode, zoneCode + ".osm.pbf");

        osmMerger.merge(convertFiles, outputFile);

        return outputFile.toString();
    }
}
