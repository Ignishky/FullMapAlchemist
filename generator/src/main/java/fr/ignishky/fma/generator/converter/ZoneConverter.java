package fr.ignishky.fma.generator.converter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.generator.converter.product.A0;
import fr.ignishky.fma.generator.converter.product.RailRoad;
import fr.ignishky.fma.generator.converter.product.WaterArea;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import fr.ignishky.fma.generator.split.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

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
import static java.nio.file.Files.exists;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

@Slf4j
class ZoneConverter {

    private static final Pattern PATTERN_PRODUCT_FILE = Pattern.compile("^(\\w{6}|\\w{3}___)___________(.*)\\.shp");

    private final File inputFolder;
    private final File outputFolder;
    private final A0 a0;
    private final RailRoad railRoad;
    private final WaterArea waterArea;
    private final OsmMerger osmMerger;
    private final Splitter splitter;

    @Inject
    ZoneConverter(@Named(INPUT_FOLDER) File inputFolder, @Named(OUTPUT_FOLDER) File outputFolder, A0 a0,
                  RailRoad railRoad, WaterArea waterArea, OsmMerger osmMerger, Splitter splitter) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.a0 = a0;
        this.railRoad = railRoad;
        this.waterArea = waterArea;
        this.osmMerger = osmMerger;
        this.splitter = splitter;
    }

    public String convert(String countryCode, String zoneCode, CapitalProvider capitalProvider) {
        Path outputFile = Paths.get(outputFolder.getPath(), countryCode, zoneCode, zoneCode + ".osm.pbf");
        if (exists(outputFile)) {
            log.info("File {} already exists.", outputFile.toString());
            return outputFile.toString();
        }

        String[] products = Paths.get(inputFolder.getPath(), countryCode, zoneCode).toFile().list();

        if (products == null || products.length == 0) {
            throw new IllegalArgumentException(format("<inputFolder>/%s/%s must be a valid non-empty directory.", countryCode, zoneCode));
        }

        log.info("Generate zone '{}-{}' with products : {}", countryCode, zoneCode, toZone(products));
        StopWatch watch = StopWatch.createStarted();

        List<String> convertFiles = new ArrayList<>();

        if ("ax".equals(zoneCode)) {
            convertFiles.add(a0.convert(countryCode, zoneCode, capitalProvider));
        } else {
            convertFiles.add(railRoad.convert(countryCode, zoneCode));
            convertFiles.add(waterArea.convert(countryCode, zoneCode));
        }

        osmMerger.merge(convertFiles, outputFile);

        splitter.split(outputFile);

        log.info("Zone {}-{} generated in {} ms", countryCode, zoneCode, watch.getTime());
        return outputFile.toString();
    }

    private static Set<String> toZone(String[] products) {
        return stream(products)
                .map(PATTERN_PRODUCT_FILE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(2))
                .collect(toSet());
    }
}
