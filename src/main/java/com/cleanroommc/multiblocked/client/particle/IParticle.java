package com.cleanroommc.multiblocked.client.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IParticle {

    default IParticleHandler getGLHandler() {
        return IParticleHandler.DEFAULT_FX_HANDLER;
    }

    World getWorld();

    void setWorld(World world);

    double getX();

    double getY();

    double getZ();

    void setPosition(double x, double y, double z);

    default void setX(double x) {
        setPosition(x, getY(), getZ());
    }

    default void setY(double y){
        setPosition(getX(), y, getZ());
    }

    default void setZ(double z){
        setPosition(getX(), getY(), z);
    }

    boolean isBackLayer();

    boolean isAddBlend();

    void setBackLayer(boolean isBackLayer);

    void setAddBlend(boolean isBackLayer);

    void kill();

    boolean isAlive();

    void onUpdate();

    boolean shouldRendered(Entity entityIn, float partialTicks);

    @SideOnly(Side.CLIENT)
    void renderParticle(BufferBuilder buffer, float partialTicks);

    default void addParticle() {
        ParticleManager.INSTANCE.addEffect(this);
    }
}
