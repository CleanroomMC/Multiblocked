package com.cleanroommc.multiblocked.common.recipe.conditions;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.BlockSelectorWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeCondition;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.google.gson.JsonObject;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/6/14
 * @implNote BlockCondition, check whether such blocks in the structure
 */
public class BlockCondition extends RecipeCondition {
    public final static BlockCondition INSTANCE = new BlockCondition();

    public IBlockState blockState = Blocks.AIR.getDefaultState();
    public int count = 0;

    private BlockCondition() {}

    public BlockCondition(IBlockState blockState, int count) {
        this.blockState = blockState;
        this.count = count;
    }

    @Override
    public String getType() {
        return "block";
    }

    @Override
    public ITextComponent getTooltips() {
        return new TextComponentTranslation(blockState.getBlock().getTranslationKey()).appendText(" (" + count + ")");
    }

    @Override
    public boolean test(@Nonnull Recipe recipe, @Nonnull RecipeLogic recipeLogic) {
        int amount = 0;
        for (BlockPos pos : recipeLogic.controller.state.getCache()) {
            if (recipeLogic.controller.getWorld().getBlockState(pos) == blockState) {
                amount++;
                if (amount >= count) break;
            }
        }
        return amount >= count;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new BlockCondition();
    }


    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject jsonObject = super.serialize();
        jsonObject.add("block", Multiblocked.GSON.toJsonTree(blockState));
        jsonObject.addProperty("count", count);
        return jsonObject;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
        super.deserialize(config);
        blockState = Multiblocked.GSON.fromJson(config.get("block"), IBlockState.class);
        count = config.get("count").getAsInt();
        return this;
    }

    @Override
    public void openConfigurator(WidgetGroup group) {
        super.openConfigurator(group);
        group.addWidget(new BlockSelectorWidget(0, 20, true).setOnBlockStateUpdate(state -> blockState = state).setBlock(blockState));
        group.addWidget(new TextFieldWidget(0, 45, 60, 15, true, null, s->count = Integer.parseInt(s))
                .setCurrentString(count + "")
                .setNumbersOnly(Integer.MIN_VALUE, Integer.MAX_VALUE)
                .setHoverTooltip("multiblocked.gui.condition.block.count"));
    }
}
