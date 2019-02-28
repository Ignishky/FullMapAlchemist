package fr.ignishky.fma.preparator.downloader;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.ignishky.fma.preparator.downloader.model.Contents;
import fr.ignishky.fma.preparator.downloader.model.Contents.Content;
import fr.ignishky.fma.preparator.downloader.model.Releases.Release;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static fr.ignishky.fma.preparator.downloader.utils.Constants.PATTERN_7ZIP_FILE;
import static java.util.Arrays.asList;

@Slf4j
public class ContentDownloader implements Function<Release, Stream<Content>> {

    private static final List<String> ALLOWED = asList("mn");
    private static final List<String> COUNTRIES = asList("bel", "lux", "nld");

    private final HttpClient client;
    private final String token;

    @Inject
    public ContentDownloader(HttpClient client, @Named("token") String token) {
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Content> apply(Release release) {
        String contentUrl = release.getLocation() + "?label=shpd";
        log.info("Get content shpd from {} ({})", release.getVersion(), contentUrl);

        HttpGet get = new HttpGet(contentUrl);
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return new Gson().fromJson(IOUtils.toString(response, "UTF-8"), Contents.class).getContent().stream()
                    .filter(content -> {
                        Matcher matcher = PATTERN_7ZIP_FILE.matcher(content.getName());
                        return matcher.matches() && ALLOWED.contains(matcher.group(3)) && COUNTRIES.contains(matcher.group(4));
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while processing " + release.getVersion(), e);
        }
    }
}
