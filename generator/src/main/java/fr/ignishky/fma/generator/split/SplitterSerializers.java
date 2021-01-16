package fr.ignishky.fma.generator.split;

import com.vividsolutions.jts.geom.Envelope;
import crosby.binary.osmosis.OsmosisSerializer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.ignishky.fma.generator.utils.Constants.OUTPUT_FOLDER;
import static java.util.stream.Collectors.toList;

class SplitterSerializers {

    private final String splitterFolder;
    private final SplitAreas areas = new SplitAreas();
    private final Map<String, Sink> sinkByArea = new HashMap<>();

    @Inject
    SplitterSerializers(@Named(OUTPUT_FOLDER) File outputFolder) {
        splitterFolder = Path.of(outputFolder.getPath(), "splitter").toString();
    }

    List<String> getAreas(Envelope envelope) {
        return areas.files(envelope).stream().map(this::getSerializer).collect(toList());
    }

    Sink getSink(String area) {
        return sinkByArea.get(area);
    }

    Sink getSink(double x, double y) {
        return sinkByArea.get(areas.file(x, y));
    }

    private String getSerializer(String filename) {
        if (!sinkByArea.containsKey(filename)) {
            File file = Path.of(splitterFolder, filename + ".osm.pbf").toFile();
            file.getParentFile().mkdirs();
            try {
                BlockOutputStream os = new BlockOutputStream(new FileOutputStream(file));
                os.setCompress("none");
                sinkByArea.put(filename, new OsmosisSerializer(os));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return filename;
    }
}
