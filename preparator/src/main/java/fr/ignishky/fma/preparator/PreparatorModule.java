package fr.ignishky.fma.preparator;

import com.google.inject.AbstractModule;
import org.apache.http.client.HttpClient;

import java.io.File;

import static com.google.inject.name.Names.named;
import static fr.ignishky.fma.preparator.utils.Constants.OUTPUT_FOLDER;
import static fr.ignishky.fma.preparator.utils.Constants.TOKEN;
import static fr.ignishky.fma.preparator.utils.Constants.VERSION;
import static org.apache.http.impl.NoConnectionReuseStrategy.INSTANCE;
import static org.apache.http.impl.client.HttpClientBuilder.create;

public class PreparatorModule extends AbstractModule {

    private final File outputFolder;
    private final String token;
    private final String version;

    PreparatorModule(String outputFolder, String token, String version) {
        this.outputFolder = new File(outputFolder);
        this.outputFolder.mkdirs();
        this.token = token;
        this.version = version;
    }

    @Override
    protected void configure() {
        bind(HttpClient.class).toInstance(create()
                .setMaxConnPerRoute(10)
                .setConnectionReuseStrategy(INSTANCE)
                .build());
        bind(File.class).annotatedWith(named(OUTPUT_FOLDER)).toInstance(outputFolder);
        bindConstant().annotatedWith(named(TOKEN)).to(token);
        bindConstant().annotatedWith(named(VERSION)).to(version);
    }
}
