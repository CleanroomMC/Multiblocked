package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IParticleWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IRendererWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IShaderWidget;
import com.cleanroommc.multiblocked.api.registry.MultiblockedItems;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import com.cleanroommc.multiblocked.client.shader.Shaders;
import net.minecraft.init.Items;

public class RendererBuilderWidget extends WidgetGroup {

    public RendererBuilderWidget() {
        super(0, 0, 384, 256);
        setClientSideWidget();
        if (!Multiblocked.isClient()) return;
        this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_page.png")));
        this.addWidget(new ButtonWidget(40, 40, 40, 40, new ItemStackTexture(MultiblockedItems.BUILDER), this::renderer).setHoverBorderTexture(1, -1).setHoverTooltip("IRenderer Helper"));
        this.addWidget(new ButtonWidget(90, 40, 40, 40, new ItemStackTexture(Items.PAPER), this::particle).setHoverBorderTexture(1, -1).setHoverTooltip("Particle Helper"));
        this.addWidget(new ButtonWidget(140, 40, 40, 40, new ItemStackTexture(BlueprintTableTileEntity.tableDefinition.getStackForm()), this::shader).setHoverBorderTexture(1, -1).setHoverTooltip("Shader Helper"));
    }

    private void shader(ClickData clickData) {
        new IShaderWidget(this, Shaders.FBM.source);
    }

    private void particle(ClickData clickData) {
        new IParticleWidget(this);
    }

    private void renderer(ClickData clickData) {
        new IRendererWidget(this, null, null);
    }

}
