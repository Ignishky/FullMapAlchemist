package fr.ignishky.fma.generator;

import com.google.inject.AbstractModule;
import fr.ignishky.fma.generator.utils.Constants;

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
        bind(File.class).annotatedWith(named(Constants.INPUT_FOLDER)).toInstance(inputFolder);
        bind(File.class).annotatedWith(named(Constants.OUTPUT_FOLDER)).toInstance(outputFolder);
    }
}
