package fr.ignishky.fma.preparator.downloader.model;

import lombok.Value;

import java.util.List;

@Value
public class Families {

    List<Family> content;

    @Value
    public static class Family {
        String abbreviation;
        String location;
    }
}
