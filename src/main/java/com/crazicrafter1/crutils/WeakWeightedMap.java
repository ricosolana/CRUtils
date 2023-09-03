package com.crazicrafter1.crutils;

import javax.annotation.Nullable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

@Deprecated
// TODO
//  currently broken as I have no clue how to use weak references and reference queue
public class WeakWeightedMap<K> {

    private ReferenceQueue<K> queue = new ReferenceQueue<>();

    private final Map<K, Integer> weights = new HashMap<>();
    private int weight;

    public WeakWeightedMap() {
    }

    public WeakWeightedMap(Map<K, Integer> weights) {
        for (Map.Entry<K, Integer> entry : weights.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    //public static <K> WeightedRandomContainer<K> cumulative(LinkedHashMap<K, Integer> cumulative) {
    //    WeightedRandomContainer<K> container = new WeightedRandomContainer<>();
    //    int prevSum = 0;
    //    for (Map.Entry<K, Integer> entry : cumulative.entrySet()) {
    //        int weight = entry.getValue() - prevSum;
    //        container.add(entry.getKey(), weight);
    //        prevSum = entry.getValue();
    //    }
    //    return container;
    //}

    public int getWeight() {
        return weight;
    }

    /**
     * Retrieve a weighted random object
     * @return the random object
     */
    public K get() {
        int rand = RandomUtil.randomRange(0, weight - 1);

        for (Map.Entry<K, Integer> entry : weights.entrySet()) {
            int w = entry.getValue();
            if (rand < w)
                return entry.getKey();
            rand -= w;
        }

        throw new RuntimeException("get() called on empty WeakWeightedMap");
    }

    /**
     * Add a weighted object
     * @param key the key
     * @param weight the weight
     */
    public void add(K key, int weight) {
        remove(key);
        weights.put(key, weight);
        this.weight += weight;

        //new WeakReference<>(key, queue)
    }

    /**
     * Remove a weighted item
     * @param key the key
     */
    public void remove(K key) {
        Integer w = weights.remove(key);
        if (w != null)
            this.weight -= w;
    }

    public Map<K, Integer> getMap() {
        return Collections.unmodifiableMap(weights);
    }

    @Override
    public String toString() {
        return weights.toString();
    }
}
