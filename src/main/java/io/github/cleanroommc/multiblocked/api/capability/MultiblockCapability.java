package io.github.cleanroommc.multiblocked.api.capability;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to detect whether a machine has a certain capability. And provide its capability in proxy {@link CapabilityProxy}.
 *
 */
@ZenClass("mods.multiblocked.capability.capability")
@ZenRegister
public abstract class MultiblockCapability<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    protected IBlockState[] scannedStates;
    @ZenProperty
    public final String name;
    @ZenProperty
    public final int color;

    protected MultiblockCapability(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getUnlocalizedName() {
        return "multiblocked.capability." + name;
    }

    /**
     * detect whether this block has capability
     */
    public abstract boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity);

    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    public abstract T copyInner(T content);
    
    /**
     * create a proxy of this block.
     */
    public abstract CapabilityProxy<? extends T> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity);

    /**
     * Create a Widget of given contents
     */
    public ContentWidget<? super T> createContentWidget() {
        return new ContentWidget<Object>() {
            @Override
            protected void onContentUpdate() {
                if (Multiblocked.isClient()) {
                    setHoverTooltip(I18n.format("multiblocked.content.miss", io, I18n.format(MultiblockCapability.this.getUnlocalizedName()), content.toString()));
                }
            }
        }.setBackground(new ColorRectTexture(color));
    }

    public IBlockState[] getCandidates(IO io) {
        return scannedStates == null ? scannedStates = scanForCandidates(IO.BOTH) : scannedStates;
    }

    protected final IBlockState[] scanForCandidates(IO io){
        List<IBlockState> states = new ArrayList<>();
        for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            try {
                TileEntity tile = block.createTileEntity(null, block.getDefaultState());
                if (tile != null && isBlockHasCapability(io, tile)) {
                    states.add(block.getDefaultState());
                }
            } catch (Exception ignored) { }
        }
        return states.toArray(new IBlockState[0]);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public BlockComponent getAnyBlock(IO io) {
        return MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(new ResourceLocation(Multiblocked.MODID, name + "." + io.name()));
    }
}
