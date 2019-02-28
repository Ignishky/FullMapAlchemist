package fr.ignishky.fma.generator.converter.dbf;

import lombok.Value;
import org.jamel.dbf.structure.DbfRow;

import static java.nio.charset.StandardCharsets.UTF_8;

@Value
public class AlternativeName {

    Long id;
    String type;
    String name;
    String language;

    public static AlternativeName fromDbf(DbfRow entry) {
        return new AlternativeName(entry.getLong("ID"), entry.getString("NAMETYP"), entry.getString("NAME", UTF_8), entry.getString("NAMELC", UTF_8));
    }
}
