package com.cleanroommc.multiblocked.api.registry;


import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.renderer.ICustomRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.B3DRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.GTRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.GeoComponentRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.OBJRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.TextureParticleRenderer;
import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.Loader;

import java.util.Map;

public class MbdRenderers {
    public static final Map<String, ICustomRenderer> RENDERER_REGISTRY = Maps.newHashMap();

    public static void registerRenderer(ICustomRenderer renderer) {
        RENDERER_REGISTRY.put(renderer.getType().toLowerCase(), renderer);
    }

    public static ICustomRenderer getRenderer(String type) {
        return RENDERER_REGISTRY.get(type.toLowerCase());
    }

    public static void registerRenderers() {
        registerRenderer(IModelRenderer.INSTANCE);
        registerRenderer(BlockStateRenderer.INSTANCE);
        registerRenderer(B3DRenderer.INSTANCE);
        registerRenderer(OBJRenderer.INSTANCE);
        registerRenderer(TextureParticleRenderer.INSTANCE);
        registerRenderer(GTRenderer.INSTANCE);
        if (Loader.isModLoaded(Multiblocked.MODID_GEO)) {
            registerRenderer(GeoComponentRenderer.INSTANCE);
        }
    }
}
