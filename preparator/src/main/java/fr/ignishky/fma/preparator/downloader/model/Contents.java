package fr.ignishky.fma.preparator.downloader.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.util.List;

@Value
public class Contents {

    @SerializedName("contents")
    List<Content> content;

    @Value
    public static class Content {

        String name;
        String location;
    }
}
