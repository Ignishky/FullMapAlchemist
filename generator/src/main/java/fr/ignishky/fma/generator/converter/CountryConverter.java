package fr.ignishky.fma.generator.converter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static fr.ignishky.fma.generator.utils.Constants.INPUT_FOLDER;
import static fr.ignishky.fma.generator.utils.Constants.OUTPUT_FOLDER;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@Slf4j
public class CountryConverter {

    private final File inputFolder;
    private final File outputFolder;
    private final CapitalProvider capitalProvider;
    private final ZoneConverter zone;
    private final OsmMerger osmMerger;

    @Inject
    CountryConverter(@Named(INPUT_FOLDER) File inputFolder, @Named(OUTPUT_FOLDER) File outputFolder, CapitalProvider capitalProvider,
                     ZoneConverter zone, OsmMerger osmMerger) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.capitalProvider = capitalProvider;
        this.zone = zone;
        this.osmMerger = osmMerger;
    }

    public String convert(String countryCode) {

        Path countryFile = Paths.get(outputFolder.getPath(), countryCode, countryCode + ".osm.pbf");
        if(Files.exists(countryFile)) {
            log.info("File {} already exists.", countryFile.toString());
            return countryFile.toString();
        }

        String[] zones = Paths.get(inputFolder.getPath(), countryCode).toFile().list();

        if (zones == null || zones.length == 0) {
            throw new IllegalArgumentException(format("<inputFolder>/%s must be a valid non-empty directory.", countryCode));
        }

        log.info("Generate country '{}' with zones : {}", countryCode, zones);
        StopWatch watch = StopWatch.createStarted();

        capitalProvider.init(countryCode);

        List<String> convertedZoneFiles = stream(zones)
                .map(zoneCode -> zone.convert(countryCode, zoneCode, capitalProvider))
                .collect(toList());

        osmMerger.merge(convertedZoneFiles, countryFile);
        log.info("Country {} generated in {} ms", countryCode, watch.getTime());

        return countryFile.toString();
    }
}
