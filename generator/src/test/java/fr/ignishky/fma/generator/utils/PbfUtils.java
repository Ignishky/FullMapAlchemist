package fr.ignishky.fma.generator.utils;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class PbfUtils {

    public static PbfContent read(String pathname) {
        PbfReader reader = new PbfReader(new File(pathname), 1);

        PbfContent pbfContent = new PbfContent(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        Sink sinkImplementation = new Sink() {

            public void process(EntityContainer entityContainer) {

                Entity entity = entityContainer.getEntity();
                if (entity instanceof Node) {
                    pbfContent.getNodes().add((Node) entity);

                } else if (entity instanceof Way) {
                    pbfContent.getWays().add((Way) entity);

                } else if (entity instanceof Relation) {
                    pbfContent.getRelations().add((Relation) entity);
                }
            }

            public void initialize(Map<String, Object> arg0) {
            }

            public void complete() {
            }

            public void release() {
            }
        };

        reader.setSink(sinkImplementation);
        reader.run();
        return pbfContent;
    }
}
