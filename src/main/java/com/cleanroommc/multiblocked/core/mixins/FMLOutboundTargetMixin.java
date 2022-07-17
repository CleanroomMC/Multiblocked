package com.cleanroommc.multiblocked.core.mixins;

import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

/**
 * @author youyihj
 */
@Mixin(remap = false, targets = {
        "net.minecraftforge.fml.common.network.FMLOutboundHandler$OutboundTarget$5",
        "net.minecraftforge.fml.common.network.FMLOutboundHandler$OutboundTarget$6",
        "net.minecraftforge.fml.common.network.FMLOutboundHandler$OutboundTarget$7",
})
public class FMLOutboundTargetMixin {
    @Inject(method = "selectNetworks", at = @At("HEAD"), cancellable = true)
    public void selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet, CallbackInfoReturnable<List<NetworkDispatcher>> cir) {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
