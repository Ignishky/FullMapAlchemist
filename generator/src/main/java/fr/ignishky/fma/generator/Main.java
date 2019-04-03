package fr.ignishky.fma.generator;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.generator.converter.CountryConverter;
import fr.ignishky.fma.generator.merger.OsmMerger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.inject.Guice.createInjector;
import static fr.ignishky.fma.generator.utils.Constants.INPUT_FOLDER;
import static fr.ignishky.fma.generator.utils.Constants.OUTPUT_FOLDER;
import static java.util.stream.Collectors.toList;

@Slf4j
public class Main {

    private final File inputFolder;
    private final File outputFolder;
    private final CountryConverter country;
    private final OsmMerger osmMerger;

    @Inject
    Main(@Named(INPUT_FOLDER) File inputFolder, @Named(OUTPUT_FOLDER) File outputFolder, CountryConverter country, OsmMerger osmMerger) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.country = country;
        this.osmMerger = osmMerger;
    }

    public static void main(String[] args) {
        checkArgument(args.length == 2, "Usage : Main <inputFolder> <outputFolder>");

        createInjector(new GeneratorModule(args[0], args[1])).getInstance(Main.class).run();
    }

    void run() {
        String[] countries = inputFolder.list();

        if (countries == null || countries.length == 0) {
            throw new IllegalArgumentException("<inputFolder> must be a valid non-empty directory.");
        }

        log.info("Generating Europe file with countries : {}", Arrays.toString(countries));
        StopWatch watch = StopWatch.createStarted();

        List<String> convertedCountries = Stream.of(countries).map(country::convert).collect(toList());

        osmMerger.merge(convertedCountries, Paths.get(outputFolder.getPath(), "Europe.osm.pbf"));

        log.info("Europe.osm.pbf generated in {} ms", watch.getTime());
    }
}
