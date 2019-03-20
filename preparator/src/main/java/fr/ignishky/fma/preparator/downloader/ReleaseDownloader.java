package fr.ignishky.fma.preparator.downloader;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.preparator.downloader.model.Products.Product;
import fr.ignishky.fma.preparator.downloader.model.Releases;
import fr.ignishky.fma.preparator.downloader.model.Releases.Release;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.ignishky.fma.preparator.downloader.utils.Constants.TOKEN;
import static fr.ignishky.fma.preparator.downloader.utils.Constants.VERSION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class ReleaseDownloader implements Function<Product, Stream<Release>> {

    private final String version;
    private final HttpClient client;
    private final String token;

    @Inject
    ReleaseDownloader(@Named(VERSION) String version, HttpClient client, @Named(TOKEN) String token) {
        this.version = version;
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Release> apply(Product product) {
        String releaseUrl = product.getLocation() + "/releases";
        String name = product.getName();
        log.info("Get release {} from {} ({})", version, name, releaseUrl);

        HttpGet get = new HttpGet(releaseUrl);
        get.addHeader(AUTHORIZATION, token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return new Gson().fromJson(IOUtils.toString(response, UTF_8), Releases.class).getContent().stream() //
                    .filter(release -> version.equals(release.getVersion()));

        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while downloading release " + name, e);
        }
    }
}
