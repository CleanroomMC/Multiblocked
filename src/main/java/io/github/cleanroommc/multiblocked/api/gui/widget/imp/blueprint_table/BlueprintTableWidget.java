package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import io.github.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockedItems;
import io.github.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;

public class BlueprintTableWidget extends TabContainer {
    private static final ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/blueprint_page.png");

    public BlueprintTableWidget(BlueprintTableTileEntity table) {
        super(0, 0, 330, 256);
        addWidget(0, new ImageWidget(0, 0, 330, 256, PAGE.getSubTexture(0, 0, 330 / 512., 1)));
        addTab(new TabButton(6, 6, 18, 18)
                .setTexture(PAGE.getSubTexture(330 / 512., 0, 18 / 512., 18 / 256.), PAGE.getSubTexture(330 / 512., 18 / 256., 18 / 512., 18 / 256.)),
                new BlueprintWidget(table));
        addTab(new TabButton(36, 6, 18, 18)
                        .setTexture(PAGE.getSubTexture(330 / 512., 0, 18 / 512., 18 / 256.), PAGE.getSubTexture(330 / 512., 18 / 256., 18 / 512., 18 / 256.)),
                new BlueprintWidget(table));
        addTab(new TabButton(66, 6, 18, 18)
                        .setTexture(PAGE.getSubTexture(330 / 512., 0, 18 / 512., 18 / 256.), PAGE.getSubTexture(330 / 512., 18 / 256., 18 / 512., 18 / 256.)),
                new BlueprintWidget(table));
        addWidget(new ImageWidget(7, 7, 16, 16, new ItemStackTexture(MultiblockedItems.BLUEPRINT)));
        addWidget(new ImageWidget(37, 7, 16, 16, new ItemStackTexture(MultiblockedItems.BUILDER)));
        addWidget(new ImageWidget(67, 7, 16, 16, new ItemStackTexture(BlueprintTableTileEntity.tableDefinition.getStackForm())));
    }
}
