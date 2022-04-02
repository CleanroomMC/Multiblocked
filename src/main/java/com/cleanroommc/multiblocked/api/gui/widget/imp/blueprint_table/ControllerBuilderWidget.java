package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components.ControllerWidget;
import com.cleanroommc.multiblocked.api.item.ItemBlueprint;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import com.cleanroommc.multiblocked.client.MultiblockedResourceLoader;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ControllerBuilderWidget extends TemplateBuilderWidget {
    List<SelectableWidgetGroup> files = new ArrayList<>();

    public ControllerBuilderWidget(BlueprintTableTileEntity table) {
        super(table);
        templateButton.setHoverTooltip("Create a controller from a blueprint");
    }

    @Override
    protected void onBuildTemplate(ClickData clickData) {
        if (clickData.isRemote) {
            if (pos != null && facing != null && selected != null && facing.getAxis() != EnumFacing.Axis.Y) {
                BlockPos[] poses = ItemBlueprint.getPos(selected);
                if (poses != null) {
                    World world = table.getWorld();
                    ResourceLocation location = new ResourceLocation("mod_id:component_id");
                    ControllerDefinition controllerDefinition = new ControllerDefinition(location);
                    controllerDefinition.baseRenderer = new BlockStateRenderer(world.getBlockState(pos));
                    for (Widget widget : widgets) {
                        widget.setVisible(false);
                        widget.setActive(false);
                    }
                    new ControllerWidget(this, controllerDefinition, new JsonBlockPattern(world, location, pos, facing,
                            poses[0].getX(), poses[0].getY(), poses[0].getZ(),
                            poses[1].getX(), poses[1].getY(), poses[1].getZ()),
                            "empty", jsonObject -> {
                        for (Widget widget : widgets) {
                            widget.setVisible(true);
                            widget.setActive(true);
                        }
                        if (jsonObject != null) {
                            FileUtility.saveJson(new File(MultiblockedResourceLoader.location, "definition/controller/" + jsonObject.get("location").getAsString().replace(":", "_") + ".json"), jsonObject);
                            updateList();
                        }
                    });
                }
            } else {
                // TODO
            }
        }
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        TileEntity tileEntity = table.getWorld().getTileEntity(table.getPos().offset(EnumFacing.UP).offset(table.getFrontFacing().getOpposite()).offset(table.getFrontFacing().rotateY()));
        Map<Integer, ItemStack> caught = new Int2ObjectOpenHashMap<>();
        if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack itemStack = handler.getStackInSlot(i);
                    if (ItemBlueprint.isItemBlueprint(itemStack) && ItemBlueprint.isRaw(itemStack)) {
                        caught.put(i, itemStack);
                    }
                }
            }
        }
        buffer.writeVarInt(caught.size());
        caught.forEach((k, v) -> {
            buffer.writeItemStack(v);
            buffer.writeVarInt(k);
        });
    }

    protected void updateList() {
        int size = files.size();
        files.forEach(containers::waitToRemoved);
        files.clear();
        File path = new File(MultiblockedResourceLoader.location, "definition/controller");
        if (!path.isDirectory()) {
            if (!path.mkdirs()) {
                return;
            }
        }
        for (File file : Optional.ofNullable(path.listFiles((s, name) -> name.endsWith(".json"))).orElse(new File[0])) {
            SelectableWidgetGroup widgetGroup = (SelectableWidgetGroup) new SelectableWidgetGroup(0, (containers.widgets.size() - size) * 22, containers.getSize().width, 20)
                    .setSelectedTexture(-2, 0xff00aa00)
                    .setOnSelected(W -> {
                        templateButton.setVisible(false);
                        selected = null;
                    })
                    .addWidget(new ImageWidget(0, 0, 150, 20, new ColorRectTexture(0x4faaaaaa)))
                    .addWidget(new ButtonWidget(134, 4, 12, 12, new ResourceTexture("multiblocked:textures/gui/option.png"), cd -> {
                        JsonElement jsonElement = FileUtility.loadJson(file);
                        if (jsonElement != null) {
                            try {
                                String recipeMap = jsonElement.getAsJsonObject().get("recipeMap").getAsString();
                                JsonBlockPattern pattern = Multiblocked.GSON.fromJson(jsonElement.getAsJsonObject().get("basePattern"), JsonBlockPattern.class);
                                ControllerDefinition definition = Multiblocked.GSON.fromJson(jsonElement, ControllerDefinition.class);
                                for (Widget widget : widgets) {
                                    widget.setVisible(false);
                                    widget.setActive(false);
                                }
                                new ControllerWidget(this, definition, pattern, recipeMap, jsonObject -> {
                                    for (Widget widget : widgets) {
                                        widget.setVisible(true);
                                        widget.setActive(true);
                                    }
                                    if (jsonObject != null) {
                                        FileUtility.saveJson(file, jsonObject);
                                    }
                                });
                            } catch (Exception ignored) {}
                        }
                    }).setHoverBorderTexture(1, -1).setHoverTooltip("setting"))
                    .addWidget(new ImageWidget(32, 0, 100, 20, new TextTexture(file.getName()).setWidth(100).setType(TextTexture.TextType.ROLL)))
                    .addWidget(new ImageWidget(4, 2, 18, 18, new ItemStackTexture(Items.PAPER)));
            files.add(widgetGroup);
            containers.addWidget(widgetGroup);
        }
    }

    @Override
    public void readInitialData(PacketBuffer buffer) {
        super.readInitialData(buffer);
        updateList();
    }


    public void onSelected(ItemStack selected, int slot) {
        super.onSelected(selected, slot);
        if (sceneWidget != null) {
            sceneWidget.setRenderFacing(true);
            sceneWidget.setRenderSelect(true);
        }
    }
}
