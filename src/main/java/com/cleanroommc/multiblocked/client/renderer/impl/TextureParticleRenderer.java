package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.particle.AbstractParticle;
import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TextureParticleRenderer extends ParticleRenderer{

    protected ResourceLocation texture = new ResourceLocation(Multiblocked.MODID, "start");
    protected boolean isShader = true;
    protected float scale = 16;
    protected int skyLight = 15;
    protected int blockLight = 15;

    @Override
    protected AbstractParticle createParticle(World world, double x, double y, double z) {
        CommonParticle particle =  isShader ? new ShaderTextureParticle(world, x, y, z) : new CommonParticle(world, x, y, z);
        particle.setAddBlend(isAddBlend);
        particle.setBackLayer(isBackLayer);
        particle.setScale(scale);
        particle.setLightingMap(blockLight, skyLight);
        particle.setTexture(texture);
        particle.setImmortal();
        return particle;
    }
}
