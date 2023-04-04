package com.cleanroommc.multiblocked.api.capability;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.BlockComponent;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.jei.IJeiIngredientAdapter;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Used to detect whether a machine has a certain capability. And provide its capability in proxy {@link CapabilityProxy}.
 *
 */
@ZenClass("mods.multiblocked.capability.Capability")
@ZenRegister
public abstract class MultiblockCapability<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    @ZenProperty
    public final String name;
    @ZenProperty
    public final int color;
    @Nullable
    public final IJeiIngredientAdapter<T, ?> jeiIngredientAdapter;

    protected MultiblockCapability(String name, int color, IJeiIngredientAdapter<T, ?> jeiIngredientAdapter) {
        this.name = name;
        this.color = color;
        this.jeiIngredientAdapter = jeiIngredientAdapter;
    }

    protected MultiblockCapability(String name, int color) {
        this(name, color, null);
    }

    public String getUnlocalizedName() {
        return "multiblocked.capability." + name;
    }

    /**
     * default content for the RecipeMapWidget selector
     */
    public abstract T defaultContent();

    /**
     * detect whether this block has capability
     */
    public abstract boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity);

    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    public abstract T copyInner(T content);

    /**
     * deep copy with a new amount of this content by the given modifier.
     */
    public T copyInnerByModifier(T content, ContentModifier modifier) {
        return copyInner(content);
    }
    
    /**
     * create a proxy of this block.
     */
    protected abstract CapabilityProxy<? extends T> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity);

    /**
     * create a proxy of this block.
     */
    public CapabilityProxy<? extends T> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity, EnumFacing facing, @Nullable Map<Long, Set<String>> slotsMap) {
        CapabilityProxy<? extends T> proxy = createProxy(io, tileEntity);
        proxy.facing = facing;
        proxy.slots = slotsMap == null ? null : slotsMap.get(tileEntity.getPos().toLong());
        if (tileEntity instanceof IInnerCapabilityProvider) {
            IInnerCapabilityProvider slotNameProvider = (IInnerCapabilityProvider) tileEntity;
            if (proxy.slots == null) {
                proxy.slots = slotNameProvider.getSlotNames();
            } else {
                proxy.slots.addAll(slotNameProvider.getSlotNames());
            }
        }
        return proxy;
    }

    /**
     * Create a Widget of given contents
     */
    public ContentWidget<? super T> createContentWidget() {
        return new ContentWidget<T>() {
            @Override
            protected void onContentUpdate() {
                if (Multiblocked.isClient()) {
                    setHoverTooltip(I18n.format("multiblocked.content.miss", io, I18n.format(MultiblockCapability.this.getUnlocalizedName()), content.toString()));
                }
            }

            @Override
            public void openConfigurator(WidgetGroup dialog) {
                super.openConfigurator(dialog);
                dialog.addWidget(new LabelWidget(5, 30, "multiblocked.gui.label.configurator"));
            }

        }.setBackground(new ColorRectTexture(color));
    }

    public boolean hasTrait() {
        return false;
    }

    public CapabilityTrait createTrait() {
        return null;
    }

    public final <C> Set<C> getCapability(Capability<C> capability, @Nonnull TileEntity tileEntity) {
        Set<C> found = new LinkedHashSet<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            C cap = tileEntity.getCapability(capability, facing);
            if (cap != null) return Collections.singleton(cap);
        }
        return found;
    }

    /**
     * For blocks that only support a capability on limited sides, find the first supporting side.
     */
    @Nonnull
    public final <C> EnumFacing getFrontFacing(Capability<C> capability, @Nonnull TileEntity tileEntity) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            C cap = tileEntity.getCapability(capability, facing);
            if (cap != null) {
                return facing;
            }
        }
        return EnumFacing.UP;
    }

    /**
     * Get candidate blocks for display in JEI as well as automated builds
     */
    public BlockInfo[] getCandidates() {
        if (hasTrait()) {
            return MbdComponents.getBlockInfoWithCapability(this);
        }
        throw new RuntimeException("Multiblocked Capability " + name + " doesn't give proper candidates.");
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public final BlockComponent getAnyBlock() {
        return MbdComponents.COMPONENT_BLOCKS_REGISTRY.get(new ResourceLocation(Multiblocked.MODID, name + ".any"));
    }

    public final JsonElement serialize(Object obj) {
        return serialize((T)obj, null, null);
    }

    public final T deserialize(JsonElement jsonElement){
        return deserialize(jsonElement, null, null);
    }

}
