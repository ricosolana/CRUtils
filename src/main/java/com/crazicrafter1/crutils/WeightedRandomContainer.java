package com.crazicrafter1.crutils;

import javax.annotation.Nullable;
import java.util.*;

public class WeightedRandomContainer<K> {

    private final HashMap<K, Integer> weights;
    private int weight;

    public WeightedRandomContainer() {
        this.weights = new HashMap<>();
    }

    public WeightedRandomContainer(Map<K, Integer> weights) {
        this.weights = new HashMap<>();
        for (Map.Entry<K, Integer> entry : weights.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public static <K> WeightedRandomContainer<K> cumulative(LinkedHashMap<K, Integer> cumulative) {
        WeightedRandomContainer<K> container = new WeightedRandomContainer<>();
        int prevSum = 0;
        for (Map.Entry<K, Integer> entry : cumulative.entrySet()) {
            int weight = entry.getValue() - prevSum;
            container.add(entry.getKey(), weight);
            prevSum = entry.getValue();
        }
        return container;
    }

    public int getWeight() {
        return weight;
    }

    /**
     * Retrieve a weighted random object
     * @return the random object
     */
    public K getRandom() {
        int rand = RandomUtil.randomRange(0, weight - 1);

        for (Map.Entry<K, Integer> entry : weights.entrySet()) {
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
        return weights.get(key);
    }

    /**
     * Add a weighted object
     * @param key the key
     * @param weight the weight
     */
    public void add(K key, int weight) {
        //if (weights.containsKey(key)) throw new RuntimeException("Cannot add new")
        remove(key);
        weights.put(key, weight);
        this.weight += weight;
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
