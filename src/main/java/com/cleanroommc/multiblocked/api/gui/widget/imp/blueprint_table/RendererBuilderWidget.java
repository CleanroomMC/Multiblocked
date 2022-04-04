package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IParticleWidget;

public class RendererBuilderWidget extends WidgetGroup {

    public RendererBuilderWidget() {
        super(0, 0, 384, 256);
        setClientSideWidget();
        if (!Multiblocked.isClient()) return;
        this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_page.png")));
        new IParticleWidget(this);
    }

}
