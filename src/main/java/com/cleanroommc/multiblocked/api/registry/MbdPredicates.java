package com.cleanroommc.multiblocked.api.registry;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateAnyCapability;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateBlocks;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateStates;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import java.util.Map;

public class MbdPredicates {
    public static final Map<String, Class<? extends SimplePredicate>> PREDICATE_REGISTRY = Maps.newHashMap();

    public static void registerPredicate(Class<? extends SimplePredicate> predicate) {
        try {
            PREDICATE_REGISTRY.put(predicate.newInstance().type, predicate);
        } catch (InstantiationException | IllegalAccessException e) {
            Multiblocked.LOGGER.error("Registered predicates require a parameterless constructor");
        }
    }

    public static Class<? extends SimplePredicate> getPredicate(String type) {
        return PREDICATE_REGISTRY.get(type.toLowerCase());
    }

    public static SimplePredicate createPredicate(String type, JsonObject json) {
        if (type.equals("any")) return SimplePredicate.ANY;
        if (type.equals("air")) return SimplePredicate.AIR;
        Class<? extends SimplePredicate> clazz = getPredicate(type);
        if (clazz != null) {
            try {
                SimplePredicate simplePredicate = clazz.newInstance();
                simplePredicate.fromJson(Multiblocked.GSON, json);
                simplePredicate.buildPredicate();
                return simplePredicate;
            } catch (Exception ignored) { }
        }
        return null;
    }

    public static void registerPredicates() {
        registerPredicate(PredicateComponent.class);
        registerPredicate(PredicateStates.class);
        registerPredicate(PredicateBlocks.class);
        registerPredicate(PredicateAnyCapability.class);
    }
}
