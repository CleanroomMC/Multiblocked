package com.cleanroommc.multiblocked.api.registry;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateAnyCapability;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateBlocks;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateCustomAny;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateMetaTileEntity;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateStates;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.Loader;

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

    private static Class<? extends SimplePredicate> getPredicate(String type) {
        return PREDICATE_REGISTRY.get(type.toLowerCase());
    }

    public static SimplePredicate createPredicate(String type) {
        return createPredicate(type, null);
    }

    public static SimplePredicate createPredicate(String type, JsonObject json) {
        if (type.equals("any")) return SimplePredicate.ANY;
        if (type.equals("air")) return SimplePredicate.AIR;
        Class<? extends SimplePredicate> clazz = getPredicate(type);
        if (clazz != null) {
            try {
                SimplePredicate simplePredicate = clazz.newInstance();
                if (json == null) return simplePredicate.buildPredicate();
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
        registerPredicate(PredicateCustomAny.class);
        if (Loader.isModLoaded(Multiblocked.MODID_GTCE) && isCEu()){
            registerPredicate(PredicateMetaTileEntity.class);
        }
    }

    public static boolean isCEu() {
        String version = Loader.instance().getIndexedModList().get("gregtech").getVersion();
        return Integer.parseInt(version.split("\\.")[0]) >= 2;
    }
}
