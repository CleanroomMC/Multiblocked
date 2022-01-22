package io.github.cleanroommc.multiblocked.api.tile;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
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
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class BlueprintTableTileEntity extends ControllerTileEntity{

    @Override
    public void updateFormed() {
        MultiblockWorldSavedData.getOrCreate(getWorld()).removeLoading(this);
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
    
    public static ControllerDefinition definition = new ControllerDefinition(new ResourceLocation(Multiblocked.MODID, "blueprint_table"), new RecipeMap("blueprint_table"));

    static {
        definition.recipeMap.inputCapabilities.add(MultiblockCapabilities.ITEM);
        definition.baseRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/blueprint_table"));
        definition.formedRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/blueprint_table_formed"));
        definition.isOpaqueCube = false;
        definition.basePattern = FactoryBlockPattern.start()
                .aisle("T", "C")
                .where('T', definition.selfPredicate())
                .where('C', Predicates.anyCapability(IO.IN, MultiblockCapabilities.ITEM))
                .build();
    }

}
