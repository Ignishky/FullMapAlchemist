package fr.ignishky.fma.preparator.downloader;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.preparator.downloader.model.Families;
import fr.ignishky.fma.preparator.downloader.model.Families.Family;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

@Slf4j
public class FamiliesDownloader {

    private static final String FAMILIES_URL = "https://api.tomtom.com/mcapi/families";
    private static final List<String> ALLOWED = singletonList("MN");

    private final HttpClient client;
    private final String token;

    @Inject
    public FamiliesDownloader(HttpClient client, @Named("token") String token) {
        this.client = client;
        this.token = token;
    }

    public Stream<Family> get() {
        log.info("Get all families ({})", FAMILIES_URL);

        HttpGet get = new HttpGet(FAMILIES_URL);
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return new Gson().fromJson(IOUtils.toString(response, UTF_8), Families.class).getContent().stream() //
                    .filter(f -> ALLOWED.contains(f.getAbbreviation()));

        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while processing families", e);
        }
    }
}
