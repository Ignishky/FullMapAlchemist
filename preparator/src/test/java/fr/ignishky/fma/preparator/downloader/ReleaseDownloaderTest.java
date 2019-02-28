package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Products;
import fr.ignishky.fma.preparator.downloader.model.Releases;
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

class ReleaseDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private ReleaseDownloader releaseDownloader;

    @BeforeEach
    public void setUp() throws Exception {

        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/downloader/releases.json").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(response);

        releaseDownloader = new ReleaseDownloader("2016.09", client, "validToken");
    }

    @Test
    public void should_download_only_desired_release_from_product() {

        Stream<Releases.Release> release = releaseDownloader.apply(new Products.Product("prod1", "loc1"));

        assertThat(release).containsOnly(new Releases.Release("2016.09", "https://api.test/releases/248"));
    }
}
