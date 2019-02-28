package fr.ignishky.fma.generator.merger;

import crosby.binary.osmosis.OsmosisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static com.google.common.io.Files.copy;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Slf4j
public class OsmMerger {

    public void merge(List<String> inputFiles, Path outputFile) {

        List<File> files = inputFiles.stream().map(File::new).filter(File::exists).collect(toList());

        if (files.size() == 1) {
            try {
                copy(files.get(0), outputFile.toFile());
            } catch (IOException e) {
                throw new IllegalStateException(format("Unable to copy file '%s' to '%s'", inputFiles.get(0), outputFile), e);
            }

        } else if (files.size() > 1) {
            PbfIterator[] iterators = inputFiles.stream().map(PbfIterator::new).collect(toList()).toArray(new PbfIterator[] {});
            Iterator<EntityContainer> merge = MergingOsmPbfIterator.merge(iterators);

            log.info("Writing merged data to {}", outputFile);
            try (FileOutputStream output = new FileOutputStream(outputFile.toFile())) {
                OsmosisSerializer serializer = new OsmosisSerializer(new BlockOutputStream(output));
                while (merge.hasNext()) {
                    serializer.process(merge.next());
                }
                serializer.complete();
            } catch (IOException e) {
                throw new IllegalStateException(format("Unable to open file '%s'", outputFile), e);
            }
        }
    }
}
