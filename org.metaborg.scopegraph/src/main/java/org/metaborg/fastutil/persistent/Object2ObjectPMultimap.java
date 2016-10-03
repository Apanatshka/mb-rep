package org.metaborg.fastutil.persistent;

import java.util.Set;

import org.pcollections.PSet;

public interface Object2ObjectPMultimap<K, V> {

    PSet<V> get(K key);

    Object2ObjectPMultimap<K,V> put(K key, V value);

    Set<K> keySet();

}