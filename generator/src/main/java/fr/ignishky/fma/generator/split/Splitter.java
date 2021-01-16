package fr.ignishky.fma.generator.split;

import com.github.davidmoten.geo.LatLong;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Envelope;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

import java.nio.file.Path;
import java.util.List;

import static fr.ignishky.fma.generator.helper.Geohash.decodeGeohash;

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
                List<String> areas = serializers.getAreas(envelope(way));
                if(areas.size() > 1) {
                    for (WayNode node : way.getEntity().getWayNodes()) {
                        areas.forEach(area -> areasByNode.put(node.getNodeId(), area));
                    }
                }
            }

            @Override
            public void process(RelationContainer relation) {
                List<String> areas = serializers.getAreas(envelope(relation));
                if(areas.size() > 1) {
                    for (RelationMember member : relation.getEntity().getMembers()) {
                        if (member.getMemberType() == EntityType.Node) {
                            areas.forEach(area -> areasByNode.put(member.getMemberId(), area));
                        } else if (member.getMemberType() == EntityType.Way) {
                            areas.forEach(area -> areasByWay.put(member.getMemberId(), area));
                        }
                    }
                }
            }
        });
    }

    private void processPass(Path file, Multimap<Long, String> areasByWay, Multimap<Long, String> areasByNode) {
        read(file, new SplitterSink("process pass") {

            @Override
            public void process(NodeContainer node) {
                Node entity = node.getEntity();
                if (areasByNode.containsKey(entity.getId())) {
                    areasByNode.get(entity.getId()).forEach(area -> serializers.getSink(area).process(node));
                } else {
                    serializers.getSink(entity.getLongitude(), entity.getLatitude()).process(node);
                }
            }

            @Override
            public void process(WayContainer way) {
                List<String> areas = serializers.getAreas(envelope(way));
                long wayId = way.getEntity().getId();
                if (areasByWay.containsKey(wayId)) {
                    areasByWay.get(wayId).forEach(area -> serializers.getSink(area).process(way));
                } else {
                    areas.forEach(area -> serializers.getSink(area).process(way));
                }
            }

            @Override
            public void process(RelationContainer relation) {
                serializers.getAreas(envelope(relation)).forEach(area -> serializers.getSink(area).process(relation));
            }
        });
    }

    private static void read(Path file, Sink sink) {
        PbfReader reader = new PbfReader(file.toFile(), 2);
        reader.setSink(sink);
        reader.run();
    }

    private static Envelope envelope(WayContainer way) {
        Envelope env = new Envelope();
        for (WayNode wn : way.getEntity().getWayNodes()) {
            LatLong geohash = decodeGeohash(wn.getNodeId());
            env.expandToInclude(geohash.getLon(), geohash.getLat());
        }
        return env;
    }

    private static Envelope envelope(RelationContainer relation) {
        Envelope env = new Envelope();
        for (RelationMember wn : relation.getEntity().getMembers()) {
            LatLong geohash = decodeGeohash(wn.getMemberId());
            env.expandToInclude(geohash.getLon(), geohash.getLat());
        }
        return env;
    }
}
