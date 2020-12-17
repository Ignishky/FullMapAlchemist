package fr.ignishky.fma.preparator;

import com.github.paweladamski.httpclientmock.HttpClientMock;
import com.github.paweladamski.httpclientmock.action.Action;
import com.google.inject.AbstractModule;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.util.Modules.override;
import static fr.ignishky.fma.preparator.downloader.FamiliesDownloader.FAMILIES_URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.list;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class MainIT {

    private static final String OUTPUT_FOLDER = "target/preparator/main";
    private static final PreparatorModule PREPARATOR_MODULE = new PreparatorModule(OUTPUT_FOLDER, "tokenIT", "2016.09");

    private final Main main = createInjector(override(PREPARATOR_MODULE).with(new DownloadModuleIT())).getInstance(Main.class);

    @Test
    void should_chain_downloader() throws Exception {

        main.run();

        assertThat(list(Path.of(OUTPUT_FOLDER))).containsOnly(Path.of(OUTPUT_FOLDER, "lux"), Path.of(OUTPUT_FOLDER, "eur2016_09-shpd-mn-lux-lux.7z.001"));
        assertThat(list(Path.of(OUTPUT_FOLDER , "lux"))).containsOnly(Path.of(OUTPUT_FOLDER, "lux", "lux"));
        assertThat(list(Path.of(OUTPUT_FOLDER , "lux", "lux"))).containsOnly(Path.of(OUTPUT_FOLDER, "lux", "lux", "lux______________a0.shp"));
    }

    private static class DownloadModuleIT extends AbstractModule {
        @Override
        protected void configure() {
            HttpClientMock client = new HttpClientMock();

            try {
                client.onGet(FAMILIES_URL).doReturnJSON(getFile("families.json"));
                client.onGet("https://api.test/families/300/products").doReturnJSON(getFile("products.json"));
                client.onGet("https://api.test/products/230/releases").doReturnJSON(getFile("releases.json"));
                client.onGet("https://api.test/releases/248?label=shpd").doReturnJSON(getFile("contents.json"));
                client.onGet("https://api.test/contents/792/download-url").doReturnJSON("{\"url\":\"https://redirect.url\"}");

                Action file = r -> {
                    HttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), SC_OK, "ok");
                    response.setEntity(new FileEntity(new File("src/test/resources/downloader/eur2018_09-shpd-mn-lux-ax.7z.001")));
                    return response;
                };

                client.onGet("https://redirect.url").doAction(file);

            } catch (IOException e) {
                fail(e.getMessage());
            }

            bind(HttpClient.class).toInstance(client);
        }

        private static String getFile(String name) throws IOException {
            return readFileToString(new File("src/test/resources/downloader/" + name), UTF_8);
        }
    }
}
