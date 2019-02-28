package fr.ignishky.fma.generator.helper;

import com.google.inject.name.Named;
import fr.ignishky.fma.generator.reader.ShapefileIterator;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
public class CapitalProvider {

    private final File inputFolder;
    private final List<Centroid> allCapitals = new ArrayList<>();

    @Inject
    public CapitalProvider(@Named("inputFolder") File inputFolder) {
        this.inputFolder = inputFolder;
    }

    public void init(String countryCode) {
        log.info("Extract all capitals for country '{}'", countryCode);

        for (File zone : Paths.get(inputFolder.getAbsolutePath(), countryCode).toFile().listFiles()) {
            for (File file : Stream.of(zone.listFiles()).filter(file -> file.getName().endsWith("sm.shp")).collect(toList())) {
                try (ShapefileIterator iterator = new ShapefileIterator(file.toPath())) {
                    log.info("Reading {}", file);
                    while (iterator.hasNext()) {
                        Centroid centroid = Centroid.from(iterator.next());
                        if (centroid.getAdminclass() <= 7) {
                            allCapitals.add(centroid);
                        }
                    }
                }
            }
        }
        log.info("Extracted {} capitals for country '{}'", allCapitals.size(), countryCode);
    }

    public List<Centroid> get(int tomtomLevel) {
        return allCapitals.stream().filter(c -> c.getAdminclass() <= tomtomLevel).collect(toList());
    }
}
