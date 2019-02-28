package fr.ignishky.fma.preparator.downloader;

import fr.ignishky.fma.preparator.downloader.model.Families;
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

class FamiliesDownloaderTest {

    private final HttpClient client = mock(HttpClient.class);

    private FamiliesDownloader familiesDownloader;

    @BeforeEach
    public void setUp() throws Exception {

        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/downloader/families.json").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(response);

        familiesDownloader = new FamiliesDownloader(client, "validToken");
    }

    @Test
    public void should_only_download_mn_sp_2dcm_families() {

        Stream<Families.Family> families = familiesDownloader.get();

        assertThat(families).containsOnly(new Families.Family("MN", "https://api.test/families/300"));
    }
}
