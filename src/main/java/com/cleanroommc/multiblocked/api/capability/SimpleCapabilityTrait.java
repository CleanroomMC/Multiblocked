package com.cleanroommc.multiblocked.api.capability;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.util.JsonUtil;
import com.cleanroommc.multiblocked.util.Position;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class SimpleCapabilityTrait extends CapabilityTrait {
    protected IO[] capabilityIO;
    protected IO[] guiIO;
    protected int[] x;
    protected int[] y;

    public SimpleCapabilityTrait(MultiblockCapability<?> capability) {
        super(capability);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        if (jsonElement == null) {
            jsonElement = new JsonArray();
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        int size = jsonArray.size();
        capabilityIO = new IO[size];
        guiIO = new IO[size];
        x = new int[size];
        y = new int[size];
        int i = 0;
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            capabilityIO[i] = JsonUtil.getEnumOr(jsonObject, "cIO", IO.class, IO.BOTH);
            guiIO[i] = JsonUtil.getEnumOr(jsonObject, "gIO", IO.class, IO.BOTH);
            x[i] = JsonUtils.getInt(jsonObject, "x", 5);
            y[i] = JsonUtils.getInt(jsonObject, "y", 5);
            i++;
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < capabilityIO.length; i++) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("cIO", capabilityIO[i].ordinal());
            jsonObject.addProperty("gIO", guiIO[i].ordinal());
            jsonObject.addProperty("x", x[i]);
            jsonObject.addProperty("y", y[i]);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    private int getColorByIO(IO io) {
        return io == IO.IN ? 0xaf00ff00 : io == IO.OUT ? 0xafff0000 : 0xaf0000ff;
    }

    private void refreshSlots(DraggableScrollableWidgetGroup dragGroup) {
        dragGroup.widgets.forEach(dragGroup::waitToRemoved);
        for (int i = 0; i < guiIO.length; i++) {
            int finalI = i;
            ButtonWidget setting = new ButtonWidget(18, 4, 10, 10, new ResourceTexture("multiblocked:textures/gui/option.png"), null);
            setting.setVisible(false);
            DraggableWidgetGroup button = new DraggableWidgetGroup(5, 5, 18, 18);
            button.setSelfPosition(new Position(x[finalI], y[finalI]));
            button.setOnSelected(w -> setting.setVisible(true));
            button.setOnUnSelected(w -> setting.setVisible(false));
            button.addWidget(setting);
            ImageWidget imageWidget= new ImageWidget(1, 1, 16, 16, new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO[finalI])), new ColorBorderTexture(1, getColorByIO(capabilityIO[finalI]))));
            button.addWidget(imageWidget);
            button.setOnEndDrag(b -> {
                x[finalI] = b.getSelfPosition().x;
                y[finalI] = b.getSelfPosition().y;
            });

            dragGroup.addWidget(button);
            setting.setOnPressCallback(cd2 -> {
                DialogWidget dialog = new DialogWidget(dragGroup, true);
                dialog.addWidget(new ImageWidget(0, 0, 176, 256, new ColorRectTexture(0xaf000000)));
                dialog.addWidget(new ButtonWidget(5, 5, 100, 20, new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("remove slot")), cd3 -> {
                    removeSlot(finalI);
                    refreshSlots(dragGroup);
                    dialog.close();
                }));
                dialog.addWidget(new SelectorWidget(5, 30, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                        .setValue(capabilityIO[finalI].name())
                        .setOnChanged(io-> {
                            capabilityIO[finalI] = IO.valueOf(io);
                            imageWidget.setImage(new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO[finalI])), new ColorBorderTexture(1, getColorByIO(capabilityIO[finalI]))));
                        })
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(new ColorRectTexture(0xffaaaaaa))
                        .setHoverTooltip("Capability IO (e.g., pipe interaction)"));
                dialog.addWidget(new SelectorWidget(50, 30, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                        .setValue(guiIO[finalI].name())
                        .setOnChanged(io-> {
                            guiIO[finalI] = IO.valueOf(io);
                            imageWidget.setImage(new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO[finalI])), new ColorBorderTexture(1, getColorByIO(capabilityIO[finalI]))));
                        })
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(new ColorRectTexture(0xffaaaaaa))
                        .setHoverTooltip("Gui IO (e.g. gui interaction)"));
                initDialog(dialog, finalI);
            });
        }
    }

    protected void initDialog(DialogWidget dialog, final int index) {

    }
    
    protected void addSlot() {
        capabilityIO = ArrayUtils.add(capabilityIO, IO.BOTH);
        guiIO = ArrayUtils.add(guiIO, IO.BOTH);
        x = ArrayUtils.add(x, 5);
        y = ArrayUtils.add(y, 5);
    }

    protected void removeSlot(int index) {
        capabilityIO = ArrayUtils.remove(capabilityIO, index);
        guiIO = ArrayUtils.remove(guiIO, index);
        x = ArrayUtils.remove(x, index);
        y = ArrayUtils.remove(y, index);
    }

    @Override
    public void openConfigurator(WidgetGroup parentDialog) {
        DraggableScrollableWidgetGroup dragGroup = new DraggableScrollableWidgetGroup((384 - 176) / 2, 0, 176, 256);
        parentDialog.addWidget(dragGroup);
        refreshSlots(dragGroup);
        // add new slot
        parentDialog.addWidget(new ButtonWidget((384 - 176) / 2 -20,35, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd -> {
            addSlot();
            refreshSlots(dragGroup);
        }));
    }

}
