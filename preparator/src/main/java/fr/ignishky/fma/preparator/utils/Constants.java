package fr.ignishky.fma.preparator.utils;

import java.util.regex.Pattern;

public enum Constants {
    ;

    public static final String VERSION = "version";
    public static final String TOKEN = "token";
    public static final String OUTPUT_FOLDER = "outputFolder";

    // Ex : eur2018_09-shpd-mn-lux-ax.7z.001
    public static final Pattern PATTERN_7ZIP_FILE = Pattern.compile("^(.*?)-shp(.?)-(.*?)-(.*?)-(.*?)\\.7z\\.001");
}
