package com.cleanroommc.multiblocked.client.particle;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTLaserParticle;
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
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Optional.Interface(modid = Multiblocked.MODID_CT, iface = "com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTLaserParticle")
public class LaserBeamParticle extends AbstractParticle implements ICTLaserParticle {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private int body = -1;
    private int head = -1;
    private Vector3 direction;
    private float beamHeight = 0.075f;
    private float headWidth;
    private float alpha = 1;
    private float emit;
    private boolean verticalMode;

    public LaserBeamParticle(World worldIn, Vector3 startPos, Vector3 endPos) {
        super(worldIn, startPos.x, startPos.y, startPos.z);
        this.direction = endPos.copy().subtract(startPos);
    }

    @Override
    public LaserBeamParticle getInner() {
        return this;
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
    public void setBody(ResourceLocation location) {
        ITextureObject texture = ResourceUtils.getTextureObject(location);
        body = texture == null ? -1 : texture.getGlTextureId();
    }

    /**
     * Set head body texture
     * 
     * @param location texture resource.
     */
    public void setHead(ResourceLocation location) {
        ITextureObject texture = ResourceUtils.getTextureObject(location);
        head = texture == null ? -1 : texture.getGlTextureId();
    }

    public void setStartPos(Vector3 startPos) {
        this.direction.add(posX, posY, posZ).subtract(startPos);
    }

    public void setEndPos(Vector3 endPos) {
        this.direction = endPos.copy().subtract(posX, posY, posZ);
    }

    public void setBeamHeight(float beamHeight) {
        this.beamHeight = beamHeight;
    }

    public void setHeadWidth(float headWidth) {
        this.headWidth = headWidth;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    /**
     * Set emit speed.
     * @param emit emit speed. from start to end.
     */
    public void setEmit(float emit) {
        this.emit = emit;
    }

    /**
     * Is 3D beam rendered by two perpendicular quads.
     * <P>
     *     It is not about performance, some textures are suitable for this, some are not, please choose according to the texture used.
     * </P>
     */
    public void setVerticalMode(boolean verticalMode) {
        this.verticalMode = verticalMode;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderParticle(BufferBuilder buffer, float partialTicks) {
        double tX = posX - ParticleManager.interPosX;
        double tY = posY - ParticleManager.interPosY;
        double tZ = posZ - ParticleManager.interPosZ;
        GlStateManager.translate(tX, tY, tZ);

        Vector3 cameraDirection = null;
        if (!verticalMode) {
            cameraDirection = new Vector3(posX, posY, posZ).subtract(new Vector3(ParticleManager.entity.getPositionEyes(partialTicks)));
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
