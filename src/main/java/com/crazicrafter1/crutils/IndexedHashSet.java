package com.crazicrafter1.crutils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class IndexedHashSet<T> extends LinkedHashSet<T> {

    public IndexedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public IndexedHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public IndexedHashSet() {
    }

    public IndexedHashSet(Collection<? extends T> c) {
        super(c);
    }

    public int indexOf(Object element) {
        Iterator<T> it = iterator();
        for (int i=0; i < size(); i++) {
            if (it.hasNext()) {
                if (element.equals(it.next()))
                    return i;
            }
        }
        return -1;
    }

}
