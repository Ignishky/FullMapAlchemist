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

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

@Slf4j
public class ArchiveDownloader implements Function<Content, File> {

    private final File outputFolder;
    private final HttpClient client;
    private final String token;

    @Inject
    public ArchiveDownloader(@Named("outputFolder") File outputFolder, HttpClient client, @Named("token") String token) {
        this.outputFolder = outputFolder;
        this.client = client;
        this.token = token;
    }

    public File apply(Content content) {
        File downloaded = new File(outputFolder, content.getName());

        String archiveUrl = content.getLocation() + "/download-url";
        HttpGet get = new HttpGet(archiveUrl);
        get.addHeader("Authorization", token);

        log.info("Get redirect URL for {} ({})", content.getName(), archiveUrl);
        try (InputStream redirect = client.execute(get).getEntity().getContent()) {

            String redirectUrl = new Gson().fromJson(IOUtils.toString(redirect, "UTF-8"), Redirect.class).getUrl();

            log.info("Downloading {} to {}", redirectUrl, downloaded.getAbsolutePath());
            try (InputStream archive = client.execute(new HttpGet(redirectUrl)).getEntity().getContent()) {
                copyInputStreamToFile(archive, downloaded);
            }
            return downloaded;

        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while processing " + content.getName(), e);
        }
    }

    @Value
    private class Redirect {
        String url;
    }
}
