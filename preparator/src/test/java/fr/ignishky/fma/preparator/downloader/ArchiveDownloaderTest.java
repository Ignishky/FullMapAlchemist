package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Contents;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchiveDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private ArchiveDownloader archiveDownloader;

    @BeforeEach
    public void setUp() throws Exception {

        HttpResponse redirectResponse = mock(HttpResponse.class);
        when(redirectResponse.getEntity()).thenReturn(new StringEntity("{\"url\":\"redirectUrl\"}"));

        HttpResponse archiveResponse = mock(HttpResponse.class);
        when(archiveResponse.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/downloader/eur2018_09-shpd-mn-lux-ax.7z.001").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(redirectResponse, archiveResponse);

        archiveDownloader = new ArchiveDownloader(new File("target"), client, "validToken");
    }

    @Test
    public void should_download_archive_from_content() {
        File file = archiveDownloader.apply(new Contents.Content("content.7z.001", "loc"));

        assertThat(file.getPath()).isEqualTo("target/content.7z.001");
    }
}
