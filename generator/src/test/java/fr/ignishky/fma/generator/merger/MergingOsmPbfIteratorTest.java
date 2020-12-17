package fr.ignishky.fma.generator.merger;

import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Iterators.forArray;
import static fr.ignishky.fma.generator.utils.CollectionUtils.streamIterator;
import static java.util.Collections.emptyIterator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MergingOsmPbfIteratorTest {

    @Test
    void should_merge_with_empty_iterator() {
        assertThat(new MergingOsmPbfIterator(emptyIterator(), List.of(node(1)).iterator()).hasNext()).isTrue();
        assertThat(new MergingOsmPbfIterator(emptyIterator(), emptyIterator()).hasNext()).isFalse();
    }

    @Test
    void should_merge_headers() {
        Tag tag1 = new Tag("k1", "v1");
        Tag tag2 = new Tag("k2", "v2");

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(forArray(header(tag1)), forArray(header(tag2)));

        assertThat(merge.hasNext()).isTrue();
        assertThat(merge.next().getEntity().getTags()).containsOnly(tag1, tag2);
        assertThat(merge.hasNext()).isFalse();
    }

    @Test
    void should_keep_one_header_and_feed_nodes() {
        EntityContainer header = header(new Tag("k1", "v1"));
        EntityContainer node = node(123L);

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(forArray(header), forArray(node));

        assertThat(merge.hasNext()).isTrue();
        assertThat(merge.next()).isEqualTo(header);
        assertThat(merge.hasNext()).isTrue();
        assertThat(merge.next()).isEqualTo(node);
        assertThat(merge.hasNext()).isFalse();
    }

    @Test
    void should_keep_other_header_and_feed_nodes() {
        EntityContainer node = node(123L);
        EntityContainer header = header(new Tag("k1", "v1"));

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(forArray(node), forArray(header));

        assertThat(merge.hasNext()).isTrue();
        assertThat(merge.next()).isEqualTo(header);
        assertThat(merge.hasNext()).isTrue();
        assertThat(merge.next()).isEqualTo(node);
        assertThat(merge.hasNext()).isFalse();
    }

    @Test
    void should_order_nodes_by_id() {
        EntityContainer node1 = node(1L);
        EntityContainer node2 = node(2L);
        EntityContainer node3 = node(3L);
        EntityContainer node5 = node(5L);
        EntityContainer node6 = node(6L);

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(forArray(node1, node3, node5), forArray(node2, node6));

        assertThat(streamIterator(merge)).containsExactly(node1, node2, node3, node5, node6);
    }

    @Test
    void should_order_nodes_before_ways() {
        EntityContainer node1 = node(1L);
        EntityContainer node2 = node(2L);
        EntityContainer way3 = way(3L);
        EntityContainer way5 = way(5L);
        EntityContainer node6 = node(6L);

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(forArray(node1, way3, way5), forArray(node2, node6));

        assertThat(streamIterator(merge)).containsExactly(node1, node2, node6, way3, way5);
    }

    @Test
    void should_order_ways_before_relations() {
        EntityContainer node1 = node(1L);
        EntityContainer node2 = node(2L);
        EntityContainer way3 = way(3L);
        EntityContainer relation4 = relation(4);
        EntityContainer way5 = way(5L);
        EntityContainer relation6 = relation(6);

        Iterator<EntityContainer> merge = new MergingOsmPbfIterator(forArray(node1, way3, way5), forArray(node2, relation4, relation6));

        assertThat(streamIterator(merge)).containsExactly(node1, node2, way3, way5, relation4, relation6);
    }

    @Test
    void should_merge_more_than_2_iterators() {
        EntityContainer node1 = node(1);
        EntityContainer node2 = node(2);
        EntityContainer node3 = node(3);
        EntityContainer relation5 = relation(5);
        EntityContainer way6 = way(6);
        EntityContainer relation10 = relation(10);
        EntityContainer way13 = way(13);
        EntityContainer node15 = node(15);
        EntityContainer relation21 = relation(21);
        EntityContainer relation22 = relation(22);
        Iterator<EntityContainer> it1 = forArray(node1, way13, relation21);
        Iterator<EntityContainer> it2 = forArray(node3, relation5, relation22);
        Iterator<EntityContainer> it3 = forArray(node2, node15);
        Iterator<EntityContainer> it4 = forArray(way6, relation10);

        Iterator<EntityContainer> merge = MergingOsmPbfIterator.init(List.of(it1, it2, it3, it4));

        assertThat(streamIterator(merge)).containsExactly(node1, node2, node3, node15, way6, way13, relation5, relation10, relation21, relation22);
    }

    @Test
    void should_keep_only_one_node_when_duplicate_ids() {

        EntityContainer node1 = node(1);
        EntityContainer node3 = node(3);
        EntityContainer node4 = node(4);
        Iterator<EntityContainer> merge = MergingOsmPbfIterator.init(List.of(forArray(node1, node3), forArray(node3, node4)));

        assertThat(streamIterator(merge)).containsExactly(node1, node3, node4);
    }

    private static EntityContainer header(Tag... tags) {
        return container(bound(newArrayList(tags)));
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
        return new CommonEntityData(id, 0, (TimestampContainer) null, null, 0L, new ArrayList<>(1));
    }

    private static Bound bound(Collection<Tag> tags) {
        return new Bound("") {
            @Override
            public int getVersion() {
                return 20170200;
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
