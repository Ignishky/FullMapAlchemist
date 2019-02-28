package fr.ignishky.fma.preparator;

import com.google.inject.Inject;
import fr.ignishky.fma.preparator.downloader.ArchiveDownloader;
import fr.ignishky.fma.preparator.downloader.ContentDownloader;
import fr.ignishky.fma.preparator.downloader.FamiliesDownloader;
import fr.ignishky.fma.preparator.downloader.ProductsDownloader;
import fr.ignishky.fma.preparator.downloader.ReleaseDownloader;
import fr.ignishky.fma.preparator.extractor.ShapefileExtractor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.inject.Guice.createInjector;

public class Main {

    private final FamiliesDownloader familiesDownloader;
    private final ProductsDownloader productsDownloader;
    private final ReleaseDownloader releaseDownloader;
    private final ContentDownloader contentDownloader;
    private final ArchiveDownloader archiveDownloader;
    private final ShapefileExtractor shapefileExtractor;

    @Inject
    public Main(FamiliesDownloader familiesDownloader, ProductsDownloader productsDownloader, ReleaseDownloader releaseDownloader,
                ContentDownloader contentDownloader, ArchiveDownloader archiveDownloader, ShapefileExtractor shapefileExtractor) {
        this.familiesDownloader = familiesDownloader;
        this.productsDownloader = productsDownloader;
        this.releaseDownloader = releaseDownloader;
        this.contentDownloader = contentDownloader;
        this.archiveDownloader = archiveDownloader;
        this.shapefileExtractor = shapefileExtractor;
    }

    public static void main(String[] args) {
        checkArgument(args.length == 3, "Usage : Main <outputFolder> <token> <version>");

        createInjector(new PreparatorModule(args[0], args[1], args[2])).getInstance(Main.class).run();
    }

    /* package */ void run() {
        familiesDownloader.get()
                .flatMap(productsDownloader)
                .flatMap(releaseDownloader)
                .flatMap(contentDownloader)
                .map(archiveDownloader)
                .forEach(shapefileExtractor::decompress);
    }
}
