package com.cleanroommc.multiblocked.client.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IParticle {

    default IParticleHandler getGLHandler() {
        return IParticleHandler.DEFAULT_FX_HANDLER;
    }

    boolean isBackLayer();

    void kill();

    boolean isAlive();

    void onUpdate();

    boolean shouldRendered(Entity entityIn, float partialTicks);

    @SideOnly(Side.CLIENT)
    void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationXZ, float rotationZ, float rotationYZ, float rotationXY);
}
