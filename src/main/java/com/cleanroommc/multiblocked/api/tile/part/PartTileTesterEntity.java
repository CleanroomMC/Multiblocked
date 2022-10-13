package com.cleanroommc.multiblocked.api.tile.part;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.util.ModularUIBuilder;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tester.PartScriptWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tester.ZSScriptWidget;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;

public class PartTileTesterEntity extends PartTileEntity<PartDefinition> {
    public final static PartDefinition DEFAULT_DEFINITION = new PartDefinition(new ResourceLocation("multiblocked:part_tester"), PartTileTesterEntity.class);

    @Override
    public void setDefinition(ComponentDefinition definition) {
        if (definition == null) {
            definition = DEFAULT_DEFINITION;
        } else if (definition != DEFAULT_DEFINITION && world != null) {
            if (isRemote()) {
                scheduleChunkForRenderUpdate();
            } else {
                notifyBlockUpdate();
                super.setDefinition(definition);
                if (needAlwaysUpdate()) {
                    MultiblockWorldSavedData.getOrCreate(world).addLoading(this);
                }
                return;
            }
        }
        super.setDefinition(definition);
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        super.onRightClick(player, hand, facing, hitX, hitY, hitZ);
        return true;
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        if (Multiblocked.isClient() && Multiblocked.isSinglePlayer()) {
            TabContainer tabContainer = new TabContainer(0, 0, 200, 232);
            new PartScriptWidget(this, tabContainer);
            if (Loader.isModLoaded(Multiblocked.MODID_CT)) {
                new ZSScriptWidget(tabContainer);
            }
            if (getDefinition() != DEFAULT_DEFINITION) {
                if (!traits.isEmpty()) initTraitUI(tabContainer, entityPlayer);
            }
            return new ModularUIBuilder(IGuiTexture.EMPTY, 196, 256)
                    .widget(tabContainer)
                    .build(this, entityPlayer);
        }
        return null;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        compound.setString("loc", DEFAULT_DEFINITION.location.toString());
        super.readFromNBT(compound);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setString("loc", DEFAULT_DEFINITION.location.toString());
        return compound;
    }

    public static void registerTestPart() {
        DEFAULT_DEFINITION.getBaseStatus().setRenderer(new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/part_tester")));
        DEFAULT_DEFINITION.properties.isOpaque = false;
        MbdComponents.registerComponent(DEFAULT_DEFINITION);
    }
}
