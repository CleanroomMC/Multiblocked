package com.cleanroommc.multiblocked.api.crafttweaker.interfaces;

import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.util.Position3f;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.multiblocked.client.TextureParticle")
public interface ICTTextureParticle extends ICTParticle{

    @SideOnly(Side.CLIENT)
    CommonParticle getInner();

    @ZenMethod
    default boolean isShader() {
        return getInner() instanceof ShaderTextureParticle;
    }

    @ZenMethod
    void setSize(float particleWidth, float particleHeight);

    @ZenMethod
    void setMotionless(boolean motionless);

    @ZenMethod
    void setMotion(double xSpeedIn, double ySpeedIn, double zSpeedIn);

    @ZenMethod
    default void setMotion(Position3f vec) {
        setMotion(vec.getX(), vec.getY(), vec.getZ());
    }

    @ZenMethod
    void setColor(int color);

    @ZenMethod
    default void setColor(int alpha, int red, int green, int blue) {
        setColor((alpha << 24) | (red << 16) | (green << 8) | blue);
    }

    @ZenMethod
    void setScale(float scale);

    @ZenMethod
    void setGravity(float gravity);

    @ZenMethod
    void setTexturesCount(int texturesCount);

    @ZenMethod
    void setTexturesIndex(int particleTextureIndexX, int particleTextureIndexY);

    @ZenMethod
    default void setTexture(String texture) {
        getInner().setTexture(new ResourceLocation(texture));
    }

    @ZenMethod
    void setLightingMap(int block, int sky);

}
