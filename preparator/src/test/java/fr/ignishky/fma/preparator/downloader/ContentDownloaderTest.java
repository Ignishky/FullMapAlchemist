package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Contents.Content;
import fr.ignishky.fma.preparator.downloader.model.Releases.Release;
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

class ContentDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private final ContentDownloader contentDownloader = new ContentDownloader(client, TOKEN);

    @Test
    void should_throws_IllegalStateException_when_client_throws_IOException() throws Exception {

        when(client.execute(any(HttpGet.class))).thenThrow(new IOException("Content Test Exception"));

        assertThrows(IllegalStateException.class, () -> contentDownloader.apply(new Release("ver", "loc")));
    }

    @Test
    void should_download_all_mn_content_from_release() throws Exception {

        HttpResponse contentResponse = mock(HttpResponse.class);
        when(contentResponse.getEntity()).thenReturn(new FileEntity(new File("src/test/resources/downloader/contents.json")));

        when(client.execute(any(HttpGet.class))).thenReturn(contentResponse);

        Stream<Content> content = contentDownloader.apply(new Release("2016.09", "loc1"));

        assertThat(content).containsOnly(new Content("eur2016_09-shpd-mn-lux-lux.7z.001", "https://api.test/contents/792"));
    }
}
