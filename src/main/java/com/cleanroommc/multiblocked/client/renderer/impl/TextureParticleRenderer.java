package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.particle.AbstractParticle;
import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TextureParticleRenderer extends ParticleRenderer{

    public ResourceLocation texture;
    public boolean isShader = false;
    public float scale = 1;
    public int light = -1;

    public TextureParticleRenderer(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected AbstractParticle createParticle(ComponentTileEntity<?> te, double x, double y, double z) {
        CommonParticle particle = isShader ? new ShaderTextureParticle(te.getWorld(), x, y, z) : new CommonParticle(te.getWorld(), x, y, z);
        particle.setScale(scale);
        if (light >= 0) {
            particle.setLightingMap(light, light);
        }
        particle.setTexture(texture);
        return particle;
    }
}
