package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Families.Family;
import fr.ignishky.fma.preparator.downloader.model.Products.Product;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static fr.ignishky.fma.preparator.downloader.utils.Constants.TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductsDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private final ProductsDownloader productsDownloader = new ProductsDownloader(client, TOKEN);

    @Test
    void should_throws_IllegalStateException_when_client_throws_IOException() throws Exception {

        when(client.execute(any(HttpGet.class))).thenThrow(new IOException("Products Test Exception"));

        assertThrows(IllegalStateException.class, () -> productsDownloader.apply(new Family("abb", "loc")));
    }

    @Test
    void should_download_product_from_family() throws Exception {

        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(new FileEntity(new File("src/test/resources/downloader/products.json")));

        when(client.execute(any(HttpGet.class))).thenReturn(response);

        Stream<Product> productStream = productsDownloader.apply(new Family("abb", "loc"));

        assertThat(productStream).containsOnly(new Product("EUR", "https://api.test/products/230"));
    }
}
