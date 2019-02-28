package fr.ignishky.fma.generator;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.generator.converter.CountryConverter;
import fr.ignishky.fma.generator.merger.OsmMerger;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.inject.Guice.createInjector;
import static java.util.stream.Collectors.toList;

@Slf4j
public class Main {

    private final File inputFolder;
    private final File outputFolder;
    private final CountryConverter countryConverter;
    private final OsmMerger osmMerger;

    @Inject
    public Main(@Named("inputFolder") File inputFolder, @Named("outputFolder") File outputFolder, CountryConverter countryConverter,
                OsmMerger osmMerger) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.countryConverter = countryConverter;
        this.osmMerger = osmMerger;
    }

    public static void main(String[] args) {
        checkArgument(args.length == 2, "Usage : Main <inputFolder> <outputFolder>");

        createInjector(new GeneratorModule(args[0], args[1])).getInstance(Main.class).run();
    }

    /* package */ void run() {
        String[] countries = inputFolder.list();

        if (countries == null) {
            throw new IllegalArgumentException("<inputFolder> must be a valid directory.");
        }

        log.info("Generating OSM file with countries : {}", Arrays.toString(countries));

        List<String> convertedCountryFiles = Stream.of(countries).map(countryConverter::generate).collect(toList());

        osmMerger.merge(convertedCountryFiles, Paths.get(outputFolder.getPath(), "Europe.osm.pbf"));
    }
}
