package io.github.cleanroommc.multiblocked.api.item;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockedItems;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class ItemMultiblockBuilder extends Item {

    public ItemMultiblockBuilder() {
        setRegistryName(Multiblocked.MODID, "multiblock_builder");
        setCreativeTab(Multiblocked.CREATIVE_TAB);
        setTranslationKey(Multiblocked.MODID + "multiblock_builder");
    }

    public static boolean isItemMultiblockBuilder(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemMultiblockBuilder;
    }

    public static boolean isRaw(ItemStack stack) {
        return stack.getMetadata() == 0;
    }

    public static boolean setPattern(ItemStack stack) {
        if (isItemMultiblockBuilder(stack)) {
            stack.setItemDamage(1);
            return true;
        }
        return false;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            ItemStack hold = player.getHeldItem(hand);
            if (isItemMultiblockBuilder(hold) && tileEntity instanceof ControllerTileEntity) {
                if (isRaw(hold)) {
                    ((ControllerTileEntity) tileEntity).getPattern().autoBuild(player, new MultiblockState(world, pos));
                    return EnumActionResult.SUCCESS;
                } else {
                    String json = hold.getOrCreateSubCompound("pattern").getString("json");
                    if (!json.isEmpty()) {
                        JsonBlockPattern jsonBlockPattern = Multiblocked.GSON.fromJson(json, JsonBlockPattern.class);
                        if (jsonBlockPattern.predicates.get("controller") instanceof PredicateComponent) {
                            PredicateComponent predicate = (PredicateComponent) jsonBlockPattern.predicates.get("controller");
                            if (predicate.location.equals(((ControllerTileEntity) tileEntity).getDefinition().location)) {
                                jsonBlockPattern.build().autoBuild(player, new MultiblockState(world, pos));
                                return EnumActionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
        }
        return EnumActionResult.PASS;
    }

    public static class BuilderRecipeLogic extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe{
        private final ItemStack resultStack;
        private final Ingredient builder;
        private final Ingredient blueprint;

        public BuilderRecipeLogic() {
            this.setRegistryName(Multiblocked.MODID, "builder");
            this.resultStack = MultiblockedItems.BUILDER.getDefaultInstance();
            ItemMultiblockBuilder.setPattern(resultStack);
            ItemStack stack = MultiblockedItems.BLUEPRINT.getDefaultInstance();
            ItemBlueprint.setPattern(stack);
            blueprint = Ingredient.fromStacks(stack);
            stack = MultiblockedItems.BUILDER.getDefaultInstance();
            ItemMultiblockBuilder.setPattern(stack);
            builder = Ingredient.fromStacks(MultiblockedItems.BUILDER.getDefaultInstance(), stack);
        }

        @Override
        public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
            ItemStack a = null;
            ItemStack b = null;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack itemStack = inv.getStackInSlot(i);
                if (itemStack.isEmpty()) continue;
                if (a == null) {
                    a = itemStack;
                } else if (b == null) {
                    b = itemStack;
                } else {
                    return false;
                }
            }
            if (a== null || b == null) return false;
            return (builder.test(a) && blueprint.test(b)) || (builder.test(b) && blueprint.test(a));
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
            ItemStack a = null;
            ItemStack b = null;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack itemStack = inv.getStackInSlot(i);
                if (itemStack.isEmpty()) continue;
                if (a == null) {
                    a = itemStack;
                } else if (b == null) {
                    b = itemStack;
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (a== null || b == null) return ItemStack.EMPTY;
            ItemStack builder = MultiblockedItems.BUILDER.getDefaultInstance();
            ItemMultiblockBuilder.setPattern(builder);
            if (ItemBlueprint.isItemBlueprint(a) && !ItemBlueprint.isRaw(a)) {
                builder.getOrCreateSubCompound("pattern").setString("json", a.getOrCreateSubCompound("pattern").getString("json"));
            } else if (ItemBlueprint.isItemBlueprint(b) && !ItemBlueprint.isRaw(b)) {
                builder.getOrCreateSubCompound("pattern").setString("json", b.getOrCreateSubCompound("pattern").getString("json"));
            }
            return builder;
        }

        @Nonnull
        @Override
        public ItemStack getRecipeOutput() {
            return resultStack.copy();
        }

        @Nonnull
        @Override
        public NonNullList<Ingredient> getIngredients() {
            return NonNullList.from(builder, blueprint);
        }

        @Override
        public boolean canFit(int width, int height) {
            return width * height >= 2;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Nonnull
        @Override
        public String getGroup() {
            return "";
        }
    }
}
