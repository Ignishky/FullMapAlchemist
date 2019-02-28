package fr.ignishky.fma.generator.writer;

import crosby.binary.osmosis.OsmosisSerializer;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;

@Slf4j
public class PbfSink implements Sink {

    @Delegate
    private final OsmosisSerializer serializer;

    public PbfSink(Path output) {
        try {
            BlockOutputStream os = new BlockOutputStream(new FileOutputStream(output.toFile()));
            os.setCompress("none");
            serializer = new OsmosisSerializer(os);
        } catch (FileNotFoundException e) {
            log.error("Unable to create output directory {}", output, e);
            throw new IllegalStateException("Unable to create output directory {}" + output, e);
        }
    }
}
