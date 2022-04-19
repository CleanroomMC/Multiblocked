package com.cleanroommc.multiblocked.core.vanilla.mixins;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityRendererDispatcher.class)
public class TileEntityRendererDispatcherMixin {
    @Shadow public static TileEntityRendererDispatcher instance;

    @Inject(method = "getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;", at = @At(value = "HEAD"), cancellable = true)
    private <T extends TileEntity> void injectGetRenderer(TileEntity tileEntityIn, CallbackInfoReturnable<TileEntitySpecialRenderer<T>> cir) {
        if (tileEntityIn != null) {
            if (tileEntityIn.getWorld() == Minecraft.getMinecraft().world && MultiblockWorldSavedData.modelDisabled.contains(tileEntityIn.getPos())) {
                cir.setReturnValue(null);
            } else if (tileEntityIn instanceof ComponentTileEntity && !((ComponentTileEntity<?>) tileEntityIn).hasTESRRenderer()) {
                cir.setReturnValue(null);
            }
        }
    }
}
