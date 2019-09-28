package fr.ignishky.fma.generator.utils;

import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public final class ShapefileUtils {

    public static void assertTag(Collection<? extends Tag> tags, String key, String value) {
        assertThat(tags.stream().filter(tag -> key.equals(tag.getKey())).map(Tag::getValue)).containsOnly(value);
    }
}
