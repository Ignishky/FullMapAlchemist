package fr.ignishky.fma.generator.utils;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollectionUtils {

    private CollectionUtils() {}

    public static <T> Stream<T> stream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), false);
    }

    public static <T> Stream<T> streamIterator(Iterator<T> it) {
        return stream(() -> it);
    }
}
