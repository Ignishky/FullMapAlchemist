package fr.ignishky.fma.generator.converter.dbf;

import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class NameProvider {

    private final Map<Long, List<AlternativeName>> alternateNames = new HashMap<>();

    public void loadAlternateNames(Path path) {
        log.info("Reading {}", path);

        try (DbfReader reader = new DbfReader(path.toFile())) {
            DbfRow row;

            while ((row = reader.nextRow()) != null) {
                AlternativeName altName = AlternativeName.fromDbf(row);
                List<AlternativeName> altNames = alternateNames.getOrDefault(altName.getId(), new ArrayList<>());
                altNames.add(altName);
                alternateNames.put(altName.getId(), altNames);
            }
        }
    }

    public Map<String, String> getAlternateNames(Long tomtomId) {
        return alternateNames.getOrDefault(tomtomId, new ArrayList<>()).stream()
                .collect(toMap(NameProvider::getKeyAlternativeName, AlternativeName::getName, (key1, key2) -> key2));
    }

    private static String getKeyAlternativeName(AlternativeName alternativeName) {
        String keyPrefix = "ON".equals(alternativeName.getType()) ? "name:" : "alt_name:";

        try {
            return keyPrefix + Language.valueOf(alternativeName.getLanguage()).getValue();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not find language : " + alternativeName.getLanguage(), e);
        }
    }
}
