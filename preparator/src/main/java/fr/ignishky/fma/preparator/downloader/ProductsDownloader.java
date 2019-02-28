package fr.ignishky.fma.preparator.downloader;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.preparator.downloader.model.Families.Family;
import fr.ignishky.fma.preparator.downloader.model.Products;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

@Slf4j
public class ProductsDownloader implements Function<Family, Stream<Products.Product>> {

    private static final List<String> ALLOWED = singletonList("EUR");

    private final HttpClient client;
    private final String token;

    @Inject
    public ProductsDownloader(HttpClient client, @Named("token") String token) {
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Products.Product> apply(Family family) {
        String productUrl = family.getLocation() + "/products";
        log.info("Get all products from {} ({})", family.getAbbreviation(), productUrl);

        HttpGet get = new HttpGet(productUrl);
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return new Gson().fromJson(IOUtils.toString(response, "UTF-8"), Products.class).getContent().stream()
                    .filter(product -> ALLOWED.contains(product.getName()));

        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while processing " + family.getAbbreviation(), e);
        }
    }
}
