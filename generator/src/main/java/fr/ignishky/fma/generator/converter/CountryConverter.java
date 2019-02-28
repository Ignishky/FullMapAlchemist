package fr.ignishky.fma.generator.converter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.generator.helper.CapitalProvider;
import fr.ignishky.fma.generator.merger.OsmMerger;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Slf4j
public class CountryConverter {

    private final File inputFolder;
    private final File outputFolder;
    private final CapitalProvider capitalProvider;
    private final ZoneConverter zoneConverter;
    private final OsmMerger osmMerger;

    @Inject
    public CountryConverter(@Named("inputFolder") File inputFolder, @Named("outputFolder") File outputFolder, CapitalProvider capitalProvider,
                            ZoneConverter zoneConverter, OsmMerger osmMerger) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.capitalProvider = capitalProvider;
        this.zoneConverter = zoneConverter;
        this.osmMerger = osmMerger;
    }

    public String generate(String countryCode) {

        String[] zones = Paths.get(inputFolder.getPath(), countryCode).toFile().list();

        if (zones == null) {
            throw new IllegalArgumentException(format("<inputFolder>/%s must be a valid directory.", countryCode));
        }

        log.info("Generate country '{}' with zones : {}", countryCode, zones);

        capitalProvider.init(countryCode);

        List<String> convertedZoneFiles = Stream.of(zones)
                .map(zoneCode -> zoneConverter.generate(countryCode, zoneCode, capitalProvider))
                .collect(toList());

        Path outputFile = Paths.get(outputFolder.getPath(), countryCode, countryCode + ".osm.pbf");

        osmMerger.merge(convertedZoneFiles, outputFile);

        return outputFile.toString();
    }
}
