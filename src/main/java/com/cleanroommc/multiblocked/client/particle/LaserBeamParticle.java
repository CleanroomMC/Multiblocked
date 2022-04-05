package com.cleanroommc.multiblocked.client.particle;

import com.cleanroommc.multiblocked.client.renderer.fx.LaserBeamRenderer;
import com.cleanroommc.multiblocked.util.ResourceUtils;
import com.cleanroommc.multiblocked.util.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LaserBeamParticle extends AbstractParticle {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private int body = -1;
    private int head = -1;
    private Vector3 direction;
    private float beamHeight = 0.075f;
    private float headWidth;
    private float alpha = 1;
    private float emit;
    private boolean doubleVertical;

    public LaserBeamParticle(World worldIn, Vector3 startPos, Vector3 endPos) {
        super(worldIn, startPos.x, startPos.y, startPos.z);
        this.setImmortal();
        this.setRenderRange(64);
        this.direction = endPos.copy().subtract(startPos);
    }

    @Override
    public boolean shouldRendered(Entity entityIn, float partialTicks) {
        if (squareRenderRange < 0) return true;
        Vec3d eyePos = entityIn.getPositionEyes(partialTicks);
        return eyePos.squareDistanceTo(posX, posY, posZ) <= squareRenderRange ||
                eyePos.squareDistanceTo(posX + direction.x, posY + direction.y, posZ + direction.z) <= squareRenderRange;
    }

    /**
     * Set beam body texture
     * 
     * @param location texture resource.
     */
    public LaserBeamParticle setBody(ResourceLocation location) {
        ITextureObject texture = ResourceUtils.getTextureObject(location);
        body = texture == null ? -1 : texture.getGlTextureId();
        return this;
    }

    /**
     * Set head body texture
     * 
     * @param location texture resource.
     */
    public LaserBeamParticle setHead(ResourceLocation location) {
        ITextureObject texture = ResourceUtils.getTextureObject(location);
        head = texture == null ? -1 : texture.getGlTextureId();
        return this;
    }

    public LaserBeamParticle setStartPos(Vector3 startPos) {
        this.direction.add(posX, posY, posZ).subtract(startPos);
        return this;
    }

    public LaserBeamParticle setEndPos(Vector3 endPos) {
        this.direction = endPos.copy().subtract(posX, posY, posZ);
        return this;
    }

    public LaserBeamParticle setBeamHeight(float beamHeight) {
        this.beamHeight = beamHeight;
        return this;
    }

    public LaserBeamParticle setHeadWidth(float headWidth) {
        this.headWidth = headWidth;
        return this;
    }

    public LaserBeamParticle setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    /**
     * Set emit speed.
     * @param emit emit speed. from start to end.
     */
    public LaserBeamParticle setEmit(float emit) {
        this.emit = emit;
        return this;
    }

    /**
     * Is 3D beam rendered by two perpendicular quads.
     * <P>
     *     It is not about performance, some textures are suitable for this, some are not, please choose according to the texture used.
     * </P>
     */
    public LaserBeamParticle setDoubleVertical(boolean doubleVertical) {
        this.doubleVertical = doubleVertical;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks) {
        double tX = posX - particleManager.interPosX;
        double tY = posY - particleManager.interPosY;
        double tZ = posZ - particleManager.interPosZ;
        GlStateManager.translate(tX, tY, tZ);

        Vector3 cameraDirection = null;
        if (!doubleVertical) {
            cameraDirection = new Vector3(posX, posY, posZ).subtract(new Vector3(entityIn.getPositionEyes(partialTicks)));
        }
        float offset = - emit * (MINECRAFT.player.ticksExisted + partialTicks);
        LaserBeamRenderer.renderRawBeam(body, head, direction, cameraDirection, beamHeight, headWidth, alpha, offset);
        GlStateManager.translate(-tX, -tY, -tZ);
    }

    @Override
    public IParticleHandler getGLHandler() {
        return HANDLER;
    }

    public static IParticleHandler HANDLER = new IParticleHandler() {
        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        public void preDraw(BufferBuilder buffer) {
            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            GlStateManager.enableRescaleNormal();
            GlStateManager.disableCull();
        }

        @Override
        public void postDraw(BufferBuilder buffer) {
            GlStateManager.enableCull();
            GlStateManager.disableRescaleNormal();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }

    };
}
