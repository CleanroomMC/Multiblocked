package com.cleanroommc.multiblocked.client.particle;

import com.cleanroommc.multiblocked.util.ResourceUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class CommonParticle extends AbstractParticle{
    private static final AxisAlignedBB EMPTY_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    private static final Map<ResourceLocation, IParticleHandler> textureMap = new HashMap<>();

    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public double motionX;
    public double motionY;
    public double motionZ;
    private AxisAlignedBB boundingBox;
    public boolean onGround;
    public boolean canCollide;
    public float width;
    public float height;
    public int particleTextureIndexX;
    public int particleTextureIndexY;
    public float particleScale;
    public float particleGravity;
    public float particleRed;
    public float particleGreen;
    public float particleBlue;
    public float particleAlpha;
    public int texturesCount = 1;
    public int lightMap = -1;
    public boolean motionless = false;

    protected ResourceLocation customTexture;

    public CommonParticle(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.boundingBox = EMPTY_AABB;
        this.width = 0.6F;
        this.height = 1.8F;
        this.particleAlpha = 1.0F;
        this.setSize(0.2F, 0.2F);
        this.setPosition(posXIn, posYIn, posZIn);
        this.prevPosX = posXIn;
        this.prevPosY = posYIn;
        this.prevPosZ = posZIn;
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.particleLife = (int)(4.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
        this.canCollide = true;
    }

    public CommonParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        this(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.motionX = xSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        this.motionY = ySpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        this.motionZ = zSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        float f = (float)(Math.random() + Math.random() + 1.0D) * 0.15F;
        float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        this.motionX = this.motionX / (double)f1 * (double)f * 0.4000000059604645D;
        this.motionY = this.motionY / (double)f1 * (double)f * 0.4000000059604645D + 0.10000000149011612D;
        this.motionZ = this.motionZ / (double)f1 * (double)f * 0.4000000059604645D;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        float f = this.width / 2.0F;
        float f1 = this.height;
        this.setBoundingBox(new AxisAlignedBB(x - (double)f, y, z - (double)f, x + (double)f, y + (double)f1, z + (double)f));
    }

    public void setSize(float particleWidth, float particleHeight) {
        if (particleWidth != this.width || particleHeight != this.height) {
            this.width = particleWidth;
            this.height = particleHeight;
            setPosition(posX, posY, posZ);
        }
    }

    /**
     * It can stop motion. It always has a motion before {@link net.minecraft.client.particle.Particle#onUpdate()}
     */
    public void setMotionless(boolean motionless) {
        this.motionless = motionless;
    }

    /**
     * Set color blend of this particle.
     */
    public void setColor(int color) {
        this.particleRed = (color >> 16 & 255) / 255.0F;
        this.particleGreen = (color >> 8 & 255) / 255.0F;
        this.particleBlue = (color & 255) / 255.0F;
        this.particleAlpha = (color >> 24 & 255) / 255.0F;
    }

    /**
     * Set scale of this particle.
     */
    public void setScale(float scale) {
        this.particleScale = scale;
    }

    /**
     * Set Gravity of this particle.
     */
    public void setGravity(float gravity) {
        this.particleGravity = gravity;
    }

    /**
     * Set sub-texture coord.
     */
    public void setTexturesIndex(int particleTextureIndexX, int particleTextureIndexY) {
        this.particleTextureIndexX = particleTextureIndexX;
        this.particleTextureIndexY = particleTextureIndexY;
    }

    /**
     * How many sub-textures in the current texture. it always 16 in the {@link net.minecraft.client.particle.Particle}. but we allow the bigger or smaller texture in the MBDParticle.
     */
    public void setTexturesCount(int texturesCount) {
        this.texturesCount = texturesCount;
    }

    public void setParticleTextureIndex(int particleTextureIndex) {
        this.particleTextureIndexX = particleTextureIndex % texturesCount;
        this.particleTextureIndexY = particleTextureIndex / texturesCount;
    }

    public float getTexturesCount() {
        return texturesCount;
    }

    public boolean isMotionless() {
        return motionless;
    }


    public void onUpdate() {
        super.onUpdate();

        if (!motionless) {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            this.motionY -= 0.04D * (double)this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;

            if (this.onGround) {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
            }
        }
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB bb)
    {
        this.boundingBox = bb;
    }

    public void move(double x, double y, double z) {
        double d0 = y;
        double origX = x;
        double origZ = z;

        if (this.canCollide) {
            List<AxisAlignedBB> list = this.world.getCollisionBoxes(null, this.getBoundingBox().expand(x, y, z));
            for (AxisAlignedBB axisalignedbb : list) {
                y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
            }
            this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));
            for (AxisAlignedBB axisalignedbb1 : list) {
                x = axisalignedbb1.calculateXOffset(this.getBoundingBox(), x);
            }
            this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));
            for (AxisAlignedBB axisalignedbb2 : list) {
                z = axisalignedbb2.calculateZOffset(this.getBoundingBox(), z);
            }

            this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));
        }
        else {
            this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
        }

        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = axisalignedbb.minY;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

        this.onGround = d0 != y && d0 < 0.0D;

        if (origX != x)
        {
            this.motionX = 0.0D;
        }

        if (origZ != z)
        {
            this.motionZ = 0.0D;
        }
    }

    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks) {
        float rotationX = ParticleManager.rotationX;
        float rotationZ = ParticleManager.rotationXZ;
        float rotationYZ = ParticleManager.rotationZ;
        float rotationXY = ParticleManager.rotationYZ;
        float rotationXZ = ParticleManager.rotationXY;

        float minU = this.particleTextureIndexX * 1F / texturesCount;
        float maxU = minU + 1F / texturesCount;//0.0624375F;
        float minV = this.particleTextureIndexY * 1F / texturesCount;
        float maxV = minV + 1F / texturesCount;//0.0624375F;
        float scale = 0.1F * this.particleScale;

        float renderX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - ParticleManager.interPosX);
        float renderY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - ParticleManager.interPosY);
        float renderZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - ParticleManager.interPosZ);
        int brightnessForRender = this.getBrightnessForRender();
        int j = brightnessForRender >> 16 & 0xffff;
        int k = brightnessForRender & 0xffff;
        buffer.pos(renderX - rotationX * scale - rotationXY * scale, renderY - rotationZ * scale,  (renderZ - rotationYZ * scale - rotationXZ * scale)).tex(maxU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX - rotationX * scale + rotationXY * scale, renderY + rotationZ * scale,  (renderZ - rotationYZ * scale + rotationXZ * scale)).tex(maxU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX + rotationX * scale + rotationXY * scale,  (renderY + rotationZ * scale),  (renderZ + rotationYZ * scale + rotationXZ * scale)).tex(minU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX + rotationX * scale - rotationXY * scale,  (renderY - rotationZ * scale),  (renderZ + rotationYZ * scale - rotationXZ * scale)).tex(minU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }

    public int getBrightnessForRender() {
        if (lightMap >= 0) return lightMap;
        BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
        return this.world.isBlockLoaded(blockpos) ? this.world.getCombinedLight(blockpos, 0) : 0;
    }

    public void setTexture(ResourceLocation texture) {
        this.customTexture = texture;
    }

    public void setLightingMap(int block, int sky) {
        lightMap = (((block * 16) & 0xffff) << 16) | ((sky * 16) & 0xffff);
    }

    @Override
    public IParticleHandler getGLHandler() {
        return textureMap.computeIfAbsent(customTexture, TexturedParticleHandler::new);
    }

    private static class TexturedParticleHandler implements IParticleHandler {
        private final ResourceLocation texture;

        public TexturedParticleHandler(ResourceLocation texture) {
            this.texture = texture;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void preDraw(BufferBuilder buffer) {
            ResourceUtils.bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void postDraw(BufferBuilder buffer) {
            Tessellator.getInstance().draw();
        }
    }
}
