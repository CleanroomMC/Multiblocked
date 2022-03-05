package io.github.cleanroommc.multiblocked.core.mixins;

import io.github.cleanroommc.multiblocked.client.MultiblockedResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Shadow @Final private List<IResourcePack> defaultResourcePacks;

	@Inject(method = "refreshResources", at = @At(value = "HEAD"))
	private void addInternalResourcePack(CallbackInfo ci) {
		if (defaultResourcePacks.isEmpty() || !defaultResourcePacks.contains(MultiblockedResourceLoader.INSTANCE)) {
			defaultResourcePacks.add(MultiblockedResourceLoader.INSTANCE);
		}
	}

}
