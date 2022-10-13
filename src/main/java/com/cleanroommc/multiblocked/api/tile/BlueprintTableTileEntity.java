package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.CustomProperties;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.util.ModularUIBuilder;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.BlueprintTableWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.controller.structure.StructurePageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.pattern.FactoryBlockPattern;
import com.cleanroommc.multiblocked.api.pattern.Predicates;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class BlueprintTableTileEntity extends ControllerTileEntity{

    @Override
    public void updateFormed() {
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        recipeLogic = null;
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        if (isFormed()) {
            return new ModularUIBuilder(IGuiTexture.EMPTY, 384, 256)
                    .widget(new BlueprintTableWidget(this))
                    .build(this, entityPlayer);
        } else {
            TabContainer tabContainer = new TabContainer(0, 0, 200, 232);
            new StructurePageWidget(this.definition, tabContainer);
            return new ModularUIBuilder(IGuiTexture.EMPTY, 196, 256)
                    .widget(tabContainer)
                    .build(this, entityPlayer);
        }
    }

    public final static ControllerDefinition tableDefinition = new ControllerDefinition(new ResourceLocation(Multiblocked.MODID, "blueprint_table"), BlueprintTableTileEntity.class);
    public final static PartDefinition partDefinition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "blueprint_table_part"));

    public static void registerBlueprintTable() {
        tableDefinition.getRecipeMap().inputCapabilities.add(MbdCapabilities.ITEM);
        tableDefinition.getBaseStatus().setRenderer(new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/blueprint_table_controller")));
        tableDefinition.getIdleStatus().setRenderer(new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/blueprint_table_formed")));
        tableDefinition.properties.isOpaque = false;

        partDefinition.getBaseStatus().setRenderer(new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/blueprint_table")));
        partDefinition.properties.rotationState = CustomProperties.RotationState.NONE;
        partDefinition.properties.isOpaque = false;

        tableDefinition.setBasePattern(FactoryBlockPattern.start()
                .aisle("PPP", "C  ")
                .aisle("PTP", "   ")
                .where(' ', Predicates.any())
                .where('T', Predicates.component(tableDefinition))
                .where('P', Predicates.component(partDefinition).disableRenderFormed())
                .where('C', Predicates.anyCapability(MbdCapabilities.ITEM).disableRenderFormed())
                .build());
        MbdComponents.registerComponent(tableDefinition);
        MbdComponents.registerComponent(partDefinition);
    }
}
