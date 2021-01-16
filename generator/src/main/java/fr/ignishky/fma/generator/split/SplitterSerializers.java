package fr.ignishky.fma.generator.split;

import com.github.davidmoten.geo.LatLong;
import com.vividsolutions.jts.geom.Envelope;
import crosby.binary.osmosis.OsmosisSerializer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
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

import static fr.ignishky.fma.generator.helper.Geohash.decodeGeohash;
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

    List<String> getAreas(EntityContainer entityContainer) {
        return areas.files(envelope(entityContainer)).stream().map(this::getSerializer).collect(toList());
    }

    Sink getSink(String area) {
        return sinkByArea.get(area);
    }

    private static Envelope envelope(EntityContainer entityContainer) {
        Envelope env = new Envelope();
        Entity entity = entityContainer.getEntity();
        switch (entity.getType()) {
            case Node:
                LatLong geohash = decodeGeohash(entity.getId());
                env.expandToInclude(geohash.getLon(), geohash.getLat());
                break;
            case Way:
                for (WayNode wn : ((Way) entity).getWayNodes()) {
                    geohash = decodeGeohash(wn.getNodeId());
                    env.expandToInclude(geohash.getLon(), geohash.getLat());
                }
                break;
            case Relation:
                for (RelationMember wn : ((Relation) entity).getMembers()) {
                    geohash = decodeGeohash(wn.getMemberId());
                    env.expandToInclude(geohash.getLon(), geohash.getLat());
                }
                break;
            default:
                throw new IllegalArgumentException("Could not generate envelop for " + entity.getType());
        }
        return env;
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
