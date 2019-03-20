package fr.ignishky.fma.generator.helper;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;

public final class Layers {

    private Layers() {
    }

    public static int layer(Map<String, String> tags, boolean start, boolean end) {
        if (tags.containsKey("layer:from")) {
            if (start) {
                return layer(tags.get("layer:from"));
            } else if (end) {
                return layer(tags.get("layer:to"));
            }
        } else {
            return layer(tags.get("layer"));
        }
        return 0;
    }

    public static int layer(String input) {
        if (input != null) {
            int level = parseInt(input);
            if (level == 0) {
                return 0;
            }
            checkState(level >= -10 && level <= 10, "Too many layers");
            return 2 * Math.abs(level) - (level < 0 ? 0 : 1);
        }
        return 0;
    }
}
