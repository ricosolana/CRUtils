package com.crazicrafter1.crutils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class WeightedRandomContainer<K> {

    private HashMap<K, Integer> weights;
    private int weight;

    public WeightedRandomContainer() {
        this.weights = new HashMap<>();
    }

    public WeightedRandomContainer(Map<K, Integer> weights) {
        this.weights = new HashMap<>(weights);
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
        int rand = ProbabilityUtil.randomRange(0, weight - 1);

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
        weights.put(key, weight);
        this.weight += weight;
    }

    /**
     * Remove a weighted item
     * @param key the key
     * @throws NullPointerException If the key does not exist
     */
    public void remove(K key) {
        int w = Objects.requireNonNull(weights.remove(key));
        this.weight -= w;
    }

    public Map<K, Integer> getMap() {
        return weights;
    }

    @Override
    public String toString() {
        return weights.toString();
    }
}
