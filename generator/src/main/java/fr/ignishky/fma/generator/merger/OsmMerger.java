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

    public void merge(List<String> inputs, Path outputFile) {

        if (inputs.size() == 1) {
            try {
                log.info("Copy {} to {}", inputs.get(0), outputFile);
                copy(new File(inputs.get(0)), outputFile.toFile());

            } catch (IOException e) {
                throw new IllegalStateException(format("Unable to copy file '%s' to '%s'", inputs.get(0), outputFile), e);
            }

        } else if (inputs.size() > 1) {
            log.info("Merging {} to {}", inputs, outputFile);
            List<Iterator<EntityContainer>> iterators = inputs.stream().map(PbfIterator::new).collect(toList());
            Iterator<EntityContainer> merge = MergingOsmPbfIterator.init(iterators);

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
