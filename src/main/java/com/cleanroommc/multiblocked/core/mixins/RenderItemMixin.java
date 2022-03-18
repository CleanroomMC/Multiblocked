package com.cleanroommc.multiblocked.core.mixins;

import com.cleanroommc.multiblocked.client.renderer.ICustomItemRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class RenderItemMixin {

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", at = @At(value = "HEAD"), cancellable = true)
    private void injectRenderBlockDamage(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (!stack.isEmpty() && model instanceof ICustomItemRenderer) {
            ICustomItemRenderer renderer = (ICustomItemRenderer) model;
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            renderer.renderItem(stack);
            GlStateManager.popMatrix();
            ci.cancel();
        }
    }
}
