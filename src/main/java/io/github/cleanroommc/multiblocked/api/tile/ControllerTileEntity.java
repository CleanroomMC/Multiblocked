package io.github.cleanroommc.multiblocked.api.tile;

import io.github.cleanroommc.multiblocked.api.multiblock.MultiblockDefinition;
import io.github.cleanroommc.multiblocked.api.pattern.BlockPattern;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class ControllerTileEntity extends ComponentTileEntity{
    public final MultiblockDefinition definition;
    public MultiblockState state;
    public BlockPattern pattern;

    public ControllerTileEntity(ResourceLocation location, MultiblockDefinition definition) {
        super(location);
        this.definition = definition;
    }

    public boolean checkPattern() {
        if (pattern == null) initPattern();
        if (state == null) return false;
        return pattern.checkPatternAt(state);
    }

    public void initPattern() {
        pattern = definition.patternSupplier.getPattern(this);
    }

    public boolean isFormed() {
        return state != null && state.isFormed();
    }

    public void updateFormed() {
    }

    public void onStructureFormed() {
        if (definition.structureFormed != null) {
            definition.structureFormed.onStructureFormed(this);
        }
    }

    public void onStructureInvalid() {
        if (definition.structureInvalid != null) {
            definition.structureInvalid.onStructureInvalid(this);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isFormed());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            state = new MultiblockState(world, pos);
        } else {
            state = null;
        }
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        state = MultiblockWorldSavedData.getOrCreate(world).mapping.get(pos);
    }

    @Nonnull
    @Override
    public NBTTagCompound serializeNBT() {
        return super.serializeNBT();
    }

    @Override
    public ComponentTileEntity createNewTileEntity() {
        return new ControllerTileEntity(getLocation(), definition);
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            if (state == null) state = new MultiblockState(world, pos);
            ItemStack held = player.getHeldItem(hand);
            if (held.isItemEqual(definition.getCatalyst())) {
                if (checkPattern()) { // formed
                    player.swingArm(hand);
                    ITextComponent formedMsg = new TextComponentTranslation("multiblocked.multiblock.formed", getLocalizedName());
                    player.sendStatusMessage(formedMsg, true);
                    if (!player.isCreative() && definition.consumeCatalyst) {
                        held.shrink(1);
                    }
                    onStructureFormed();
                    MultiblockWorldSavedData.getOrCreate(world).addMapping(state);
                }
            }
        }
        return false;
    }
}
