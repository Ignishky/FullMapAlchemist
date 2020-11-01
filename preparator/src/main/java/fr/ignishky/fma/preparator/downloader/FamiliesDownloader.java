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

import static fr.ignishky.fma.preparator.utils.Constants.TOKEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class FamiliesDownloader {

    public static final String FAMILIES_URL = "https://api.tomtom.com/mcapi/families";
    private static final List<String> ALLOWED = List.of("MN");
    private static final Gson GSON = new Gson();

    private final HttpClient client;
    private final String token;

    @Inject
    FamiliesDownloader(HttpClient client, @Named(TOKEN) String token) {
        this.client = client;
        this.token = token;
    }

    public Stream<Family> get() {
        log.info("Get all families ({})", FAMILIES_URL);

        HttpGet get = new HttpGet(FAMILIES_URL);
        get.addHeader(AUTHORIZATION, token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return GSON.fromJson(IOUtils.toString(response, UTF_8), Families.class).getContent().stream()
                    .filter(family -> ALLOWED.contains(family.getAbbreviation()));

        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while downloading families", e);
        }
    }
}
