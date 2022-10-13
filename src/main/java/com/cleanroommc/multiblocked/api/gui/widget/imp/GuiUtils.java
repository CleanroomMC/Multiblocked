package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/8/16
 * @implNote GuiUtils
 */
public class GuiUtils {

    public static WidgetGroup createSelector(int x, int y, String text, String tips, String init, List<String> candidates, Consumer<String> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new SelectorWidget(0, 0, 65, 15, candidates, -1)
                .setValue(init)
                .setOnChanged(onPressed)
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xff333333))
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(70, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createBoolSwitch(int x, int y, String text, String tips, boolean init, Consumer<Boolean> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new SwitchWidget(0, 0, 15, 15, (cd, r)->onPressed.accept(r))
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                .setPressed(init)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(20, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createIntField(int x, int y, String text, String tips, int init, int min, int max, Consumer<Integer> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new TextFieldWidget(0, 2, 30, 10, true, null, s -> onPressed.accept(Integer.parseInt(s)))
                .setCurrentString(init + "")
                .setNumbersOnly(min, max)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(35, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createFloatField(int x, int y, String text, String tips, float init, float min, float max, Consumer<Float> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new TextFieldWidget(0, 2, 30, 10, true, null, s -> onPressed.accept(Float.parseFloat(s)))
                .setCurrentString(init + "")
                .setNumbersOnly(min, max)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(35, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createStringField(int x, int y, String text, String tips, String init, Consumer<String> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new TextFieldWidget(0, 2, 60, 10, true, null, onPressed)
                .setCurrentString(init)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(65, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createItemStackSelector(WidgetGroup parent, int x, int y, String text, List<ItemStack> init, Consumer<List<ItemStack>> onUpdated) {
        WidgetGroup group = new WidgetGroup(x, y, 162, 100);
        List<ItemStack> itemList = new ArrayList<>(init);
        DraggableScrollableWidgetGroup container = new DraggableScrollableWidgetGroup(0, 25, 162, 80).setBackground(new ColorRectTexture(0xffaaaaaa));
        group.addWidget(container);
        for (ItemStack itemStack : itemList) {
            addItemStackSelectorWidget(parent, itemList, container, itemStack, onUpdated);
        }
        group.addWidget(new LabelWidget(0, 6, text));
        group.addWidget(new ButtonWidget(142, 0, 20, 20, cd -> {
            itemList.add(null);
            addItemStackSelectorWidget(parent, itemList, container, null, onUpdated);
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/add.png")).setHoverBorderTexture(1, -1));
        return group;
    }

    private static void addItemStackSelectorWidget(WidgetGroup parent, List<ItemStack> itemList, DraggableScrollableWidgetGroup container, ItemStack itemStack, Consumer<List<ItemStack>> onUpdated) {
        ItemStackSelectorWidget bsw = new ItemStackSelectorWidget(parent, 0, container.widgets.size() * 21 + 1, 140);
        container.addWidget(bsw);
        bsw.addWidget(new ButtonWidget(143, 1, 18, 18, cd -> {
            int index = (bsw.getSelfPosition().y - 1) / 21;
            itemList.remove(index);
            onUpdated.accept(itemList);
            for (int i = index + 1; i < container.widgets.size(); i++) {
                container.widgets.get(i).addSelfPosition(0, -21);
            }
            container.waitToRemoved(bsw);
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/remove.png")).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.remove"));
        if (itemStack != null) {
            bsw.setItemStack(itemStack);
        }
        bsw.setOnItemStackUpdate(stack->{
            int index = (bsw.getSelfPosition().y - 1) / 21;
            itemList.set(index, stack);
            onUpdated.accept(itemList);
        });
    }

    public static WidgetGroup createFluidStackSelector(WidgetGroup parent, int x, int y, String text, List<FluidStack> init, Consumer<List<FluidStack>> onUpdated) {
        WidgetGroup group = new WidgetGroup(x, y, 162, 100);
        List<FluidStack> fluidList = new ArrayList<>(init);
        DraggableScrollableWidgetGroup container = new DraggableScrollableWidgetGroup(0, 25, 162, 80).setBackground(new ColorRectTexture(0xffaaaaaa));
        group.addWidget(container);
        for (FluidStack fluidStack : fluidList) {
            addFluidStackSelectorWidget(parent, fluidList, container, fluidStack, onUpdated);
        }
        group.addWidget(new LabelWidget(0, 6, text));
        group.addWidget(new ButtonWidget(142, 0, 20, 20, cd -> {
            fluidList.add(null);
            addFluidStackSelectorWidget(parent, fluidList, container, null, onUpdated);
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/add.png")).setHoverBorderTexture(1, -1));
        return group;
    }

    private static void addFluidStackSelectorWidget(WidgetGroup parent, List<FluidStack> fluidList, DraggableScrollableWidgetGroup container, FluidStack fluidStack, Consumer<List<FluidStack>> onUpdated) {
        FluidStackSelectorWidget bsw = new FluidStackSelectorWidget(parent, 0, container.widgets.size() * 21 + 1, 140);
        container.addWidget(bsw);
        bsw.addWidget(new ButtonWidget(143, 1, 18, 18, cd -> {
            int index = (bsw.getSelfPosition().y - 1) / 21;
            fluidList.remove(index);
            onUpdated.accept(fluidList);
            for (int i = index + 1; i < container.widgets.size(); i++) {
                container.widgets.get(i).addSelfPosition(0, -21);
            }
            container.waitToRemoved(bsw);
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/remove.png")).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.remove"));
        if (fluidStack != null) {
            bsw.setFluidStack(fluidStack);
        }
        bsw.setOnFluidStackUpdate(stack->{
            int index = (bsw.getSelfPosition().y - 1) / 21;
            fluidList.set(index, stack);
            onUpdated.accept(fluidList);
        });
    }
}
