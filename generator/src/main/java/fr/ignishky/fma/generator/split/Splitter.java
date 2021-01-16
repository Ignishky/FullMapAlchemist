package fr.ignishky.fma.generator.split;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

import java.nio.file.Path;
import java.util.List;

import static fr.ignishky.fma.generator.helper.Geohash.decodeGeohash;
import java.util.HashSet;
import java.util.Set;

public class Splitter {

    private final SplitterSerializers serializers;

    @Inject
    public Splitter(SplitterSerializers serializers) {
        this.serializers = serializers;
    }

    public void split(Path file) {
        Multimap<Long, String> areasByNode = ArrayListMultimap.create();
        Multimap<Long, String> areasByWay = ArrayListMultimap.create();

        preparePass(file, areasByWay, areasByNode);
        processPass(file, areasByWay, areasByNode);
    }

    private void preparePass(Path fileToSplit, Multimap<Long, String> areasByWay, Multimap<Long, String> areasByNode) {
        read(fileToSplit, new SplitterSink("prepare pass") {

            @Override
            public void process(NodeContainer node) {
                // Don't process node in the prepare pass
            }

            @Override
            public void process(WayContainer way) {
                // List all wayNodes for all the way's areas.
                Way entity = way.getEntity();
                for (String area : serializers.getAreas(way)) {
                    areasByWay.put(entity.getId(), area);
                    for (WayNode node : entity.getWayNodes()) {
                        areasByNode.put(node.getNodeId(), area);
                    }
                }
            }

            @Override
            public void process(RelationContainer relation) {
                // List all node members for all the relation's areas.
                for (String area : serializers.getAreas(relation)) {
                    for (RelationMember member : relation.getEntity().getMembers()) {
                        if (member.getMemberType() == EntityType.Node) {
                            areasByNode.put(member.getMemberId(), area);
                        } else if (member.getMemberType() == EntityType.Way) {
                            areasByWay.put(member.getMemberId(), area);
                        }
                    }
                }
            }
        });
    }

    private void processPass(Path file, Multimap<Long, String> areasByWay, Multimap<Long, String> areasByNode) {
        Set<Sink> sinks = new HashSet<>();

        read(file, new SplitterSink("process pass") {

            @Override
            public void process(NodeContainer node) {
                Node entity = node.getEntity();
                if (areasByNode.containsKey(entity.getId())) {
                    // the mode is needed by a way or relation from another area
                    for (String area : areasByNode.get(entity.getId())) {
                        process(node, area);
                    }
                } else {
                    process(node, serializers.getAreas(node).get(0));
                }
            }

            @Override
            public void process(WayContainer way) {
                for (String area : areasByWay.get(way.getEntity().getId())) {
                    process(way, area);
                }
            }

            @Override
            public void process(RelationContainer relation) {
                for (String area : serializers.getAreas(relation)) {
                    process(relation, area);
                }
            }

            private void process(EntityContainer entityContainer, String area) {
                Sink sink = serializers.getSink(area);
                sink.process(entityContainer);
                sinks.add(sink);
            }
        });

        for (Sink sink : sinks) {
            sink.complete();
        }
    }

    private static void read(Path file, Sink sink) {
        PbfReader reader = new PbfReader(file.toFile(), 2);
        reader.setSink(sink);
        reader.run();
    }
}
