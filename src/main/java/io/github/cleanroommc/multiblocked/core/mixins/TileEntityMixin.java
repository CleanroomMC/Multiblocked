package io.github.cleanroommc.multiblocked.core.mixins;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TileEntity.class)
public class TileEntityMixin {

	@Shadow @Final private static Logger LOGGER;

	@Inject(method = "create", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void preventDataLoss(World world, NBTTagCompound nbt, CallbackInfoReturnable<TileEntity> cir, TileEntity tile, String s, Class<? extends TileEntity> oclass) {
		if (oclass == null) {
			Block block = world.getBlockState(new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"))).getBlock();
			if (block instanceof BlockComponent) {
				try {
					oclass = ((BlockComponent) block).definition.clazz;
					TileEntity newTile = oclass.newInstance();
					newTile.setWorld(world);
					newTile.readFromNBT(nbt);
					cir.setReturnValue(newTile);
				} catch (Throwable throwable) {
					LOGGER.error("Failed to load data for block entity {}", s, throwable);
					FMLLog.log.error("A TileEntity {}({}) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", s, oclass.getName(), throwable);
				}
			}
		}
	}

}
