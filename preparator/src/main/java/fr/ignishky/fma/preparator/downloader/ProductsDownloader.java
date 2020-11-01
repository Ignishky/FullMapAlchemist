package fr.ignishky.fma.preparator.downloader;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.preparator.downloader.model.Families.Family;
import fr.ignishky.fma.preparator.downloader.model.Products;
import fr.ignishky.fma.preparator.downloader.model.Products.Product;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.ignishky.fma.preparator.utils.Constants.TOKEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class ProductsDownloader implements Function<Family, Stream<Product>> {

    private static final List<String> ALLOWED = List.of("EUR");
    private static final Gson GSON = new Gson();

    private final HttpClient client;
    private final String token;

    @Inject
    ProductsDownloader(HttpClient client, @Named(TOKEN) String token) {
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Product> apply(Family family) {
        String productUrl = family.getLocation() + "/products";
        String abbreviation = family.getAbbreviation();
        log.info("Get all products from {} ({})", abbreviation, productUrl);

        HttpGet get = new HttpGet(productUrl);
        get.addHeader(AUTHORIZATION, token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return GSON.fromJson(IOUtils.toString(response, UTF_8), Products.class).getContent().stream()
                    .filter(product -> ALLOWED.contains(product.getName()));

        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while downloading product " + abbreviation, e);
        }
    }
}
