package com.cleanroommc.multiblocked.api.crafttweaker.interfaces;

import com.cleanroommc.multiblocked.client.particle.AbstractParticle;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IWorld;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

@ZenRegister
@ZenClass("mods.multiblocked.client.Particle")
public interface ICTParticle {

    AbstractParticle getInner();

    @ZenGetter
    default IWorld world() {
        return CraftTweakerMC.getIWorld(getInner().getWorld());
    }

    @ZenGetter
    default double x() {
        return getInner().getX();
    }

    @ZenGetter
    default double y() {
        return getInner().getY();
    }

    @ZenGetter
    default double z() {
        return getInner().getZ();
    }

    @ZenSetter
    default void x(double x) {
       getInner().setX(x);
    }

    @ZenSetter
    default void y(double y){
       getInner().setY(y);
    }

    @ZenSetter
    default void z(double z) {
        getInner().setZ(z);
    }

    @ZenMethod
    void setPosition(double x, double y, double z);

    @ZenMethod
    void setLife(int life);

    @ZenMethod
    int getLife();

    @ZenGetter
    @ZenMethod
    boolean isBackLayer();

    @ZenGetter
    @ZenMethod
    boolean isAddBlend();

    @ZenMethod
    void setBackLayer(boolean isBackLayer);

    @ZenMethod
    void setAddBlend(boolean isBackLayer);

    @ZenMethod
    void kill();

    @ZenMethod
    boolean isAlive();

    @ZenMethod
    void setRenderRange(int renderRange);

    @ZenMethod
    int getRenderRange();

    @ZenMethod
    void setImmortal();

    @ZenMethod
    default void create() {
        getInner().addParticle();
    }

}
