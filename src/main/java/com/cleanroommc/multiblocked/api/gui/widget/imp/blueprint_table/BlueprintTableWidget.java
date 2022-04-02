package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.registry.MultiblockedItems;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import net.minecraft.init.Items;

public class BlueprintTableWidget extends WidgetGroup {
    public final BlueprintTableTileEntity table;

    public BlueprintTableWidget(BlueprintTableTileEntity table) {
        super(0, 0, 384, 256);
        this.table = table;
        this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_table.png")));
        this.addWidget(new ButtonWidget(40, 40, 40, 40, new ItemStackTexture(MultiblockedItems.BUILDER), this::templateBuilder).setHoverBorderTexture(1, -1).setHoverTooltip("builder template"));
        this.addWidget(new ButtonWidget(90, 40, 40, 40, new ItemStackTexture(Items.PAPER), this::recipeMapBuilder).setHoverBorderTexture(1, -1).setHoverTooltip("recipe map"));
        this.addWidget(new ButtonWidget(140, 40, 40, 40, new ItemStackTexture(BlueprintTableTileEntity.tableDefinition.getStackForm()), this::controllerBuilder).setHoverBorderTexture(1, -1).setHoverTooltip("controller block"));
        this.addWidget(new ButtonWidget(40, 90, 40, 40, new ItemStackTexture(BlueprintTableTileEntity.partDefinition.getStackForm()), this::partBuilder).setHoverBorderTexture(1, -1).setHoverTooltip("part block"));
    }

    private void partBuilder(ClickData clickData) {
        widgets.forEach(this::waitToRemoved);
        this.addWidget(0, new PartBuilderWidget());
    }

    private void recipeMapBuilder(ClickData clickData) {
        widgets.forEach(this::waitToRemoved);
        WidgetGroup group = new WidgetGroup(0, 0, getSize().width, getSize().height).setClientSideWidget();
        this.addWidget(0, group);
        if (Multiblocked.isClient()) {
            group.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_page.png")));
            group.addWidget(new RecipeMapBuilderWidget(this, 200, 31, 150, 188));
        }
    }

    private void controllerBuilder(ClickData clickData) {
        widgets.forEach(this::waitToRemoved);
        this.addWidget(0, new ControllerBuilderWidget(table));
    }

    private void templateBuilder(ClickData clickData) {
        widgets.forEach(this::waitToRemoved);
        this.addWidget(0, new TemplateBuilderWidget(table));
    }

}
