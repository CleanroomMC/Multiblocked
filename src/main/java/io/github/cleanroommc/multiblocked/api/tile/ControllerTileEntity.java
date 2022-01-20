package io.github.cleanroommc.multiblocked.api.tile;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.mc1120.player.MCPlayer;
import crafttweaker.mc1120.world.MCFacing;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import io.github.cleanroommc.multiblocked.gui.factory.TileEntityUIFactory;
import io.github.cleanroommc.multiblocked.gui.modular.ModularUI;
import io.github.cleanroommc.multiblocked.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.gui.util.ModularUIBuilder;
import io.github.cleanroommc.multiblocked.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.gui.widget.imp.controller.IOPageWidget;
import io.github.cleanroommc.multiblocked.gui.widget.imp.tab.TabButton;
import io.github.cleanroommc.multiblocked.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A TileEntity that defies all controller machines.
 *
 * Head of the multiblock.
 */
@ZenClass("mods.multiblocked.tile.Controller")
@ZenRegister
public class ControllerTileEntity extends ComponentTileEntity<ControllerDefinition>{
    public MultiblockState state;
    protected Table<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilities;
    private Map<Long, Map<MultiblockCapability, IO>> settings;
    protected LongOpenHashSet parts;
    @ZenProperty
    protected RecipeLogic recipeLogic;

    public ControllerTileEntity() {
    }

    @ZenMethod
    public boolean checkPattern() {
        if (state == null) return false;
        return definition.basePattern.checkPatternAt(state);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return definition.allowRotate && facing.getAxis() != EnumFacing.Axis.Y;
    }

    @ZenGetter
    public boolean isFormed() {
        return state != null && state.isFormed();
    }

    public void updateFormed() {
        if (recipeLogic != null) recipeLogic.update();
    }

    public Table<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> getCapabilities() {
        return capabilities;
    }

    /**
     * Called when its formed, server side only.
     */
    public void onStructureFormed() {
        recipeLogic = new RecipeLogic(this);
        // init capabilities
        Map<Long, EnumMap<IO, Set<MultiblockCapability>>> capabilityMap = state.getMatchContext().get("capabilities");
        if (capabilityMap != null) {
            capabilities = Tables.newCustomTable(new EnumMap<>(IO.class), Object2ObjectOpenHashMap::new);
            for (Map.Entry<Long, EnumMap<IO, Set<MultiblockCapability>>> entry : capabilityMap.entrySet()) {
                TileEntity tileEntity = world.getTileEntity(BlockPos.fromLong(entry.getKey()));
                if (tileEntity != null) {
                    if (settings != null) {
                        Map<MultiblockCapability, IO> caps = settings.get(entry.getKey());
                        if (caps != null) {
                            for (Map.Entry<MultiblockCapability, IO> ioEntry : caps.entrySet()) {
                                IO io = ioEntry.getValue();
                                MultiblockCapability capability = ioEntry.getKey();
                                if (io == null) continue;
                                if (!capabilities.contains(io, capability)) {
                                    capabilities.put(io, capability, new Long2ObjectOpenHashMap<>());
                                }
                                capabilities.get(io, capability).put(entry.getKey().longValue(), capability.createProxy(io, tileEntity));
                            }
                        }
                    } else {
                        entry.getValue().forEach((io,set)->{
                            for (MultiblockCapability capability : set) {
                                if (capability.isBlockHasCapability(io, tileEntity)) {
                                    if (!capabilities.contains(io, capability)) {
                                        capabilities.put(io, capability, new Long2ObjectOpenHashMap<>());
                                    }
                                    capabilities.get(io, capability).put(entry.getKey().longValue(), capability.createProxy(io, tileEntity));
                                }
                            }
                        });
                    }
                }
            }
        }

        settings = null;

        // init parts
        parts = state.getMatchContext().get("parts");
        if (parts != null) {
            for (Long pos : parts) {
                TileEntity tileEntity = world.getTileEntity(BlockPos.fromLong(pos));
                if (tileEntity instanceof PartTileEntity) {
                    ((PartTileEntity<?>) tileEntity).addedToController(this);
                }
            }
        }

        writeCustomData(-1, this::writeState);
        if (definition.structureFormed != null) {
            definition.structureFormed.apply(this);
        }
    }

