package fr.ignishky.fma.generator.merger;

import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static fr.ignishky.fma.generator.utils.CollectionUtils.streamIterator;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MergingOsmPbfIteratorTest {

    @Test
    public void should_merge_with_empty_iterator() {
        List<EntityContainer> newArrayList = newArrayList();

        assertThat(new MergingOsmPbfIterator(newArrayList.iterator(), newArrayList(node(1)).iterator()).hasNext()).isTrue();
        assertThat(new MergingOsmPbfIterator(newArrayList.iterator(), newArrayList.iterator()).hasNext()).isFalse();
    }

    @Test
    public void should_merge_headers() {
        Iterator<EntityContainer> it1 = newArrayList(header(20170200, new Tag("k1", "v1"))).iterator();
        Iterator<EntityContainer> it2 = newArrayList(header(20170200, new Tag("k2", "v2"))).iterator();

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(it1, it2);

        assertThat(toMap(merge.next().getEntity().getTags())).containsOnly(entry("k1", "v1"), entry("k2", "v2"));
        assertThat(merge.hasNext()).isFalse();
    }

    @Test
    public void should_keep_one_header_and_feed_nodes() {
        Iterator<EntityContainer> it1 = newArrayList(header(20170200, new Tag("k1", "v1"))).iterator();
        Iterator<EntityContainer> it2 = newArrayList(node(123L)).iterator();

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(it1, it2);

        assertThat(merge.next().getEntity().getType()).isEqualTo(EntityType.Bound);
        assertThat(merge.next().getEntity().getType()).isEqualTo(EntityType.Node);
        assertThat(merge.hasNext()).isFalse();
    }

    @Test
    public void should_keep_other_header_and_feed_nodes() {
        Iterator<EntityContainer> it1 = newArrayList(node(123L)).iterator();
        Iterator<EntityContainer> it2 = newArrayList(header(20170200, new Tag("k1", "v1"))).iterator();

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(it1, it2);

        assertThat(merge.next().getEntity().getType()).isEqualTo(EntityType.Bound);
        assertThat(merge.next().getEntity().getType()).isEqualTo(EntityType.Node);
        assertThat(merge.hasNext()).isFalse();
    }

    @Test
    public void should_order_nodes_by_id() {
        Iterator<EntityContainer> it1 = newArrayList(node(1L), node(3L), node(5L)).iterator();
        Iterator<EntityContainer> it2 = newArrayList(node(2L), node(6L)).iterator();

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(it1, it2);

        assertThat(streamIterator(merge).map(ec -> ec.getEntity().getId()).collect(toList())).containsExactly(1L, 2L, 3L, 5L, 6L);
    }

    @Test
    public void should_order_nodes_before_ways() {
        Iterator<EntityContainer> it1 = newArrayList(node(1L), way(3L), way(5L)).iterator();
        Iterator<EntityContainer> it2 = newArrayList(node(2L), node(6L)).iterator();

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(it1, it2);

        assertThat(streamIterator(merge).map(ec -> ec.getEntity().getId()).collect(toList())).containsExactly(1L, 2L, 6L, 3L, 5L);
    }

    @Test
    public void should_order_ways_before_relations() {
        Iterator<EntityContainer> it1 = newArrayList(node(1L), way(3L), way(5L)).iterator();
        Iterator<EntityContainer> it2 = newArrayList(node(2L), relation(4), relation(6)).iterator();

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(it1, it2);

        assertThat(streamIterator(merge).map(ec -> ec.getEntity().getId()).collect(toList())).containsExactly(1L, 2L, 3L, 5L, 4L, 6L);
    }

    @Test
    public void should_merge_more_than_2_iterators() {
        Iterator<EntityContainer> it1 = newArrayList(node(1), way(13), relation(21)).iterator();
        Iterator<EntityContainer> it2 = newArrayList(node(3), relation(5), relation(22)).iterator();
        Iterator<EntityContainer> it3 = newArrayList(node(2), node(15), relation(24)).iterator();
        Iterator<EntityContainer> it4 = newArrayList(node(4), way(6), relation(20)).iterator();

        Iterator<EntityContainer> merge = MergingOsmPbfIterator.merge(it1, it2, it3, it4);

        assertThat(streamIterator(merge).map(ec -> ec.getEntity().getId()).collect(toList()))
                .containsExactly(1L, 2L, 3L, 4L, 15L, 6L, 13L, 5L, 20L, 21L, 22L, 24L);
    }

    @Test
    public void should_keep_only_one_node_when_duplicate_ids() {
        Iterator<EntityContainer> it1 = newArrayList(node(1), node(3)).iterator();
        Iterator<EntityContainer> it2 = newArrayList(node(3), node(4)).iterator();
        Iterator<EntityContainer> it3 = newArrayList(node(3), node(4)).iterator();

        Iterator<EntityContainer> merge = MergingOsmPbfIterator.merge(it1, it2, it3);

        assertThat(streamIterator(merge).map(ec -> ec.getEntity().getId()).collect(toList())).containsExactly(1L, 3L, 4L);
    }

    private static Map<String, String> toMap(Collection<Tag> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
    }

    private static EntityContainer header(int version, Tag... tags) {
        return container(bound(version, newArrayList(tags)));
    }

    private static EntityContainer container(Entity entity) {
        EntityContainer container = mock(EntityContainer.class);
        when(container.getEntity()).thenReturn(entity);
        return container;
    }

    private static EntityContainer node(long id) {
        return container(new Node(data(id), 0.0, 0.0));
    }

    private static EntityContainer way(long id) {
        return container(new Way(data(id)));
    }

    private static EntityContainer relation(long id) {
        return container(new Relation(data(id)));
    }

    private static CommonEntityData data(long id) {
        return new CommonEntityData(id, 0, (Date) null, null, 0L);
    }

    private static Bound bound(int version, Collection<Tag> tags) {
        return new Bound("") {
            @Override
            public int getVersion() {
                return version;
            }

            @Override
            public OsmUser getUser() {
                return null;
            }

            @Override
            public Collection<Tag> getTags() {
                return tags;
            }
        };
    }
}
