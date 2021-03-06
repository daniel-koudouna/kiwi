package com.proxy.kiwi.utils;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Dynamic<T> implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private T value;
    transient private List<Consumer<T>> singleCallbacks;
    transient private List<BiConsumer<T, T>> doubleCallbacks;

    public Dynamic(T value) {
        this.value = value;
        singleCallbacks = new LinkedList<>();
        doubleCallbacks = new LinkedList<>();
    }

    public void onChange(Consumer<T> callback) {
        regen();
        singleCallbacks.add(callback);
    }

    public void onChange(BiConsumer<T, T> callback) {
        regen();
        doubleCallbacks.add(callback);
    }

    public void regen() {
        if (singleCallbacks == null) {
            singleCallbacks = new LinkedList<>();
        }
        if (doubleCallbacks == null) {
            doubleCallbacks = new LinkedList<>();
        }
    }

    public void update(T value) {
        T prev = this.value;
        this.value = value;
        regen();
        singleCallbacks.forEach(c -> c.accept(value));
        doubleCallbacks.forEach(c -> c.accept(prev, value));
    }

    public T get() {
        return value;
    }
}