    public void onStructureInvalid() {
        recipeLogic = null;
        // invalid parts
        if (parts != null) {
            for (Long pos : parts) {
                TileEntity tileEntity = world.getTileEntity(BlockPos.fromLong(pos));
                if (tileEntity instanceof PartTileEntity) {
                    ((PartTileEntity<?>) tileEntity).removedFromController(this);
                }
            }
            parts = null;
        }
        capabilities = null;

        writeCustomData(-1, this::writeState);
        if (definition.structureInvalid != null) {
            definition.structureInvalid.apply(this);
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == -1) {
            readState(buf);
            scheduleChunkForRenderUpdate();
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        writeState(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readState(buf);
        scheduleChunkForRenderUpdate();
    }

    private void writeState(PacketBuffer buffer) {
        buffer.writeBoolean(isFormed());
    }

    private void readState(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            state = new MultiblockState(world, pos);
        } else {
            state = null;
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("recipeLogic")) {
            recipeLogic = new RecipeLogic(this);
            recipeLogic.readFromNBT(compound.getCompoundTag("recipeLogic"));
        }
        if (compound.hasKey("capabilities")) {
            NBTTagList tagList = compound.getTagList("capabilities", Constants.NBT.TAG_COMPOUND);
            settings = new HashMap<>();
            for (NBTBase base : tagList) {
                NBTTagCompound tag = (NBTTagCompound) base;
                settings.computeIfAbsent(tag.getLong("pos"), l->new HashMap<>())
                        .put(MultiblockCapabilities.get(tag.getString("cap")),
                                IO.VALUES[tag.getInteger("io")]);
            }
        }
        state = MultiblockWorldSavedData.getOrCreate(world).mapping.get(pos);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (recipeLogic != null) compound.setTag("recipeLogic", recipeLogic.writeToNBT(new NBTTagCompound()));
        if (capabilities != null) {
            NBTTagList tagList = new NBTTagList();
            for (Table.Cell<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> cell : capabilities.cellSet()) {
                IO io = cell.getRowKey();
                MultiblockCapability cap = cell.getColumnKey();
                Long2ObjectOpenHashMap<CapabilityProxy<?>> value = cell.getValue();
                if (io != null && cap != null && value != null) {
                    for (long posLong : value.keySet()) {
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setInteger("io", io.ordinal());
                        tag.setString("cap", cap.name);
                        tag.setLong("pos", posLong);
                        tagList.appendTag(tag);
                    }
                }
            }
            compound.setTag("capabilities", tagList);
        }
        return compound;
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (definition.onRightClick != null) {
            if (definition.onRightClick.apply(this, new MCPlayer(player), new MCFacing(facing), hitX, hitY, hitZ)) return true;
        }
        if (!world.isRemote) {
            if (!isFormed()) {
                if (state == null) state = new MultiblockState(world, pos);
                ItemStack held = player.getHeldItem(hand);
                if (definition.catalyst == null || held.isItemEqual(definition.catalyst)) {
                    if (checkPattern()) { // formed
                        player.swingArm(hand);
                        ITextComponent formedMsg = new TextComponentTranslation("multiblocked.multiblock.formed", getLocalizedName());
                        player.sendStatusMessage(formedMsg, true);
                        if (!player.isCreative() && definition.consumeCatalyst) {
                            held.shrink(1);
                        }
                        MultiblockWorldSavedData.getOrCreate(world).addMapping(state);
                        onStructureFormed();
                        return true;
                    }
                }
            }
            if (!player.isSneaking()) {
                if (!world.isRemote && player instanceof EntityPlayerMP) {
                    return TileEntityUIFactory.INSTANCE.openUI(this, (EntityPlayerMP) player);
                }
            }
        }
        return false;
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        TabContainer tabContainer = new TabContainer(0, 0, 200, 232);
        if (isFormed()) {
            tabContainer.addTab(
                    new TabButton(0, 0, 20, 20).setTexture(new ColorRectTexture(-1), new ColorRectTexture(0xff000000)),
                    new IOPageWidget(this));
        }
        tabContainer.addTab(new TabButton(0, 20, 20, 20)
                .setTexture(new ColorRectTexture(-1), new ColorRectTexture(0xff000000)),
                new WidgetGroup(20,0,200,135)).addWidget(new LabelWidget(10, 0, ()->"tab2").setTextColor(-1));
        tabContainer.addTab(new TabButton(0, 40, 20, 20)
                .setTexture(new ColorRectTexture(-1), new ColorRectTexture(0xff000000)),
                new WidgetGroup(20,0,200,135)).addWidget(new LabelWidget(10, 0, ()->"tab3").setTextColor(-1));
        return new ModularUIBuilder(IGuiTexture.EMPTY, 196, 256)
                .widget(tabContainer)
                .build(this, entityPlayer);
    }
}
