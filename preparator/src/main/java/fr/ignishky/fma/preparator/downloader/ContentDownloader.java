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
import static fr.ignishky.fma.preparator.downloader.utils.Constants.TOKEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class ContentDownloader implements Function<Release, Stream<Content>> {

    private static final List<String> ALLOWED = singletonList("mn");
    private static final List<String> COUNTRIES = asList("bel", "lux", "nld");

    private final HttpClient client;
    private final String token;

    @Inject
    ContentDownloader(HttpClient client, @Named(TOKEN) String token) {
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Content> apply(Release release) {
        String contentUrl = release.getLocation() + "?label=shpd";
        log.info("Get content shpd from {} ({})", release.getVersion(), contentUrl);

        HttpGet get = new HttpGet(contentUrl);
        get.addHeader(AUTHORIZATION, token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return new Gson().fromJson(IOUtils.toString(response, UTF_8), Contents.class).getContent().stream()
                    .filter(content -> {
                        Matcher matcher = PATTERN_7ZIP_FILE.matcher(content.getName());
                        return matcher.matches() && ALLOWED.contains(matcher.group(3)) && COUNTRIES.contains(matcher.group(4));
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Something goes wrong while downloading content " + release.getVersion(), e);
        }
    }
}
