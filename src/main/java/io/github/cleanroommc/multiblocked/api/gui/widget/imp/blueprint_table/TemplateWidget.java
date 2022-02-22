package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import net.minecraft.item.ItemStack;

public class TemplateWidget extends WidgetGroup {
    ItemStack itemStack;
    public TemplateWidget(ItemStack itemStack) {
        super(0, 0, 330, 256);
        this.itemStack = itemStack;
    }
}
