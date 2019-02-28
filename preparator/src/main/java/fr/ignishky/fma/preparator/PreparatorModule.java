package fr.ignishky.fma.preparator;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.http.client.HttpClient;

import java.io.File;

import static org.apache.http.impl.NoConnectionReuseStrategy.INSTANCE;
import static org.apache.http.impl.client.HttpClientBuilder.create;

public class PreparatorModule extends AbstractModule {

    private final File outputFolder;
    private final String token;
    private final String version;

    public PreparatorModule(String outputFolder, String token, String version) {
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
        bind(File.class).annotatedWith(Names.named("outputFolder")).toInstance(outputFolder);
        bindConstant().annotatedWith(Names.named("token")).to(token);
        bindConstant().annotatedWith(Names.named("version")).to(version);
    }
}
