package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Products.Product;
import fr.ignishky.fma.preparator.downloader.model.Releases.Release;
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

class ReleaseDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private final ReleaseDownloader releaseDownloader = new ReleaseDownloader("2016.09", client, TOKEN);

    @Test
    void should_throws_IllegalStateException_when_client_throws_IOException() throws Exception {

        when(client.execute(any(HttpGet.class))).thenThrow(new IOException("Release Test Exception"));

        assertThrows(IllegalStateException.class, () -> releaseDownloader.apply(new Product("prod", "loc")));
    }

    @Test
    void should_download_only_desired_release_from_product() throws Exception {

        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(new FileEntity(new File("src/test/resources/downloader/releases.json")));

        when(client.execute(any(HttpGet.class))).thenReturn(response);

        Stream<Release> release = releaseDownloader.apply(new Product("prod", "loc"));

        assertThat(release).containsOnly(new Release("2016.09", "https://api.test/releases/248"));
    }
}
