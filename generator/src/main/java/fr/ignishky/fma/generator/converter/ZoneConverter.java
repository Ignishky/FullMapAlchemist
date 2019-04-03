package fr.ignishky.fma.generator.converter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.generator.converter.product.A0Shapefile;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.ignishky.fma.generator.utils.Constants.INPUT_FOLDER;
import static fr.ignishky.fma.generator.utils.Constants.OUTPUT_FOLDER;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

@Slf4j
class ZoneConverter {

    private static final Pattern PATTERN_PRODUCT_FILE = Pattern.compile("^\\w{6}___________(.*)\\.[a-z]{3}");

    private final File inputFolder;
    private final File outputFolder;
    private final A0Shapefile a0Shapefile;
    private final OsmMerger osmMerger;

    @Inject
    ZoneConverter(@Named(INPUT_FOLDER) File inputFolder, @Named(OUTPUT_FOLDER) File outputFolder, A0Shapefile a0Shapefile, OsmMerger osmMerger) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.a0Shapefile = a0Shapefile;
        this.osmMerger = osmMerger;
    }

    String convert(String countryCode, String zoneCode, CapitalProvider capitalProvider) {
        String[] products = Paths.get(inputFolder.getPath(), countryCode, zoneCode).toFile().list();

        if (products == null || products.length == 0) {
            throw new IllegalArgumentException(format("<inputFolder>/%s/%s must be a valid non-empty directory.", countryCode, zoneCode));
        }

        log.info("Generate zone '{}-{}' with products : {}", countryCode, zoneCode, toZone(products));

        List<String> convertFiles = new ArrayList<>(1);

        if ("ax".equals(zoneCode)) {
            convertFiles.add(a0Shapefile.convert(countryCode, zoneCode, capitalProvider));
        }

        //TODO : To delete when all zone have data.
        if (convertFiles.isEmpty()) {
            return null;
        }

        Path outputFile = Paths.get(outputFolder.getPath(), countryCode, zoneCode, zoneCode + ".osm.pbf");
        osmMerger.merge(convertFiles, outputFile);

        return outputFile.toString();
    }

    private static Set<String> toZone(String[] products) {
        return of(products).map(PATTERN_PRODUCT_FILE::matcher).filter(Matcher::matches).map(matcher -> matcher.group(1)).collect(toSet());
    }
}
