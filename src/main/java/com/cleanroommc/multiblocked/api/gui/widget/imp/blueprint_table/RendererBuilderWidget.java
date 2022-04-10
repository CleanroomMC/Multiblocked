package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ShaderTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IParticleWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IRendererWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IShaderWidget;
import com.cleanroommc.multiblocked.client.shader.Shaders;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RendererBuilderWidget extends WidgetGroup {
    public final ShaderTexture shaderTexture;

    public RendererBuilderWidget() {
        super(0, 0, 384, 256);
        setClientSideWidget();
        this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_page.png")));
        this.addWidget(new ButtonWidget(40, 40, 40, 40, new ItemStackTexture(new ItemStack(Blocks.BEACON)), this::renderer).setHoverBorderTexture(1, -1).setHoverTooltip("IRenderer Helper"));
        this.addWidget(new ButtonWidget(90, 40, 40, 40, new ResourceTexture("multiblocked:textures/fx/fx.png"), this::particle).setHoverBorderTexture(1, -1).setHoverTooltip("Particle Helper"));
        this.addWidget(new ButtonWidget(140, 40, 40, 40, shaderTexture = ShaderTexture.createShader(new ResourceLocation(Multiblocked.MODID, "fbm")), this::shader).setHoverBorderTexture(1, -1).setHoverTooltip("Shader Helper"));
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

    @Override
    public void setGui(ModularUI gui) {
        super.setGui(gui);
        if (gui != null && shaderTexture != null) {
            gui.registerCloseListener(shaderTexture::dispose);
        }
    }

}
