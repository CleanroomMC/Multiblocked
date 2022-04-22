package com.cleanroommc.multiblocked.common.capability.widget;

import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.PhantomSlotWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import com.cleanroommc.multiblocked.util.CycleItemStackHandler;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemsContentWidget extends ContentWidget<ItemsIngredient> {
    protected CycleItemStackHandler itemHandler;

    @Override
    protected void onContentUpdate() {
        List<List<ItemStack>> stacks = Collections.singletonList(Arrays.stream(content.getMatchingStacks()).map(stack -> {
            ItemStack copy = stack.copy();
            copy.setCount(content.getAmount());
            return copy;
        }).collect(Collectors.toList()));
        if (itemHandler != null) {
            itemHandler.updateStacks(stacks);
        } else {
            itemHandler = new CycleItemStackHandler(stacks);
            addWidget(new SlotWidget(itemHandler, 0, 1, 1, false, false).setDrawOverlay(false).setOnAddedTooltips((s, l)-> {
                if (chance < 1) {
                    l.add(chance == 0 ? LocalizationUtils.format("multiblocked.gui.content.chance_0") : LocalizationUtils.format("multiblocked.gui.content.chance_1", String.format("%.1f", chance * 100)));
                    if (perTick) {
                        l.add(LocalizationUtils.format("multiblocked.gui.content.tips.per_tick"));
                    }
                }
            }));
        }
    }

    @Override
    public ItemsIngredient getJEIContent(Object content) {
        if (content instanceof ItemStack) {
            return new ItemsIngredient(this.content.getAmount(), (ItemStack) content);
        }
        return null;
    }

    @Override
    public Object getJEIIngredient(ItemsIngredient content) {
        return itemHandler.getStackInSlot(0);
    }

    @Override
    public void openConfigurator(WidgetGroup dialog) {
        super.openConfigurator(dialog);
        int x = 5;
        int y = 25;
        dialog.addWidget(new LabelWidget(5, y + 3, "multiblocked.gui.label.amount"));
        dialog.addWidget(new TextFieldWidget(125 - 60, y, 60, 15, true, null, number -> {
            content = content.isOre() ? new ItemsIngredient(Integer.parseInt(number), content.getOreDict()) : new ItemsIngredient(Integer.parseInt(number), content.matchingStacks);
            onContentUpdate();
        }).setNumbersOnly(1, Integer.MAX_VALUE).setCurrentString(content.getAmount()+""));

        TextFieldWidget ore;
        WidgetGroup groupOre = new WidgetGroup(x, y + 40, 120, 80);
        WidgetGroup groupIngredient = new WidgetGroup(x, y + 20, 120, 80);
        DraggableScrollableWidgetGroup container = new DraggableScrollableWidgetGroup(0, 20, 120, 50).setBackground(new ColorRectTexture(0xffaaaaaa));
        groupIngredient.addWidget(container);
        dialog.addWidget(groupIngredient);
        dialog.addWidget(groupOre);

        groupOre.addWidget(ore = new TextFieldWidget(30, 3, 90, 15, true,
                () -> content.isOre() ? content.getOreDict() : "", null));
        IItemHandlerModifiable handler;
        PhantomSlotWidget phantomSlotWidget = new PhantomSlotWidget(handler = new ItemStackHandler(1), 0, 0, 1).setClearSlotOnRightClick(false);
        groupOre.addWidget(phantomSlotWidget);
        phantomSlotWidget.setChangeListener(() -> {
            ItemStack newStack = handler.getStackInSlot(0);
            if (newStack.isEmpty()) return;
            int[] ids = OreDictionary.getOreIDs(newStack);
            if (ids.length > 0) {
                String oreDict = OreDictionary.getOreName(ids[0]);
                content = new ItemsIngredient(content.getAmount(), oreDict);
                ore.setCurrentString(oreDict);
                phantomSlotWidget.setHoverTooltip(LocalizationUtils.format("multiblocked.gui.trait.item.ore_dict") + ": " + Arrays.stream(ids).mapToObj(OreDictionary::getOreName).reduce("", (a, b) -> a + "\n" + b));
            } else {
                content = new ItemsIngredient(content.getAmount(), "");
                ore.setCurrentString("");
                handler.setStackInSlot(0, ItemStack.EMPTY);
            }
            onContentUpdate();

        }).setBackgroundTexture(new ColorRectTexture(0xaf444444));
        ore.setTextResponder(oreDict -> {
            content = new ItemsIngredient(content.getAmount(), oreDict);
            ItemStack[] matches = content.getMatchingStacks();
            handler.setStackInSlot(0, matches.length > 0 ? matches[0] : ItemStack.EMPTY);
            phantomSlotWidget.setHoverTooltip(LocalizationUtils.format("multiblocked.gui.trait.item.ore_dict") + ":\n"  + content.getOreDict());
            onContentUpdate();
        });
        ore.setHoverTooltip("multiblocked.gui.trait.item.ore_dic");
        dialog.addWidget(new SwitchWidget(x, y + 22, 50, 15, (cd, r)->{
            groupOre.setVisible(r);
            content = r ? new ItemsIngredient(content.getAmount(), ore.getCurrentString()) : new ItemsIngredient(content.getAmount(), content.matchingStacks);
            groupIngredient.setVisible(!r);
            if (r) {
                ItemStack[] matches = content.getMatchingStacks();
                handler.setStackInSlot(0, matches.length > 0 ? matches[0] : ItemStack.EMPTY);
                phantomSlotWidget.setHoverTooltip("oreDict: \n" + content.getOreDict());
            } else {
                updateIngredientWidget(container);
            }
            onContentUpdate();
        }).setPressed(content.isOre()).setHoverBorderTexture(1, -1)
                .setBaseTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("ore (N)"))
                .setPressedTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("ore (Y)"))
                .setHoverTooltip("using ore dictionary"));

        groupIngredient.setVisible(!content.isOre());
        groupOre.setVisible(content.isOre());
        if (content.isOre()) {
            ItemStack[] matches = content.getMatchingStacks();
            handler.setStackInSlot(0, matches.length > 0 ? matches[0] : ItemStack.EMPTY);
            phantomSlotWidget.setHoverTooltip(LocalizationUtils.format("multiblocked.gui.trait.item.ore_dict") + ":\n"  + content.getOreDict());
        } else {
            updateIngredientWidget(container);
        }
        groupIngredient.addWidget(new LabelWidget(x + 50, 5, "multiblocked.gui.tips.settings"));
        groupIngredient.addWidget(new ButtonWidget(100, 0, 20, 20, cd -> {
            ItemStack[] stacks = content.matchingStacks;
            content = new ItemsIngredient(content.getAmount(), ArrayUtils.add(stacks, new ItemStack(Items.IRON_INGOT)));
            updateIngredientWidget(container);
            onContentUpdate();
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/add.png")).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.trait.item.add"));
    }

    private void updateIngredientWidget(DraggableScrollableWidgetGroup container) {
        container.widgets.forEach(container::waitToRemoved);
        ItemStack[] matchingStacks = ArrayUtils.clone(content.matchingStacks);
        for (int i = 0; i < matchingStacks.length; i++) {
            ItemStack stack = matchingStacks[i];
            IItemHandlerModifiable handler;
            int finalI = i;
            int x = (i % 4) * 30;
            int y = (i / 4) * 20;
            container.addWidget(new PhantomSlotWidget(handler = new ItemStackHandler(1), 0, x + 1, y + 1)
                    .setClearSlotOnRightClick(false)
                    .setChangeListener(() -> {
                        ItemStack newStack = handler.getStackInSlot(0);
                        matchingStacks[finalI] = newStack;
                        content = new ItemsIngredient(content.getAmount(), matchingStacks);
                        onContentUpdate();
                    }).setBackgroundTexture(new ColorRectTexture(0xaf444444)));
            handler.setStackInSlot(0, stack);
            container.addWidget(new ButtonWidget(x + 21, y + 1, 9, 9, cd -> {
                content = new ItemsIngredient(content.getAmount(), ArrayUtils.remove(matchingStacks, finalI));
                updateIngredientWidget(container);
                onContentUpdate();
            }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/remove.png")).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.remove"));
        }
    }

}
