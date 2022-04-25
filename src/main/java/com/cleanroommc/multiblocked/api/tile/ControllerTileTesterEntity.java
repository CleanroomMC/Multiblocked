package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.util.ModularUIBuilder;
import com.cleanroommc.multiblocked.api.gui.widget.imp.controller.IOPageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.controller.RecipePage;
import com.cleanroommc.multiblocked.api.gui.widget.imp.controller.structure.StructurePageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tester.ControllerScriptWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tester.ZSScriptWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.pattern.FactoryBlockPattern;
import com.cleanroommc.multiblocked.api.pattern.Predicates;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;

public class ControllerTileTesterEntity extends ControllerTileEntity {
    public final static ControllerDefinition DEFAULT_DEFINITION = new ControllerDefinition(new ResourceLocation("multiblocked:controller_tester"), ControllerTileTesterEntity.class);

    @Override
    public void setDefinition(ComponentDefinition definition) {
        MultiblockWorldSavedData mwsd = MultiblockWorldSavedData.getOrCreate(world);
        state = null;
        if (pos != null && mwsd.mapping.containsKey(pos)) {
            mwsd.removeMapping(mwsd.mapping.get(pos));
        }
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
    public void onStructureFormed() {
        super.onStructureFormed();
        recipeLogic = new RecipeLogic(this);
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        if (Multiblocked.isClient() && Multiblocked.isSinglePlayer()) {
            TabContainer tabContainer = new TabContainer(0, 0, 200, 232);
            new ControllerScriptWidget(this, tabContainer);
            if (Loader.isModLoaded(Multiblocked.MODID_CT)) {
                new ZSScriptWidget(tabContainer);
            }
            if (getDefinition() != DEFAULT_DEFINITION) {
                if (!traits.isEmpty()) initTraitUI(tabContainer, entityPlayer);
                if (isFormed()) {
                    new RecipePage(this, tabContainer);
                    new IOPageWidget(this, tabContainer);
                } else {
                    new StructurePageWidget(this.definition, tabContainer);
                }
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

    public static void registerTestController() {
        DEFAULT_DEFINITION.baseRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/controller_tester"));
        DEFAULT_DEFINITION.isOpaqueCube = false;
        DEFAULT_DEFINITION.basePattern = FactoryBlockPattern.start()
                .aisle("@")
                .where('@', Predicates.component(DEFAULT_DEFINITION))
                .build();
        MbdComponents.registerComponent(DEFAULT_DEFINITION);
    }
}
