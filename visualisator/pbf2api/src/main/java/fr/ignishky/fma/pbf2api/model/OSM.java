package fr.ignishky.fma.pbf2api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.xml.common.XmlTimestampFormat;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Data
@With
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class OSM {

    private final String version;
    private final List<Element> elements;

    public static OSM toOSM(List<EntityContainer> entities) {
        List<Element> elements = entities.stream().map(Element::toElement).sorted().collect(toList());
        return new OSM()
                .withVersion("0.6")
                .withElements(elements);
    }

    @Data
    @With
    @NoArgsConstructor(force = true)
    @AllArgsConstructor
    public static class Element implements Comparable<Element> {

        private final String type;
        private final Long id;
        private final Double lat;
        private final Double lon;
        private final String timestamp;
        private final String user;
        private final List<Long> nodes;
        private final Map<String, String> tags;

        public static Element toElement(EntityContainer entityContainer) {
            Entity entity = entityContainer.getEntity();
            return new Element()
                    .withType(entity instanceof Node ? "node" : entity instanceof Way ? "way" : null)
                    .withId(entity.getId())
                    .withLat(entity instanceof Node ? ((Node) entity).getLatitude() : null)
                    .withLon(entity instanceof Node ? ((Node) entity).getLongitude() : null)
                    .withTimestamp(entity.getFormattedTimestamp(new XmlTimestampFormat()))
                    .withUser("Tomtom")
                    .withNodes(entity instanceof Way ? ((Way) entity).getWayNodes().stream().map(WayNode::getNodeId).collect(toList()) : null)
                    .withTags(entity instanceof Way ? ((Way) entity).getTags().stream().collect(toMap(Tag::getKey, Tag::getValue)) : null);
        }

        @Override
        public int compareTo(Element element) {
            return "node".equals(type) ? -1 : 0;
        }
    }
}
