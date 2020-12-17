package fr.ignishky.fma.generator.split;

import com.vividsolutions.jts.geom.Envelope;
import crosby.binary.osmosis.OsmosisSerializer;
import org.openstreetmap.osmosis.core.misc.v0_6.NullWriter;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.ignishky.fma.generator.utils.Constants.OUTPUT_FOLDER;
import static java.util.stream.Collectors.toList;

class SplitterSerializers {

    private final SplitAreas areas = new SplitAreas();
    private final List<Sink> serializers = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();
    private final String parent;

    @Inject
    SplitterSerializers(@Named(OUTPUT_FOLDER) File outputFolder) {
        parent = Path.of(outputFolder.getPath(), "splitter").toString();
    }

    Sink serializer(int j) {
        if (j == -1) {
            return new NullWriter();
        }
        return serializers.get(j);
    }

    Sink serializer(double x, double y) {
        return serializer(serializerIndex(x, y));
    }

    List<Integer> serializer(Envelope env) {
        return areas.file(env).stream().map(this::serializerIndex).filter(i -> i >= 0).collect(toList());
    }

    private int serializerIndex(double x, double y) {
        String file = areas.file(x, y);
        if (file == null) {
            return -1;
        }
        return serializerIndex(file);
    }

    private int serializerIndex(String filename) {
        try {
            if (index.containsKey(filename)) {
                return index.get(filename);
            }
            File file = Path.of(parent, filename, filename + ".osm.pbf").toFile();
            file.getParentFile().mkdirs();
            BlockOutputStream os = new BlockOutputStream(new FileOutputStream(file));
            os.setCompress("none");
            serializers.add(new OsmosisSerializer(os));
            int last = serializers.size() - 1;
            index.put(filename, last);
            return last;

        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}