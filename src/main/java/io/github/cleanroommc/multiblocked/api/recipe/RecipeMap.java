package io.github.cleanroommc.multiblocked.api.recipe;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityDetector;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;

/**
 * Created with IntelliJ IDEA.
 */
public class RecipeMap {

    public <T extends CapabilityProxy> boolean hasCapability(CapabilityDetector<T> tCapabilityDetector) {
        return false;
    }
}
