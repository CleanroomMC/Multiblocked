package com.cleanroommc.multiblocked.core.vanilla.mixins;

import com.cleanroommc.multiblocked.client.particle.ParticleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Shadow @Final private Minecraft mc;

    @Inject(method = "renderWorldPass",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
                    ordinal = 0))
    private void passZero(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        this.mc.profiler.endStartSection("MBDParticle");
        Entity entity = mc.getRenderViewEntity();
        ParticleManager.INSTANCE.renderParticles(true, entity == null ? mc.player : entity, partialTicks);
        this.mc.profiler.endStartSection("entities");
    }

    @Inject(method = "renderWorldPass",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
                    ordinal = 1))
    private void passOne(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        this.mc.profiler.endStartSection("MBDParticle");
        Entity entity = mc.getRenderViewEntity();
        ParticleManager.INSTANCE.renderParticles(false, entity == null ? mc.player : entity, partialTicks);
        this.mc.profiler.endStartSection("entities");
    }
}
