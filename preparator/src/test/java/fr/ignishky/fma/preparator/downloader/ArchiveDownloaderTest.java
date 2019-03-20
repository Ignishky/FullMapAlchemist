package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Contents.Content;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static fr.ignishky.fma.preparator.utils.Constants.TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchiveDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private final ArchiveDownloader archiveDownloader = new ArchiveDownloader(new File("target"), client, TOKEN);

    @Test
    void should_throws_IllegalStateException_when_client_throws_IOException() throws Exception {

        when(client.execute(any(HttpGet.class))).thenThrow(new IOException("Archive Test Exception"));

        assertThrows(IllegalStateException.class, () -> archiveDownloader.apply(new Content("fake.7z.001", "loc")));
    }

    @Test
    void should_download_archive_from_content() throws Exception {

        HttpResponse redirectResponse = mock(HttpResponse.class);
        when(redirectResponse.getEntity()).thenReturn(new StringEntity("{\"url\":\"redirectUrl\"}"));

        HttpResponse archiveResponse = mock(HttpResponse.class);
        when(archiveResponse.getEntity()).thenReturn(new FileEntity(new File("src/test/resources/downloader/eur2018_09-shpd-mn-lux-ax.7z.001")));

        when(client.execute(any(HttpGet.class))).thenReturn(redirectResponse, archiveResponse);

        File file = archiveDownloader.apply(new Content("content.7z.001", "loc"));

        assertThat(file.getPath()).isEqualTo("target/content.7z.001");
    }
}
