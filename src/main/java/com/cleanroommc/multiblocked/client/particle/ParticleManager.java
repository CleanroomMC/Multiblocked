package com.cleanroommc.multiblocked.client.particle;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.util.EntityCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * ParticleManger register, spawn, efficient rendering, update our custom particles.
 */
@SideOnly(Side.CLIENT)
public class ParticleManager {
    public final static ParticleManager INSTANCE = new ParticleManager();

    private static World currentWorld = null;
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Map<IParticleHandler, ArrayDeque<IParticle>> renderQueueBack = new HashMap<>();
    private final Map<IParticleHandler, ArrayDeque<IParticle>> renderQueueFront = new HashMap<>();
    private final Queue<Tuple<IParticleHandler, IParticle>> newParticleQueue = new ArrayDeque<>();

    public double interPosX;
    public double interPosY;
    public double interPosZ;
    public float rotationX;
    public float rotationZ;
    public float rotationYZ;
    public float rotationXY;
    public float rotationXZ;
    public Vec3d cameraViewDir;

    public void addEffect(IParticle... particles) {
        for (IParticle particle : particles) {
            if (particle.getGLHandler() != null) {
                particle.setParticleManager(this);
                newParticleQueue.add(new Tuple<>(particle.getGLHandler(), particle));
            } 
        }
    }

    public void addEffect(List<IParticle> particles) {
        for (IParticle particle : particles) {
            if (particle.getGLHandler() != null) {
                particle.setParticleManager(this);
                newParticleQueue.add(new Tuple<>(particle.getGLHandler(), particle));
            }
        }
    }

    public void updateEffects() {
        updateEffectLayer();
        if (!newParticleQueue.isEmpty()) {
            for (Tuple<IParticleHandler, IParticle> handlerParticle = newParticleQueue.poll(); handlerParticle != null; handlerParticle = newParticleQueue.poll()) {
                IParticleHandler handler = handlerParticle.getFirst();
                IParticle particle = handlerParticle.getSecond();
                Map<IParticleHandler, ArrayDeque<IParticle>> renderQueue = particle.isBackLayer() ? renderQueueBack : renderQueueFront;
                if (!renderQueue.containsKey(handler)) {
                    renderQueue.put(handler, new ArrayDeque<>());
                }
                ArrayDeque<IParticle> arrayDeque = renderQueue.get(handler);
                if (arrayDeque.size() > 6000) {
                    arrayDeque.removeFirst().kill();
                }
                arrayDeque.add(particle);
            }
        }
    }

    private void updateEffectLayer() {
        if (!renderQueueBack.isEmpty()) {
            updateQueue(renderQueueBack);
        }
        if (!renderQueueFront.isEmpty()) {
            updateQueue(renderQueueFront);
        }
    }

