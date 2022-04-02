package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.FileNode;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TreeListWidget;
import com.cleanroommc.multiblocked.client.MultiblockedResourceLoader;
import com.cleanroommc.multiblocked.util.Size;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ResourceTextureWidget extends DialogWidget {
    private static final int HEIGHT = 128;
    private static final int WIDTH = 184;

    public ResourceTextureWidget(WidgetGroup parent, Consumer<ResourceTexture> result) {
        super(parent, true);
        Size size = parent.getSize();
        int x = 130 + (size.width - 133 - WIDTH) / 2;
        int y = (size.height - HEIGHT) / 2;
        File dir = new File(MultiblockedResourceLoader.location, "assets/multiblocked");
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                close();
            }
        }
        ImageWidget imageWidget = new ImageWidget(x + 67 , y + 39, 50, 50, new ColorBorderTexture(2, -1));
        addWidget(new ImageWidget(0, 0, parent.getSize().width, parent.getSize().height, new ColorRectTexture(0x4f000000)));
        AtomicReference<ResourceTexture> selected = new AtomicReference<>();
        addWidget(new TreeListWidget<>(0, 0, 130, size.height, new FileNode(dir).setValid(DialogWidget.suffixFilter(".png")), node -> {
            if (node != null && node.isLeaf() && node.getContent().isFile()) {
                selected.set(getTextureFromFile(dir, node.getKey()));
                imageWidget.setImage(new GuiTextureGroup(new ColorBorderTexture(2, -1), selected.get()));
            }
        })
                .setNodeTexture(ResourceBorderTexture.BORDERED_BACKGROUND)
                .canSelectNode(true)
                .setLeafTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png")));
        addWidget(new ImageWidget(x, y, WIDTH, HEIGHT, ResourceBorderTexture.BORDERED_BACKGROUND));
        addWidget(new ButtonWidget(x + WIDTH / 2 - 30 - 20, y + HEIGHT - 32, 40, 20, cd -> {
            close();
            if (result != null) result.accept(selected.get());
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("confirm", -1).setDropShadow(true)).setHoverBorderTexture(1, 0xff000000));
        addWidget(new ButtonWidget(x + WIDTH / 2 + 30 - 20, y + HEIGHT - 32, 40, 20, cd -> {
            close();
            if (result != null) result.accept(null);
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("cancel", 0xffff0000).setDropShadow(true)).setHoverBorderTexture(1, 0xff000000));
        addWidget(imageWidget);
        addWidget(new ButtonWidget(x + 15, y + 15, 20, 20, cd -> {
            try {
                Desktop.getDesktop().open(dir.isDirectory() ? dir : dir.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1).setDropShadow(true)).setHoverBorderTexture(1, 0xff000000).setHoverTooltip("open folder"));
        addWidget(new ImageWidget(x + 15, y + 20, WIDTH - 30,10, new TextTexture("Texture", -1).setWidth(WIDTH - 30).setDropShadow(true)));
    }

    private ResourceTexture getTextureFromFile(File path, File r){
        return new ResourceTexture("multiblocked:" + r.getPath().replace(path.getPath(), "").substring(1).replace('\\', '/'));
    }
}
