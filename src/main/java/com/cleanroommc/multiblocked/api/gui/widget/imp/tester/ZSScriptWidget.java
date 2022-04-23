package com.cleanroommc.multiblocked.api.gui.widget.imp.tester;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.CTHelper;
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
import com.cleanroommc.multiblocked.util.FileUtility;
import net.minecraft.network.PacketBuffer;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class ZSScriptWidget extends PageWidget {

    private static final ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/ct_loader_page.png");
    private final DraggableScrollableWidgetGroup zsList;
    private final TextBoxWidget textBox;
    private final DraggableScrollableWidgetGroup tfGroup;
    private File selected;

    public ZSScriptWidget(TabContainer tabContainer) {
        super(PAGE, tabContainer); //176, 256
        this.addWidget(new ImageWidget(5, 5, 176 - 10, 150 - 55, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        this.addWidget(zsList = new DraggableScrollableWidgetGroup(10, 10, 176 - 20, 150 - 10 - 55).setBackground(new ColorRectTexture(0xff000000)));
        this.addWidget(new ButtonWidget(5, 105, 20, 20, new ResourceTexture("multiblocked:textures/gui/save.png"), cd->{
            if (!cd.isRemote) return;
            try {
                File dir = new File("scripts");
                Desktop.getDesktop().open(dir.isDirectory() ? dir : dir.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).setHoverBorderTexture(1, -1).setHoverTooltip("open folder"));
        this.addWidget(new ButtonWidget(30, 105, 140, 20, null, this::loadZS).setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("load script", -1).setDropShadow(true)).setHoverBorderTexture(1, -1));
        tfGroup = new DraggableScrollableWidgetGroup(5, 130, 176 - 10, 120)
                .setBackground(new ColorRectTexture(0xaf444444))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1));
        textBox = new TextBoxWidget(0, 0, 176 - 14, Collections.singletonList("")).setFontColor(-1).setShadow(true);
        tfGroup.addWidget(textBox);
        this.addWidget(tfGroup);
        updateList();
    }

    private void loadZS(ClickData clickData) {
        if (selected != null) {
            try {
                String zs = FileUtility.readInputStream(new FileInputStream(selected));
                if (!CTHelper.executeDynamicScript(zs)) {
                    zs = CTHelper.getError() == null ? "error" : CTHelper.getError();
                }
                textBox.setContent(Collections.singletonList(zs));
                tfGroup.computeMax();
            } catch (IOException exception) {
                Multiblocked.LOGGER.error("tester: loading ct", exception);
            }
        }
    }

    private void updateList() {
        zsList.clearAllWidgets();
        selected = null;
        File path = new File("scripts");
        if (!path.isDirectory()) {
            if (!path.mkdirs()) {
                return;
            }
        }
        for (File file : Optional.ofNullable(path.listFiles()).orElse(new File[0])) {
            if (file.isFile() && file.getName().endsWith(".zs")) {
                zsList.addWidget(new SelectableWidgetGroup(0, 1 + zsList.widgets.size() * 11, zsList
                        .getSize().width, 10)
                        .setSelectedTexture(-1, -1)
                        .setOnSelected(W -> {
                            selected = file;
                            writeClientAction(-1, buffer -> {
                                buffer.writeBoolean(selected != null);
                                if (selected != null) {
                                    buffer.writeString(file.getName());
                                }
                            });
                        })
                        .addWidget(new ImageWidget(0, 0, zsList.getSize().width, 10, new ColorRectTexture(0xff000000)))
                        .addWidget(new ImageWidget(0, 0, zsList.getSize().width, 10, new TextTexture(file.getName().replace(".zs", "")).setWidth(
                                zsList.getSize().width).setType(TextTexture.TextType.ROLL))));
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            if (buffer.readBoolean()) {
                selected = new File(new File("scripts"), buffer.readString(Short.MAX_VALUE));
            } else {
                selected = null;
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }
}
