package com.cleanroommc.multiblocked.client.particle;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTParticle;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@Optional.Interface(modid = Multiblocked.MODID_CT, iface = "com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTParticle")
public abstract class AbstractParticle implements IParticle, ICTParticle {
    public World world;
    public double posX;
    public double posY;
    public double posZ;
    public boolean isBackLayer;
    public boolean isAddBlend;
    public int particleLife;
    public int squareRenderRange;
    public Consumer<AbstractParticle> onUpdate;

    public AbstractParticle(World world, double posX, double posY, double posZ) {
        this.world = world;
        this.squareRenderRange = -1;
        setPosition(posX, posY, posZ);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public double getX() {
        return posX;
    }

    @Override
    public double getY() {
        return posY;
    }

    @Override
    public double getZ() {
        return posZ;
    }

    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }
    
    public void setLife(int life) {
        if (squareRenderRange == -1 && life > 500) {
            setRenderRange(64);
        }
        this.particleLife = life;
    }

    public int getLife() {
        return particleLife;
    }

    public void setAddBlend(boolean addBlend) {
        isAddBlend = addBlend;
    }

    @Override
    public AbstractParticle getInner() {
        return this;
    }

    @Override
    public boolean isBackLayer() {
        return isBackLayer;
    }

    @Override
    public boolean isAddBlend() {
        return isAddBlend;
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
        if (this.squareRenderRange == -1) {
            this.setRenderRange(64);
        }
        this.particleLife = -1;
    }

    /**
     * Update each tick
     */
    public void setOnUpdate(Consumer<AbstractParticle> onUpdate) {
        this.onUpdate = onUpdate;
    }
}
