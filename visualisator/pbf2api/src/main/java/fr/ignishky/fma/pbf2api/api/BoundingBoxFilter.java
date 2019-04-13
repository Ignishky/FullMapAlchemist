package fr.ignishky.fma.pbf2api.api;

import com.google.common.collect.Lists;
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class BoundingBoxFilter {

    private final Set<Long> requiredNodes = new HashSet<>(100);
    private final Set<Long> writtenNodes = new HashSet<>(100);
    private final Set<Long> writtenWays = new HashSet<>(100);

    public List<EntityContainer> filter(SplitFile entities, BoundingBox bbox) {
        List<EntityContainer> containers = addNodes(entities, bbox);
        containers.addAll(addWays(entities));
        containers.addAll(addRelations(entities));
        return containers;
    }

    private List<EntityContainer> addRelations(SplitFile entities) {
        List<EntityContainer> containers = Lists.newArrayList();
        for (Iterator<Relation> iterator = entities.getRelations(); iterator.hasNext();) {
            Relation next = iterator.next();
            List<Long> wayMembers = next.getMembers().stream().filter(rm -> rm.getMemberType() == Way).map(RelationMember::getMemberId).collect(toList());
            List<Long> nodeMembers = next.getMembers().stream().filter(rm -> rm.getMemberType() == Node).map(RelationMember::getMemberId).collect(toList());
            if (wayMembers.stream().anyMatch(writtenWays::contains) || nodeMembers.stream().anyMatch(requiredNodes::contains)) {
                RelationContainer entityContainer = new RelationContainer(next);
                containers.add(entityContainer);
            }
        }
        return containers;
    }

    private List<EntityContainer> addNodes(SplitFile entities, BoundingBox bbox) {
        List<EntityContainer> containers = Lists.newArrayList();
        for (Iterator<Node> iterator = entities.nodesWithin(bbox); iterator.hasNext();) {
            Node node = iterator.next();
            requiredNodes.add(node.getId());
            containers.add(writeNode(node));
        }
        return containers;
    }

    private EntityContainer writeNode(Node node) {
        writtenNodes.add(node.getId());
        return new NodeContainer(node);
    }

    private List<EntityContainer> addWays(SplitFile entities) {
        List<EntityContainer> containers = Lists.newArrayList();
        for (Iterator<Way> iterator = entities.getWays(); iterator.hasNext();) {
            Way way = iterator.next();
            List<Long> wayNodes = way.getWayNodes().stream().map(WayNode::getNodeId).collect(toList());
            if (wayNodes.stream().anyMatch(requiredNodes::contains)) {
                writtenWays.add(way.getId());
                containers.addAll(writeWay(entities, way));
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
}