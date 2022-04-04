package com.cleanroommc.multiblocked.client.particle;

import com.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public abstract class AbstractParticle implements IParticle {
    public World world;
    public double posX;
    public double posY;
    public double posZ;
    public boolean isBackLayer;
    public int particleLife;
    public int squareRenderRange;
    public Consumer<AbstractParticle> onUpdate;
    protected final Random rand;
    protected ParticleManager particleManager;

    public AbstractParticle(World world, double posX, double posY, double posZ) {
        this.world = world;
        this.squareRenderRange = -1;
        this.rand = Multiblocked.RNG;
        setPosition(posX, posY, posZ);
    }

    @Override
    public void setParticleManager(ParticleManager particleManager) {
        this.particleManager = particleManager;
    }

    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }
    
    public AbstractParticle setLife(int life) {
        this.particleLife = life;
        return this;
    }

    @Override
    public boolean isBackLayer() {
        return isBackLayer;
    }

    @Override
    public void setBackLayer(boolean isBackLayer) {
        this.isBackLayer = isBackLayer;
    }

    @Override
    public void kill() {
        this.particleLife = 0;
    }

    @Override
    public boolean isAlive() {
        return this.particleLife != 0;
    }

    @Override
    public void onUpdate() {
        if (this.onUpdate != null) {
            onUpdate.accept(this);
        }
        if (this.particleLife > 0) {
            particleLife--;
        }
    }

    @Override
    public boolean shouldRendered(Entity entityIn, float partialTicks) {
        if (squareRenderRange < 0) return true;
        return entityIn.getPositionEyes(partialTicks).squareDistanceTo(posX, posY, posZ) <= squareRenderRange;
    }

    /**
     * Set the render range, over the range do not render.
     * <P>
     *     -1 -- always render.
     * </P>
     */
    public void setRenderRange(int renderRange) {
        this.squareRenderRange = renderRange * renderRange;
    }

    public int getRenderRange() {
        return squareRenderRange >= 0 ? -1 : (int) Math.sqrt(squareRenderRange);
    }

    /**
     * Particles can live forever now.
     */
    public void setImmortal() {
        this.particleLife = -1;
    }

    /**
     * Update each tick
     */
    public void setOnUpdate(Consumer<AbstractParticle> onUpdate) {
        this.onUpdate = onUpdate;
    }
}
