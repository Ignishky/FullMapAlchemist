package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Families.Family;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static fr.ignishky.fma.preparator.utils.Constants.TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FamiliesDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private final FamiliesDownloader familiesDownloader = new FamiliesDownloader(client, TOKEN);

    @Test
    void should_throws_IllegalStateException_when_client_throws_IOException() throws Exception {

        when(client.execute(any(HttpGet.class))).thenThrow(new IOException("Families Test Exception"));

        assertThrows(IllegalStateException.class, familiesDownloader::get);
    }

    @Test
    void should_download_families() throws Exception {

        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(new FileEntity(new File("src/test/resources/downloader/families.json")));

        when(client.execute(any(HttpGet.class))).thenReturn(response);

        Stream<Family> families = familiesDownloader.get();

        assertThat(families).containsOnly(new Family("MN", "https://api.test/families/300"));
    }
}
