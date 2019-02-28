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

@Slf4j
public class ReleaseDownloader implements Function<Product, Stream<Release>> {

    private final String version;
    private final HttpClient client;
    private final String token;

    @Inject
    public ReleaseDownloader(@Named("version") String version, HttpClient client, @Named("token") String token) {
        this.version = version;
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Release> apply(Product product) {
        String releaseUrl = product.getLocation() + "/releases";
        log.info("Get release {} from {} ({})", version, product.getName(), releaseUrl);

        HttpGet get = new HttpGet(releaseUrl);
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return new Gson().fromJson(IOUtils.toString(response, "UTF-8"), Releases.class).getContent().stream() //
                    .filter(release -> version.equals(release.getVersion()));

        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while processing " + product.getName(), e);
        }
    }
}
