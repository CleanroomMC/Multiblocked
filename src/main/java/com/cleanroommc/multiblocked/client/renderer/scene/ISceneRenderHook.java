package com.cleanroommc.multiblocked.client.renderer.scene;

import net.minecraft.util.BlockRenderLayer;

/**
 * Scene Render State hooks.
 * This is where you decide whether or not this group of pos should be rendered. What other requirements do you have for rendering.
 */
public interface ISceneRenderHook {
    void apply(boolean isTESR, int pass, BlockRenderLayer layer);
}
