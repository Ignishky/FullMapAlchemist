package fr.ignishky.fma.preparator.downloader.utils;

import java.util.regex.Pattern;

public class Constants {

    private Constants() {}

    // Ex : eur2018_09-shpd-mn-lux-ax.7z.001
    public static final Pattern PATTERN_7ZIP_FILE = Pattern.compile("^(.*?)-shp(.?)-(.*?)-(.*?)-(.*?)\\.7z\\.001");
}
