package fr.ignishky.fma.preparator;

import com.github.paweladamski.httpclientmock.HttpClientMock;
import com.github.paweladamski.httpclientmock.action.Action;
import com.google.inject.AbstractModule;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.util.Modules.override;
import static java.nio.file.Files.list;
import static java.nio.file.Paths.get;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class MainIT {

    private static final String OUTPUT_FOLDER = "target/preparator";
    private static final PreparatorModule DOWNLOAD_MODULE = new PreparatorModule(OUTPUT_FOLDER, "token", "2016.09");

    private Main main;

    @BeforeEach
    public void setUp() {
        main = createInjector(override(DOWNLOAD_MODULE).with(new DownloadModuleIT())).getInstance(Main.class);
    }

    @Test
    public void should_chain_downloader() throws Exception {

        main.run();

        assertThat(list(get(OUTPUT_FOLDER))).containsOnly(get(OUTPUT_FOLDER, "lux"));
        assertThat(list(get(OUTPUT_FOLDER , "lux"))).containsOnly(get(OUTPUT_FOLDER, "lux", "lux"));
        assertThat(list(get(OUTPUT_FOLDER , "lux", "lux"))).containsOnly
                (get(OUTPUT_FOLDER, "lux", "lux", "lux______________a0.shp"));
    }

    private static class DownloadModuleIT extends AbstractModule {
        @Override
        protected void configure() {
            HttpClientMock client = new HttpClientMock();

            try {
                client.onGet("https://api.tomtom.com/mcapi/families").doReturnJSON(getFile("families.json"));
                client.onGet("https://api.test/families/300/products").doReturnJSON(getFile("products.json"));
                client.onGet("https://api.test/products/230/releases").doReturnJSON(getFile("releases.json"));
                client.onGet("https://api.test/releases/248?label=shpd").doReturnJSON(getFile("contents.json"));
                client.onGet("https://api.test/contents/792/download-url").doReturnJSON("{\"url\":\"https://redirect.url\"}");

                Action file = r -> {
                    BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "ok");
                    try {
                        response.setEntity(new FileEntity(new File(getClass().getResource("/downloader/eur2018_09-shpd-mn-lux-ax.7z.001").toURI())));
                    } catch (URISyntaxException e) {
                        fail(e.getMessage());
                    }
                    return response;
                };

                client.onGet("https://redirect.url").doAction(file);

            } catch (Exception e) {
                fail(e.getMessage());
            }

            bind(HttpClient.class).toInstance(client);
        }

        private String getFile(String name) throws IOException, URISyntaxException {
            return readFileToString(new File(getClass().getResource("/downloader/" + name).toURI()), "UTF-8");
        }
    }
}
