package io.github.cleanroommc.multiblocked.core.mixins;

import io.github.cleanroommc.multiblocked.events.Listeners;
import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Inject(method = "cleanup", at = @At("RETURN"))
    private void onCleanup(CallbackInfo ci) {
        Listeners.voidMapping();
    }

}
