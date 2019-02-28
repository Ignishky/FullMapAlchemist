package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Families;
import fr.ignishky.fma.preparator.downloader.model.Products;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductsDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private ProductsDownloader productsDownloader;

    @BeforeEach
    public void setUp() throws Exception {

        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/downloader/products.json").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(response);

        productsDownloader = new ProductsDownloader(client, "validToken");
    }

    @Test
    public void should_download_product_from_family() {

        Stream<Products.Product> productStream = productsDownloader.apply(new Families.Family("abb", "loc"));

        assertThat(productStream).containsOnly(new Products.Product("EUR", "https://api.test/products/230"));
    }
}
