package fr.ignishky.fma.preparator.downloader.model;

import lombok.Value;

import java.util.List;

@Value
public class Products {

    List<Product> content;

    @Value
    public static class Product {

        String name;
        String location;
    }
}