    private void updateQueue(Map<IParticleHandler, ArrayDeque<IParticle>> renderQueue) {
        Iterator<Map.Entry<IParticleHandler, ArrayDeque<IParticle>>> entryIterator = renderQueue.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<IParticleHandler, ArrayDeque<IParticle>> entry = entryIterator.next();
            Iterator<IParticle> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                IParticle particle = iterator.next();
                tickParticle(particle);
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
            if (entry.getValue().isEmpty()) {
                entryIterator.remove();
            }
        }
    }

    public void clearAllEffects(boolean cleanNewQueue) {
        if (cleanNewQueue) {
            for (Tuple<IParticleHandler, IParticle> tuple : newParticleQueue) {
                tuple.getSecond().kill();
            }
            newParticleQueue.clear();
        }
        for (ArrayDeque<IParticle> particles : renderQueueBack.values()) {
            particles.forEach(IParticle::kill);
        }
        for (ArrayDeque<IParticle> particles : renderQueueFront.values()) {
            particles.forEach(IParticle::kill);
        }
        renderQueueBack.clear();
        renderQueueFront.clear();
    }

    private void tickParticle(final IParticle particle) {
        try {
            particle.onUpdate();
        }
        catch (Throwable throwable) {
            Multiblocked.LOGGER.error("particle update error: {}", particle.toString(), throwable);
            particle.kill();
        }
    }

    public void renderParticles(boolean back, Entity entityIn, float partialTicks) {
        if (renderQueueBack.isEmpty() && back) return;
        if (renderQueueFront.isEmpty() && !back) return;
        
        updateRenderInfo(entityIn);

        interPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        interPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        interPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        if (entityIn instanceof EntityCamera) {
            interPosX *= 0.00001; //zNear / zFar
            interPosY *= 0.00001; //zNear / zFar
            interPosZ *= 0.00001; //zNear / zFar
        }
        cameraViewDir = entityIn.getLook(partialTicks);

//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);

        mc.entityRenderer.enableLightmap();
        RenderHelper.disableStandardItemLighting();
        Tessellator tessellator = Tessellator.getInstance();
//        GlStateManager.disableLighting();

        if (back) renderGlParticlesInLayer(renderQueueBack, tessellator, entityIn, partialTicks);

//        GlStateManager.depthMask(false);

        if (!back) renderGlParticlesInLayer(renderQueueFront, tessellator, entityIn, partialTicks);

        mc.entityRenderer.disableLightmap();
        RenderHelper.enableStandardItemLighting();
//        GlStateManager.depthMask(true);
//        GlStateManager.disableBlend();
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    }

    private void updateRenderInfo(Entity entityIn) {
        float radians =0.017453292F;
        rotationX = MathHelper.cos(entityIn.rotationYaw * radians);
        rotationZ = MathHelper.sin(entityIn.rotationYaw * radians);
        rotationYZ = -rotationZ * MathHelper.sin(entityIn.rotationPitch * radians);
        rotationXY = rotationX * MathHelper.sin(entityIn.rotationPitch * radians);
        rotationXZ = MathHelper.cos(entityIn.rotationPitch * radians);
    }

    private void renderGlParticlesInLayer(Map<IParticleHandler, ArrayDeque<IParticle>> renderQueue, Tessellator tessellator, Entity entityIn, float partialTicks) {
        for (IParticleHandler handler : renderQueue.keySet()) {
            ArrayDeque<IParticle> particles = renderQueue.get(handler);
            if (particles.isEmpty()) continue;
            BufferBuilder buffer = tessellator.getBuffer();
            handler.preDraw(buffer);
            for (final IParticle particle : particles) {
                if (particle.shouldRendered(entityIn, partialTicks)) {
                    try {
                        particle.renderParticle(buffer, entityIn, partialTicks);
                    }
                    catch (Throwable throwable) {
                        Multiblocked.LOGGER.error("particle render error: {}", particle.toString(), throwable);
                        particle.kill();
                    }
                }
            }
            handler.postDraw(buffer);
        }
    }

    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.isGamePaused()) {
            return;
        }

        if (currentWorld != mc.world) {
            INSTANCE.clearAllEffects(currentWorld != null);
            currentWorld = mc.world;
        }

        if (currentWorld != null) {
            INSTANCE.updateEffects();
        }
    }

    public static void debugOverlay(RenderGameOverlayEvent.Text event) {
        if (event.getLeft().size() >= 5) {
            String particleTxt = event.getLeft().get(4);
            particleTxt += "." +
                    TextFormatting.GOLD + " PARTICLE-BACK: " + INSTANCE.getStatistics(INSTANCE.renderQueueBack) +
                    TextFormatting.RED + " PARTICLE-FRONt: " + INSTANCE.getStatistics(INSTANCE.renderQueueFront);
            event.getLeft().set(4, particleTxt);
        }
    }

    public String getStatistics(Map<IParticleHandler, ArrayDeque<IParticle>> renderQueue) {
        int g = 0;

        for (ArrayDeque<IParticle> queue : renderQueue.values()) {
            g += queue.size();
        }
        return " GLFX: " + g;
    }

}
