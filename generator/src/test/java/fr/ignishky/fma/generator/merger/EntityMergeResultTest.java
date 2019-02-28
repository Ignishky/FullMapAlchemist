package fr.ignishky.fma.generator.merger;

import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Collection;
import java.util.Date;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntityMergeResultTest {

    @Test
    public void should_merge_tags_on_nodes_with_same_ids() {
        EntityMergeResult result = new EntityMergeResult(node(20170200, new Tag("k1", "v1")), node(20170200, new Tag("k2", "v2")), null, false).merge();

        assertEquals(2, result.getResult().getEntity().getTags().size());
        assertTrue(result.isMerged());
    }

    @Test
    public void should_not_merge_tags_on_nodes_with_different_ids() {
        EntityMergeResult result = new EntityMergeResult(node(20170200, new Tag("k1", "v1")), node(20170201, new Tag("k2", "v2")), null, false).merge();

        assertEquals(1, result.getResult().getEntity().getTags().size());
        assertFalse(result.isMerged());
    }

    private EntityContainer node(int id, Tag... tags) {
        return container(withData(id, newArrayList(tags), new Node(new CommonEntityData(id, id, (Date) null, null, id), 0.0, 0.0)));
    }

    private EntityContainer container(Entity entity) {
        EntityContainer container = mock(EntityContainer.class);
        when(container.getEntity()).thenReturn(entity);
        return container;
    }

    private Entity withData(int version, Collection<Tag> tags, Entity node) {
        node.setId(version);
        node.getTags().addAll(tags);
        return node;
    }
}
