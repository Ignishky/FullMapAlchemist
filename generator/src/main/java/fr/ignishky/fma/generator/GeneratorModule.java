package fr.ignishky.fma.generator;

import com.google.inject.AbstractModule;

import java.io.File;

import static com.google.inject.name.Names.named;

public class GeneratorModule extends AbstractModule {

    private final File inputFolder;
    private final File outputFolder;

    public GeneratorModule(String inputFolder, String outputFolder) {
        this.inputFolder = new File(inputFolder);
        this.outputFolder = new File(outputFolder);
        this.outputFolder.mkdirs();
    }

    @Override
    protected void configure() {
        bind(File.class).annotatedWith(named("inputFolder")).toInstance(inputFolder);
        bind(File.class).annotatedWith(named("outputFolder")).toInstance(outputFolder);
    }
}
