package com.crazicrafter1.crutils;

import javax.annotation.Nullable;
import java.util.*;

public class WeightedRandomContainer<K> {

    private final Map<K, Integer> weights;
    private int totalWeight;

    //public WeightedRandomContainer() {
    //    this.weights = new HashMap<>();
    //}

    public WeightedRandomContainer(Map<K, Integer> weights) {
        this.weights = weights;
        totalWeight = this.weights.values().stream().mapToInt(d -> d).sum();
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

    public int getTotalWeight() {
        return this.totalWeight;
    }

    /**
     * Retrieve a weighted random object
     * @return the random object
     */
    public K getRandom() {
        int rand = RandomUtil.randomRange(0, this.totalWeight - 1);

        for (Map.Entry<K, Integer> entry : this.weights.entrySet()) {
            int w = entry.getValue();
            if (rand < w)
                return entry.getKey();
            rand -= w;
        }

        throw new RuntimeException("Should not reach this point");
    }

    /**
     * Retrieve a weight by key
     * @param key the key
     * @return the weight
     */
    @Nullable
    public Integer get(K key) {
        return this.weights.get(key);
    }

    /**
     * Add a weighted object
     * @param key the key
     * @param weight the weight
     */
    public void add(K key, int weight) {
        //if (weights.containsKey(key)) throw new RuntimeException("Cannot add new")
        this.remove(key);
        this.weights.put(key, weight);
        this.totalWeight += weight;
    }

    /**
     * Remove a weighted item
     * @param key the key
     */
    public void remove(K key) {
        Integer w = this.weights.remove(key);
        if (w != null)
            this.totalWeight -= w;
    }

    public Map<K, Integer> getMap() {
        return Collections.unmodifiableMap(this.weights);
    }

    @Override
    public String toString() {
        return weights.toString();
    }
}
