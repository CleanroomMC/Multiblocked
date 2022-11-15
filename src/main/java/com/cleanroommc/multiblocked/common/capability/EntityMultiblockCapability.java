package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.recipe.EntityIngredient;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.trait.EntityCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.widget.EntityContentWidget;
import com.cleanroommc.multiblocked.jei.IJeiIngredientAdapter;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class EntityMultiblockCapability extends MultiblockCapability<EntityIngredient> {
    public static final EntityMultiblockCapability CAP = new EntityMultiblockCapability();

    private EntityMultiblockCapability() {
        super("entity", 0xFF65CB9D, new EntityIngredientAdapter());
    }

    @Override
    public EntityIngredient defaultContent() {
        return new EntityIngredient();
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        if (tileEntity instanceof ComponentTileEntity) {
            return ((ComponentTileEntity<?>) tileEntity).hasTrait(CAP);
        }
        return false;
    }

    @Override
    public EntityIngredient copyInner(EntityIngredient content) {
        return content.copy();
    }

    @Override
    public EntityIngredient copyInnerByModifier(EntityIngredient content, ContentModifier modifier) {
        return content.copy();
    }

    @Override
    public CapabilityProxy<? extends EntityIngredient> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new EntityCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super EntityIngredient> createContentWidget() {
        return new EntityContentWidget();
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return new EntityCapabilityTrait();
    }

    @Override
    public EntityIngredient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return EntityIngredient.fromJson(json);
    }

    @Override
    public JsonElement serialize(EntityIngredient src, Type typeOfSrc, JsonSerializationContext context) {
        return src.toJson();
    }

    public static class EntityCapabilityProxy extends CapabilityProxy<EntityIngredient> {

        public EntityCapabilityProxy(TileEntity tileEntity) {
            super(EntityMultiblockCapability.CAP, tileEntity);
        }

        @Override
        protected List<EntityIngredient> handleRecipeInner(IO io, Recipe recipe, List<EntityIngredient> left, @Nullable String slotName, boolean simulate) {
            TileEntity tileEntity =getTileEntity();
            if (tileEntity instanceof ComponentTileEntity) {
                ComponentTileEntity<?> component = (ComponentTileEntity<?>) tileEntity;
                BlockPos pos = component.getPos().offset(component.getFrontFacing());
                if (io == IO.IN) {
                    List<Entity> entities = component.getWorld().getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(
                            pos,
                            pos.add(1, 1, 1)));
                    for (Entity entity : entities) {
                        if (!entity.isDead) {
                            if (left.removeIf(ingredient -> ingredient.match(entity))) {
                                if (!simulate) {
                                    entity.setDead();
                                }
                            }
                        }
                    }
                } else if (io == IO.OUT){
                    if (!simulate) {
                        for (EntityIngredient ingredient : left) {
                            ingredient.spawn(component.getWorld(), ingredient.tag, pos);
                        }
                    }
                    return null;
                }
            }

            return left.isEmpty() ? null : left;
        }
        Set<Entity> entities = new HashSet<>();

        @Override
        protected boolean hasInnerChanged() {
            if (getTileEntity() instanceof ComponentTileEntity<?>) {
                ComponentTileEntity<?> component = (ComponentTileEntity<?>) getTileEntity();
                BlockPos pos = component.getPos().offset(component.getFrontFacing());
                List<Entity> entities = component.getWorld().getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(
                        pos,
                        pos.add(1, 1, 1)));
                Set<Entity> temp = new HashSet<>();
                for (Entity entity : entities) {
                    if (!entity.isDead) {
                        temp.add(entity);
                    }
                }
                if (this.entities.size() == temp.size() && this.entities.containsAll(temp)) {
                    return false;
                }
                this.entities = temp;
                return true;
            }
            return false;
        }
    }

    public static class EntityIngredientAdapter implements IJeiIngredientAdapter<EntityIngredient, ItemStack> {

        @Override
        public Class<EntityIngredient> getInternalIngredientType() {
            return EntityIngredient.class;
        }

        @Override
        public IIngredientType<ItemStack> getJeiIngredientType() {
            return VanillaTypes.ITEM;
        }

        @Override
        public Stream<ItemStack> apply(EntityIngredient content) {
            if (content.isEntityItem()) {
                return Stream.of(content.getEntityItem());
            } else {
                EntityList.EntityEggInfo egg = content.type.getEgg();
                if (egg == null) {
                    return Stream.empty();
                } else {
                    ItemStack itemStack = new ItemStack(Items.SPAWN_EGG);
                    ItemMonsterPlacer.applyEntityIdToItemStack(new ItemStack(Items.SPAWN_EGG), egg.spawnedID);
                    return Stream.of(itemStack);
                }
            }
        }
    }
}
