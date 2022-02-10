package io.github.cleanroommc.multiblocked.api.tile;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.ModularUIBuilder;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.controller.structure.StructurePageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.api.pattern.FactoryBlockPattern;
import io.github.cleanroommc.multiblocked.api.pattern.Predicates;
import io.github.cleanroommc.multiblocked.api.recipe.RecipeMap;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
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
        TabContainer tabContainer = new TabContainer(0, 0, 200, 232);
        new StructurePageWidget(definition, tabContainer);
        return new ModularUIBuilder(IGuiTexture.EMPTY, 196, 256)
                .widget(tabContainer)
                .build(this, entityPlayer);
    }

    public static ControllerDefinition tableDefinition;
    public static PartDefinition partDefinition;

    public static void registerBlueprintTable() {
        tableDefinition = new ControllerDefinition(
                new ResourceLocation(Multiblocked.MODID, "blueprint_table"),
                new RecipeMap("blueprint_table"),
                BlueprintTableTileEntity.class);
        tableDefinition.recipeMap.inputCapabilities.add(MultiblockCapabilities.ITEM);
        tableDefinition.baseRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/blueprint_table_controller"))
                .setRenderLayer(BlockRenderLayer.SOLID, true);
        tableDefinition.formedRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/blueprint_table_formed"))
                .setRenderLayer(BlockRenderLayer.SOLID, true);
        tableDefinition.isOpaqueCube = false;
        tableDefinition.disableOthersRendering = true;
        tableDefinition.noNeedCatalyst = true;


        partDefinition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "blueprint_table_part"));
        partDefinition.baseRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/blueprint_table"))
                .setRenderLayer(BlockRenderLayer.SOLID, true);
        partDefinition.allowRotate = false;
        partDefinition.isOpaqueCube = false;

        tableDefinition.basePattern = FactoryBlockPattern.start()
                .aisle("PPP", "C  ")
                .aisle("PTP", "   ")
                .where(' ', Predicates.any())
                .where('T', tableDefinition.selfPredicate(true))
                .where('P', partDefinition.selfPredicate())
                .where('C', Predicates.anyCapability(IO.IN, MultiblockCapabilities.ITEM))
                .build();

        MultiblockComponents.registerComponent(tableDefinition);
        MultiblockComponents.registerComponent(partDefinition);
    }
}
