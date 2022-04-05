package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.api.gui.texture.*;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;

import java.util.Collections;

public class IShaderWidget extends DialogWidget {
    public final ShaderTexture shaderTexture;

    public IShaderWidget(WidgetGroup parent, String init) {
        super(parent, true);
        this.addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ColorRectTexture(0xaf000000)));
        this.addWidget(new ImageWidget(35, 59, 138, 138,
                new GuiTextureGroup(new ColorBorderTexture(3, -1),
                        shaderTexture = ShaderTexture.createRawShader(init))));
        TextFieldWidget textFieldWidget = new TextFieldWidget(181, 55, 120, 20, true, null, null).setAllowEnter(true).setCurrentString(init);
        DraggableScrollableWidgetGroup tfGroup = new DraggableScrollableWidgetGroup(181, 80, 170, 120)
                .setBackground(new ColorRectTexture(0x3faaaaaa))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1));
        TextBoxWidget textBox = new TextBoxWidget(0, 0, 165, Collections.singletonList(init)).setFontColor(-1).setShadow(true);

        this.addWidget(tfGroup);
        this.addWidget(textFieldWidget);
        this.addWidget(new ButtonWidget(305, 55, 40, 20, cd -> {
            try {
                textBox.setContent(Collections.singletonList(textFieldWidget.getCurrentString()));
                shaderTexture.updateRawShader(textFieldWidget.getCurrentString());
            } catch (Throwable e) {
                textBox.setContent(Collections.singletonList(e.getMessage()));
            }
            tfGroup.computeMax();
        })
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("update", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("update"));
        tfGroup.addWidget(textBox);
    }

}
