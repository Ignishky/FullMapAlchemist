package fr.ignishky.fma.generator.split;

import com.github.davidmoten.geo.LatLong;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Envelope;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static fr.ignishky.fma.generator.helper.Geohash.decodeGeohash;

public class Splitter {

    private final SplitterSerializers kml;

    @Inject
    public Splitter(SplitterSerializers kml) {
        this.kml = kml;
    }

    public void split(Path file) {
        Multimap<Long, Long> wayByRelations = ArrayListMultimap.create();
        Multimap<Long, Integer> borderNodeTargets = ArrayListMultimap.create();
        firstPass(file, wayByRelations, borderNodeTargets);
        finalPass(wayByRelations, borderNodeTargets, file);
    }

    private void firstPass(Path file, Multimap<Long, Long> wayByRelations, Multimap<Long, Integer> borderNodeTargets) {
        read(file, new SplitterSink("first pass") {

            @Override
            public void process(NodeContainer node) {
                // Don't process node on first pass
            }

            @Override
            public void process(WayContainer way) {
                List<Integer> targets = kml.serializer(envelope(way));
                if (targets.size() > 1) {
                    for (WayNode wn : way.getEntity().getWayNodes()) {
                        targets.forEach(target -> borderNodeTargets.put(wn.getNodeId(), target));
                    }
                }
            }

            @Override
            public void process(RelationContainer relation) {
                relation.getEntity().getMembers().forEach(rm -> wayByRelations.put(rm.getMemberId(), relation.getEntity().getId()));
            }
        });
    }

    private void finalPass(Multimap<Long, Long> wayByRelations, Multimap<Long, Integer> borderNodeTargets, Path file) {
        Multimap<Long, Integer> relationTargets = ArrayListMultimap.create();
        read(file, new SplitterSink("final pass") {

            @Override
            public void process(NodeContainer node) {
                long nodeId = node.getEntity().getId();
                if (borderNodeTargets.containsKey(nodeId)) {
                    borderNodeTargets.get(nodeId).forEach(target -> kml.serializer(target).process(node));
                } else {
                    kml.serializer(node.getEntity().getLongitude(), node.getEntity().getLatitude()).process(node);
                }
            }

            @Override
            public void process(WayContainer way) {
                List<Integer> targets = kml.serializer(envelope(way));
                long wayId = way.getEntity().getId();
                if (wayByRelations.containsKey(wayId)) {
                    wayByRelations.get(wayId).forEach(rel -> relationTargets.putAll(rel, targets));
                }
                targets.forEach(target -> kml.serializer(target).process(way));
            }

            @Override
            public void process(RelationContainer relation) {
                Collection<Integer> targets = new HashSet<>(kml.serializer(envelope(relation)));
                targets.addAll(relationTargets.get(relation.getEntity().getId()));
                targets.forEach(target -> kml.serializer(target).process(relation));
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
