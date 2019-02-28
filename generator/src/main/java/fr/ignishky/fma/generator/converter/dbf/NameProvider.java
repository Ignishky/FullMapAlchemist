package fr.ignishky.fma.generator.converter.dbf;

import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class NameProvider {

    private final Map<Long, List<AlternativeName>> alternateNames = new HashMap<>();

    public void loadAlternateNames(File file) {
        log.info("Reading {}", file);

        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;

            while ((row = reader.nextRow()) != null) {
                AlternativeName altName = AlternativeName.fromDbf(row);
                List<AlternativeName> altNames = alternateNames.containsKey(altName.getId()) ? alternateNames.get(altName.getId()) : newArrayList();
                altNames.add(altName);
                alternateNames.put(altName.getId(), altNames);
            }
        }
    }

    public Map<String, String> getAlternateNames(Long tomtomId) {
        return ofNullable(alternateNames.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .collect(toMap(this::getKeyAlternativeName, AlternativeName::getName, (key1, key2) -> key2));
    }

    private String getKeyAlternativeName(AlternativeName alternativeName) {
        String keyPrefix = "ON".equals(alternativeName.getType()) ? "name:" : "alt_name:";
        return ofNullable(Enums.getIfPresent(Language.class, alternativeName.getLanguage()).orNull())
                .map(language -> keyPrefix + language.getValue())
                .orElseThrow(() -> new IllegalArgumentException("Could not find language : " + alternativeName.getLanguage()));
    }
}
