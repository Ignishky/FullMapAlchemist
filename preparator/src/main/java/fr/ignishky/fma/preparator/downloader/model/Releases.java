package fr.ignishky.fma.preparator.downloader.model;

import lombok.Value;

import java.util.List;

@Value
public class Releases {

    List<Release> content;

    @Value
    public static class Release {

        String version;
        String location;
    }
}
