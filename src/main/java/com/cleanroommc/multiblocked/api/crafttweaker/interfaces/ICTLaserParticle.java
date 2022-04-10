package com.cleanroommc.multiblocked.api.crafttweaker.interfaces;

import com.cleanroommc.multiblocked.client.particle.LaserBeamParticle;
import com.cleanroommc.multiblocked.util.Vector3;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.multiblocked.client.LaserParticle")
public interface ICTLaserParticle extends ICTParticle{

    @SideOnly(Side.CLIENT)
    LaserBeamParticle getInner();

    @ZenMethod
    default void setBody(String location) {
        getInner().setBody(new ResourceLocation(location));
    }

    @ZenMethod
    default void setHead(String location) {
        getInner().setHead(new ResourceLocation(location));
    }

    @ZenMethod
    default void setStartPos(double x, double y, double z) {
        getInner().setStartPos(new Vector3(x, y, z));
    }

    @ZenMethod
    default void setEndPos(double x, double y, double z) {
        getInner().setEndPos(new Vector3(x, y, z));
    }

    @ZenMethod
    void setBeamHeight(float beamHeight);

    @ZenMethod
    void setHeadWidth(float headWidth);

    @ZenMethod
    void setAlpha(float alpha);

    @ZenMethod
    void setEmit(float emit);

    @ZenMethod
    void setVerticalMode(boolean verticalMode);

}
