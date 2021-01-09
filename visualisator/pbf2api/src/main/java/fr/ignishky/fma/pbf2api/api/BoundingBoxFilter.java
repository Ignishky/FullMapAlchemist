package fr.ignishky.fma.pbf2api.api;

import fr.ignishky.fma.pbf2api.split.SplitFile;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class BoundingBoxFilter {

    private final Set<Long> writtenNodes = new HashSet<>();
    private final Set<Long> writtenWays = new HashSet<>();

    public List<EntityContainer> filter(SplitFile splitFile, BoundingBox bbox) {
        List<EntityContainer> containers = addNodes(splitFile, bbox);
        containers.addAll(addWays(splitFile));
        containers.addAll(addRelations(splitFile));
        return containers;
    }

    private List<EntityContainer> addNodes(SplitFile splitFile, BoundingBox bbox) {
        return splitFile.getNodes(bbox).stream().map(this::writeNode).collect(toList());
    }

    private EntityContainer writeNode(Node node) {
        writtenNodes.add(node.getId());
        return new NodeContainer(node);
    }

    private List<EntityContainer> addWays(SplitFile splitFile) {
        List<EntityContainer> containers = new ArrayList<>();
        for (Way way : splitFile.getWays()) {
            List<Long> wayNodeIds = way.getWayNodes().stream().map(WayNode::getNodeId).collect(toList());
            if (wayNodeIds.stream().anyMatch(writtenNodes::contains)) {
                writtenWays.add(way.getId());
                containers.addAll(writeWay(splitFile, way));
            }
        }
        return containers;
    }

    private List<EntityContainer> writeWay(SplitFile entities, Way way) {
        List<EntityContainer> containers = way.getWayNodes().stream()
                .map(WayNode::getNodeId)
                .filter(id -> !writtenNodes.contains(id))
                .map(id -> writeNode(entities.getNodeById(id)))
                .collect(toList());
        containers.add(new WayContainer(way));
        return containers;
    }

    private List<EntityContainer> addRelations(SplitFile entities) {
        List<EntityContainer> containers = new ArrayList<>();
        for (Iterator<Relation> iterator = entities.getRelations(); iterator.hasNext();) {
            Relation next = iterator.next();
            List<Long> wayMembers = next.getMembers().stream().filter(rm -> rm.getMemberType() == Way).map(RelationMember::getMemberId).collect(toList());
            List<Long> nodeMembers = next.getMembers().stream().filter(rm -> rm.getMemberType() == Node).map(RelationMember::getMemberId).collect(toList());
            if (wayMembers.stream().anyMatch(writtenWays::contains) || nodeMembers.stream().anyMatch(writtenNodes::contains)) {
                containers.add(new RelationContainer(next));
            }
        }
        return containers;
    }
}
