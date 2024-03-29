package com.cleanroommc.multiblocked.util;

import java.util.function.Consumer;

public interface ISearch<T> {
    default boolean isManualInterrupt() {return false;}
    void search(String word, Consumer<T> find);
}
