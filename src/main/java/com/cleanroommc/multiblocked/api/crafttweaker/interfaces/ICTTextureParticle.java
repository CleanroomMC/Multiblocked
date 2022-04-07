package com.cleanroommc.multiblocked.api.crafttweaker.interfaces;

import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.multiblocked.client.TextureParticle")
public interface ICTTextureParticle extends ICTParticle{

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
    void setColor(int color);

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
