package com.cleanroommc.multiblocked.integration.provider;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class RecipeProgressInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return String.format("%s:recipe_progress_provider", Multiblocked.MODID);
    }

    @Override
    public void addProbeInfo(@Nonnull ProbeMode probeMode, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer entityPlayer, @Nonnull World world, @Nonnull IBlockState blockState, @Nonnull IProbeHitData probeHitData) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(probeHitData.getPos());
            if (tileEntity instanceof ControllerTileEntity) {
                RecipeLogic recipeLogic = ((ControllerTileEntity) tileEntity).getRecipeLogic();
                if (recipeLogic != null) {
                    int maxProgress = recipeLogic.duration;
                    if (maxProgress > 0) {
                        int currentProgress = recipeLogic.progress;
                        String text;

                        if (maxProgress < 20) {
                            // less than 1 second uses ticks
                            text = String.format(" t / %s t", maxProgress);
                        } else {
                            currentProgress = Math.round(currentProgress / 20.0F);
                            maxProgress = Math.round(maxProgress / 20.0F);
                            text = String.format(" s / %s s", maxProgress);
                        }

                        IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                        horizontalPane.text(TextStyleClass.INFO + "{*multiblocked.top.recipe_progress*} ");
                        horizontalPane.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
                                .suffix(text)
                                .filledColor(0xFF4CBB17)
                                .alternateFilledColor(0xFF4CBB17));
                    }
                }
            }
        }
    }
}
