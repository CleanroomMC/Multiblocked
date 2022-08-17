package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.JsonBlockPatternWidget;
import com.cleanroommc.multiblocked.api.item.ItemBlueprint;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.registry.MbdItems;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TemplateBuilderWidget extends WidgetGroup {
    public final BlueprintTableTileEntity table;
    public final ButtonWidget templateButton;
    public SceneWidget sceneWidget;
    public ItemStack selected;
    public int selectedSlot;
    public BlockPos pos;
    public EnumFacing facing;
    protected DraggableScrollableWidgetGroup containers;
    private boolean init;

    public TemplateBuilderWidget(BlueprintTableTileEntity table) {
        super(0, 0, 384, 256);
        this.table = table;
        this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_page.png")));
        this.addWidget(new ImageWidget(30, 34, 160, 180, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        this.addWidget(new LabelWidget(200, 34, this::status).setTextColor(-1).setDrop(true));
        this.addWidget(new LabelWidget(200, 49, this::size).setTextColor(-1).setDrop(true));
        this.addWidget(new LabelWidget(200, 64, this::description).setTextColor(-1).setDrop(true));
        this.addWidget(templateButton = new ButtonWidget(200, 100, 20, 20, new ItemStackTexture(
                MbdItems.BUILDER), this::onBuildTemplate));
        this.addWidget(sceneWidget = (SceneWidget) new SceneWidget(30, 34, 160, 180, null)
                .useCacheBuffer()
                .setOnSelected(((pos, facing) -> {
                    this.pos = pos;
                    this.facing = facing;
                }))
                .setRenderSelect(false)
                .setRenderFacing(false)
                .setClientSideWidget());
        this.addWidget(new ImageWidget(200 - 4, 120 - 4, 150 + 8, 98 + 8, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        templateButton.setHoverTooltip("multiblocked.gui.builder.template.create");
        templateButton.setVisible(false);
    }

    protected void onBuildTemplate(ClickData clickData) {
        if (isRemote() && ItemBlueprint.isItemBlueprint(selected)) {
            JsonBlockPattern pattern = null;
            if (ItemBlueprint.isRaw(selected)) {
                BlockPos[] poses = ItemBlueprint.getPos(selected);
                World world = table.getWorld();
                if (poses != null && world.isAreaLoaded(poses[0], poses[1])) {
                    ControllerTileEntity controller = null;
                    for (int x = poses[0].getX(); x <= poses[1].getX(); x++) {
                        for (int y = poses[0].getY(); y <= poses[1].getY(); y++) {
                            for (int z = poses[0].getZ(); z <= poses[1].getZ(); z++) {
                                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                                if (te instanceof ControllerTileEntity) {
                                    controller = (ControllerTileEntity) te;
                                }
                            }
                        }
                    }
                    if (controller != null) {
                        pattern = new JsonBlockPattern(table.getWorld(), controller.getLocation(), controller.getPos(), controller.getFrontFacing(),
                                poses[0].getX(), poses[0].getY(), poses[0].getZ(),
                                poses[1].getX(), poses[1].getY(), poses[1].getZ());

                    } else {
                        // TODO tips dialog
                    }
                } else {
                    // TODO tips dialog
                }
            } else if (selected.getSubCompound("pattern") != null){
                String json = selected.getSubCompound("pattern").getString("json");
                pattern = Multiblocked.GSON.fromJson(json, JsonBlockPattern.class);
            }
            if (pattern != null) {
                new JsonBlockPatternWidget(this, pattern, patternResult -> {
                    if (patternResult != null) {
                        if (ItemBlueprint.setPattern(selected) && patternResult.predicates.get("controller") instanceof PredicateComponent) {
                            patternResult.cleanUp();
                            String json = patternResult.toJson();
                            String controller = ((PredicateComponent)patternResult.predicates.get("controller")).location.toString();
                            selected.getOrCreateSubCompound("pattern").setString("json", json);
                            selected.getOrCreateSubCompound("pattern").setString("controller", controller);
                            writeClientAction(-1, buffer -> {
                                buffer.writeVarInt(selectedSlot);
                                buffer.writeString(json);
                                buffer.writeString(controller);
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (init) return;
        init = true;
        writeUpdateInfo(-1, this::writeInitialData);
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -1) {
            readInitialData(buffer);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            int slotIndex = buffer.readVarInt();
            String json = buffer.readString(Short.MAX_VALUE);
            String controller = buffer.readString(Short.MAX_VALUE);
            TileEntity tileEntity = table.getWorld().getTileEntity(table.getPos().offset(EnumFacing.UP).offset(table.getFrontFacing().getOpposite()).offset(table.getFrontFacing().rotateY()));
            if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handler != null && handler.getSlots() > slotIndex) {
                    ItemStack itemStack = handler.getStackInSlot(slotIndex);
                    if (ItemBlueprint.isItemBlueprint(itemStack)) {
                        ItemBlueprint.setPattern(itemStack);
                        itemStack.getOrCreateSubCompound("pattern").setString("json", json);
                        itemStack.getOrCreateSubCompound("pattern").setString("controller", controller);
                    }
                }
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        super.writeInitialData(buffer);
        TileEntity tileEntity = table.getWorld().getTileEntity(table.getPos().offset(EnumFacing.UP).offset(table.getFrontFacing().getOpposite()).offset(table.getFrontFacing().rotateY()));
        Map<Integer, ItemStack> caught = new Int2ObjectOpenHashMap<>();
        if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack itemStack = handler.getStackInSlot(i);
                    if (ItemBlueprint.isItemBlueprint(itemStack)) {
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

    @Override
    public void readInitialData(PacketBuffer buffer) {
        super.readInitialData(buffer);
        this.addWidget(containers = new DraggableScrollableWidgetGroup(200, 120, 150, 98));
        containers.setClientSideWidget();
        try {
            for (int i = buffer.readVarInt(); i > 0; i--) {
                ItemStack itemStack = buffer.readItemStack();
                int slotIndex = buffer.readVarInt();
                containers.addWidget( new SelectableWidgetGroup(0, containers.widgets.size() * 22, containers.getSize().width, 20)
                        .setSelectedTexture(-2, 0xff00aa00)
                        .setOnSelected(w -> onSelected(itemStack, slotIndex))
                        .addWidget(new ImageWidget(0, 0, 150, 20, new ColorRectTexture(0x4faaaaaa)))
                        .addWidget(new ImageWidget(32, 0, 100, 20, new TextTexture(itemStack.getDisplayName()).setWidth(100).setType(TextTexture.TextType.ROLL)))
                        .addWidget(new ImageWidget(4, 2, 18, 18, new ItemStackTexture(itemStack))));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private String size() {
        String result = LocalizationUtils.format("multiblocked.gui.builder.template.size");
        if (selected != null) {
            BlockPos[] poses = ItemBlueprint.getPos(selected);
            if (poses != null) {
                result += String.format("(%dX%dX%d)", poses[1].getX() - poses[0].getX() + 1,  poses[1].getY() - poses[0].getY() + 1,  poses[1].getZ() - poses[0].getZ() + 1);
            }
        }
        return result;
    }

    private String description() {
        String result = LocalizationUtils.format("multiblocked.gui.builder.template.description") + "\n";
        if (selected != null) {
            BlockPos[] poses = ItemBlueprint.getPos(selected);
            if (poses != null) {
                result += LocalizationUtils.format("multiblocked.gui.builder.template.from", poses[0].getX(), poses[0].getY(), poses[0].getZ()) + "\n";
                result += LocalizationUtils.format("multiblocked.gui.builder.template.to", poses[0].getX(), poses[0].getY(), poses[0].getZ());
            }
        }
        return result;
    }

    private String status() {
        return LocalizationUtils.format("multiblocked.gui.builder.template.status") + " " +
                (selected == null ? "" : ItemBlueprint.isRaw(selected) ?
                        TextFormatting.YELLOW + LocalizationUtils.format("multiblocked.gui.builder.template.raw") :
                        TextFormatting.GREEN + LocalizationUtils.format("multiblocked.gui.builder.template.pattern"));
    }

    @SideOnly(Side.CLIENT)
    public void onSelected(ItemStack itemStack, int slot) {
        if (this.selected != itemStack) {
            this.selected = itemStack;
            this.selectedSlot = slot;
            if (selected != null && isRemote()) {
                this.pos = null;
                this.facing = null;
                templateButton.setVisible(true);
                if (ItemBlueprint.isRaw(itemStack)) {
                    BlockPos[] poses = ItemBlueprint.getPos(itemStack);
                    World world = table.getWorld();
                    sceneWidget.createScene(world);
                    if (poses != null && world.isAreaLoaded(poses[0], poses[1])) {
                        Set<BlockPos> rendered = new HashSet<>();
                        for (int x = poses[0].getX(); x <= poses[1].getX(); x++) {
                            for (int y = poses[0].getY(); y <= poses[1].getY(); y++) {
                                for (int z = poses[0].getZ(); z <= poses[1].getZ(); z++) {
                                    if (!world.isAirBlock(new BlockPos(x, y, z))) {
                                        rendered.add(new BlockPos(x, y, z));
                                    }
                                }
                            }
                        }
                        sceneWidget.setRenderedCore(rendered, null);
                    }
                } else if (itemStack.getSubCompound("pattern") != null){
                    String json = itemStack.getSubCompound("pattern").getString("json");
                    JsonBlockPattern pattern = Multiblocked.GSON.fromJson(json, JsonBlockPattern.class);
                    int[] centerOffset = pattern.getCenterOffset();
                    String[][] patternString = pattern.pattern;
                    Set<BlockPos> rendered = new HashSet<>();
                    TrackedDummyWorld world = new TrackedDummyWorld();
                    sceneWidget.createScene(world);
                    int offset = Math.max(patternString.length, Math.max(patternString[0].length, patternString[0][0].length()));
                    for (int i = 0; i < patternString.length; i++) {
                        for (int j = 0; j < patternString[0].length; j++) {
                            for (int k = 0; k < patternString[0][0].length(); k++) {
                                char symbol = patternString[i][j].charAt(k);
                                BlockPos pos = pattern.getActualPosOffset(k - centerOffset[2], j - centerOffset[1], i - centerOffset[0], EnumFacing.NORTH).add(offset, offset, offset);
                                world.addBlock(pos, new BlockInfo(MbdComponents.DummyComponentBlock));
                                DummyComponentTileEntity tileEntity = (DummyComponentTileEntity) world.getTileEntity(pos);
                                ComponentDefinition definition = null;
                                assert tileEntity != null;
                                if (pattern.symbolMap.containsKey(symbol)) {
                                    Set<BlockInfo> candidates = new HashSet<>();
                                    for (String s : pattern.symbolMap.get(symbol)) {
                                        SimplePredicate predicate = pattern.predicates.get(s);
                                        if (predicate instanceof PredicateComponent && ((PredicateComponent) predicate).definition != null) {
                                            definition = ((PredicateComponent) predicate).definition;
                                            break;
                                        } else if (predicate != null && predicate.candidates != null) {
                                            candidates.addAll(Arrays.asList(predicate.candidates.get()));
                                        }
                                    }
                                    definition = getComponentDefinition(definition, candidates);
                                }
                                if (definition != null) {
                                    tileEntity.setDefinition(definition);
                                }
                                tileEntity.isFormed = false;
                                tileEntity.setWorld(world);
                                tileEntity.validate();
                                rendered.add(pos);
                            }
                        }
                    }
                    sceneWidget.setRenderedCore(rendered, null);
                }
            }
        }
    }

    public static ComponentDefinition getComponentDefinition(ComponentDefinition definition, Set<BlockInfo> candidates) {
        if (candidates.size() == 1) {
            definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "i_renderer"));
            definition.getBaseStatus().setRenderer(new BlockStateRenderer(candidates.toArray(new BlockInfo[0])[0]));
        } else if (!candidates.isEmpty()) {
            definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "i_renderer"));
            definition.getBaseStatus().setRenderer(new CycleBlockStateRenderer(candidates.toArray(new BlockInfo[0])));
        }
        return definition;
    }
}
