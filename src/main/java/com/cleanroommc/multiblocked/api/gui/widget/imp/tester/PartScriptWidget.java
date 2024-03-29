package com.cleanroommc.multiblocked.api.gui.widget.imp.tester;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextBoxWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.controller.PageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.part.PartTileTesterEntity;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.gson.JsonElement;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class PartScriptWidget extends PageWidget {

    private static final ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/json_loader_page.png");
    private final PartTileTesterEntity part;
    private final DraggableScrollableWidgetGroup jsonList;
    private final TextBoxWidget textBox;
    private final DraggableScrollableWidgetGroup tfGroup;
    private File selected;

    public PartScriptWidget(PartTileTesterEntity part, TabContainer tabContainer) {
        super(PAGE, tabContainer); //176, 256
        this.part = part;
        this.addWidget(new ImageWidget(5, 5, 176 - 10, 150 - 55, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        this.addWidget(jsonList = new DraggableScrollableWidgetGroup(10, 10, 176 - 20, 150 - 10 - 55).setBackground(new ColorRectTexture(0xff000000)));
        this.addWidget(new ButtonWidget(5, 105, 20, 20, new ResourceTexture("multiblocked:textures/gui/save.png"), cd->{
            if (!cd.isRemote) return;
            try {
                File dir = new File(Multiblocked.location, "definition/part");
                Desktop.getDesktop().open(dir.isDirectory() ? dir : dir.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).setHoverBorderTexture(1, -1).setHoverTooltip("open folder"));
        this.addWidget(new ButtonWidget(30, 105, 140, 20, null, this::loadJson).setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("load script", -1).setDropShadow(true)).setHoverBorderTexture(1, -1));
        tfGroup = new DraggableScrollableWidgetGroup(5, 130, 176 - 10, 120)
                .setBackground(new ColorRectTexture(0xaf444444))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1));
        textBox = new TextBoxWidget(0, 0, 176 - 14, Collections.singletonList("")).setFontColor(-1).setShadow(true);
        tfGroup.addWidget(textBox);
        this.addWidget(tfGroup);
        updateList();
    }

    private void loadJson(ClickData clickData) {
        if (selected != null && clickData.isRemote) {
            JsonElement jsonElement = FileUtility.loadJson(selected);
            if (jsonElement != null) {
                try {
                    PartDefinition definition = new PartDefinition(new ResourceLocation(jsonElement.getAsJsonObject().get("location").getAsString()));
                    definition.fromJson(jsonElement.getAsJsonObject());
                    MbdComponents.DEFINITION_REGISTRY.put(definition.location, definition);
                    writeClientAction(-1, buffer -> buffer.writeString(definition.location.toString()));
                } catch (Exception e) {
                    Multiblocked.LOGGER.error("tester: error while loading the part json {}", selected.getName(), e);
                }
                textBox.setContent(Collections.singletonList(Multiblocked.GSON_PRETTY.toJson(jsonElement)));
                tfGroup.computeMax();
            }
        }
    }

    private void updateList() {
        jsonList.clearAllWidgets();
        selected = null;
        File path = new File(Multiblocked.location, "definition/part");
        if (!path.isDirectory()) {
            if (!path.mkdirs()) {
                return;
            }
        }
        for (File file : Optional.ofNullable(path.listFiles()).orElse(new File[0])) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                jsonList.addWidget(new SelectableWidgetGroup(0, 1 + jsonList.widgets.size() * 11, jsonList
                        .getSize().width, 10)
                        .setSelectedTexture(-1, -1)
                        .setOnSelected(W -> selected = file)
                        .addWidget(new ImageWidget(0, 0, jsonList.getSize().width, 10, new ColorRectTexture(0xff000000)))
                        .addWidget(new ImageWidget(0, 0, jsonList.getSize().width, 10, new TextTexture(file.getName().replace(".json", "")).setWidth(
                                jsonList.getSize().width).setType(TextTexture.TextType.ROLL))));
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            part.setDefinition(MbdComponents.DEFINITION_REGISTRY.get(new ResourceLocation(buffer.readString(Short.MAX_VALUE))));
        } else {
            super.handleClientAction(id, buffer);
        }
    }
}
