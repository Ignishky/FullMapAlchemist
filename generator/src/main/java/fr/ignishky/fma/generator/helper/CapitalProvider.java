package fr.ignishky.fma.generator.helper;

import com.google.inject.name.Named;
import fr.ignishky.fma.generator.reader.ShapefileIterator;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static fr.ignishky.fma.generator.utils.Constants.INPUT_FOLDER;
import static java.lang.String.format;
import static java.util.Arrays.stream;

@Slf4j
public class CapitalProvider {

    private final File inputFolder;
    private final List<Centroid> capitals = new ArrayList<>();

    @Inject
    CapitalProvider(@Named(INPUT_FOLDER) File inputFolder) {
        this.inputFolder = inputFolder;
    }

    public void init(String countryCode) {
        log.info("Extracting all capitals for country '{}'", countryCode);
        capitals.clear();

        File[] zoneDirectories = Path.of(inputFolder.getAbsolutePath(), countryCode).toFile().listFiles();
        if (zoneDirectories == null || zoneDirectories.length == 0) {
            throw new IllegalArgumentException(format("<inputFolder>/%s should be a non empty folder.", countryCode));
        }

        stream(zoneDirectories)
                .map(File::listFiles)
                .flatMap(Stream::of)
                .filter(file -> file.getName().endsWith("sm.shp"))
                .forEach(this::extractCapitals);

        log.info("Extracted {} capitals for country '{}'", capitals.size(), countryCode);
    }

    private void extractCapitals(File file) {
        try (ShapefileIterator iterator = new ShapefileIterator(file.toPath())) {
            log.info("Reading SM file : {}", file);
            while (iterator.hasNext()) {
                Centroid centroid = Centroid.from(iterator.next());
                if (centroid.getAdminClass() <= 7) {
                    capitals.add(centroid);
                }
            }
        }
    }

    public Stream<Centroid> forLevel(int tomtomLevel) {
        return capitals.stream().filter(city -> city.getAdminClass() <= tomtomLevel);
    }
}
