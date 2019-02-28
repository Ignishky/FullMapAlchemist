package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Contents;
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

class ContentDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private ContentDownloader contentDownloader;

    @BeforeEach
    public void setUp() throws Exception {

        HttpResponse contentResponse = mock(HttpResponse.class);
        when(contentResponse.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/downloader/contents.json").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(contentResponse);

        contentDownloader = new ContentDownloader(client, "validToken");
    }

    @Test
    public void should_download_all_mn_content_from_release() {

        Stream<Contents.Content> content = contentDownloader.apply(new Releases.Release("2016.09", "loc1"));

        assertThat(content).containsOnly(new Contents.Content("eur2016_09-shpd-mn-lux-lux.7z.001", "https://api.test/contents/792"));
    }
}
