package fr.ignishky.fma.preparator.downloader;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.preparator.downloader.model.Contents.Content;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static fr.ignishky.fma.preparator.downloader.utils.Constants.OUTPUT_FOLDER;
import static fr.ignishky.fma.preparator.downloader.utils.Constants.TOKEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class ArchiveDownloader implements Function<Content, File> {

    private final File outputFolder;
    private final HttpClient client;
    private final String token;

    @Inject
    ArchiveDownloader(@Named(OUTPUT_FOLDER) File outputFolder, HttpClient client, @Named(TOKEN) String token) {
        this.outputFolder = outputFolder;
        this.client = client;
        this.token = token;
    }

    public File apply(Content content) {
        String name = content.getName();
        File downloaded = new File(outputFolder, name);

        String archiveUrl = content.getLocation() + "/download-url";
        HttpGet get = new HttpGet(archiveUrl);
        get.addHeader(AUTHORIZATION, token);

        log.info("Get redirect URL for {} ({})", name, archiveUrl);
        try (InputStream redirect = client.execute(get).getEntity().getContent()) {

            String redirectUrl = new Gson().fromJson(IOUtils.toString(redirect, UTF_8), Redirect.class).getUrl();

            log.info("Downloading {} to {}", redirectUrl, downloaded.getAbsolutePath());
            try (InputStream archive = client.execute(new HttpGet(redirectUrl)).getEntity().getContent()) {
                copyInputStreamToFile(archive, downloaded);
            }
            return downloaded;

        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while downloading archive " + name, e);
        }
    }

    @Value
    private static class Redirect {
        String url;
    }
}
