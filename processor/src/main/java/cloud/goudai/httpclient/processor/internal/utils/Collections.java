package cloud.goudai.httpclient.processor.internal.utils;

import java.util.*;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public final class Collections {

    private Collections() {
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... elements) {
        Set<T> set = new HashSet<>(elements.length);
        java.util.Collections.addAll(set, elements);
        return set;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(Collection<T> collection, T... elements) {
        Set<T> set = new HashSet<>(collection.size() + elements.length);
        java.util.Collections.addAll(set, elements);
        return set;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(Collection<T> collection, Collection<T>... elements) {
        Set<T> set = new HashSet<>(collection);

        for (Collection<T> element : elements) {
            set.addAll(element);
        }

        return set;
    }

    public static <T> T first(Collection<T> collection) {
        return collection.iterator().next();
    }

    public static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> List<T> join(List<T> a, List<T> b) {
        List<T> result = new ArrayList<>(a.size() + b.size());

        result.addAll(a);
        result.addAll(b);

        return result;
    }
}