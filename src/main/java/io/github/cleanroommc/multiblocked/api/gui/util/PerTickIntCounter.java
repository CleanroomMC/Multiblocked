package io.github.cleanroommc.multiblocked.api.gui.util;

import net.minecraft.world.World;

public class PerTickIntCounter {

    private final int defaultValue;

    private long lastUpdatedWorldTime;

    private int currentValue;

    public PerTickIntCounter(int defaultValue) {
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
    }

    private void checkValueState(World world) {
        long currentWorldTime = world.getTotalWorldTime();
        if (currentWorldTime != lastUpdatedWorldTime) {
            this.lastUpdatedWorldTime = currentWorldTime;
            this.currentValue = defaultValue;
        }
    }

    public int get(World world) {
        checkValueState(world);
        return currentValue;
    }

    public void increment(World world, int value) {
        checkValueState(world);
        this.currentValue += value;
    }
}
