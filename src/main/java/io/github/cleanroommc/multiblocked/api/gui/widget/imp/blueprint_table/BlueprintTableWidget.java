package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.api.item.ItemBlueprint;
import io.github.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BlueprintTableWidget extends TabContainer {
    public final BlueprintTableTileEntity table;
    public SceneWidget sceneWidget;
    public Group selected;

    public BlueprintTableWidget(BlueprintTableTileEntity table) {
        super(0, 0, 384, 256);
        this.table = table;
        this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_page.png")));
        this.addWidget(new LabelWidget(134, 34, this::status).setTextColor(-1).setDrop(true));
        this.addWidget(new LabelWidget(134, 49, this::size).setTextColor(-1).setDrop(true));
        this.addWidget(new LabelWidget(134, 64, this::description).setTextColor(-1).setDrop(true));

    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        super.writeInitialData(buffer);
        DraggableScrollableWidgetGroup containers;
        this.addWidget(containers = new DraggableScrollableWidgetGroup(32, 152, 208 - 32, 224 - 152));
        TileEntity tileEntity = table.getWorld().getTileEntity(table.getPos().offset(EnumFacing.UP).offset(table.getFrontFacing().getOpposite()).offset(table.getFrontFacing().rotateY()));
        Set<Integer> caught = new HashSet<>();
        if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack itemStack = handler.getStackInSlot(i);
                    if (ItemBlueprint.isItemBlueprint(itemStack)) {
                        new Group(containers, handler, i);
                        caught.add(i);
                    }
                }
            }
        }
        buffer.writeVarInt(caught.size());
        caught.forEach(buffer::writeVarInt);
    }

    @Override
    public void readInitialData(PacketBuffer buffer) {
        super.readInitialData(buffer);
        DraggableScrollableWidgetGroup containers;
        this.addWidget(containers = new DraggableScrollableWidgetGroup(32, 152, 208 - 32, 224 - 152));
        int size = buffer.readVarInt();
        if (size > 0) {
            IItemHandler handler = table.getWorld().getTileEntity(table.getPos().offset(EnumFacing.UP).offset(table.getFrontFacing().getOpposite()).offset(table.getFrontFacing().rotateY())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            for (int i = size; i > 0; i--) {
                new Group(containers, handler, buffer.readVarInt());
            }
        }
    }

    private String size() {
        if (sceneWidget != null) {
            int size = 0;
            for (Collection<BlockPos> collection : sceneWidget.getRenderer().renderedBlocksMap.keySet()) {
                size += collection.size();
            }
            return "size: " + size;
        }
        return "size:";
    }

    private String description() {
        String result = "description:\n";
        if (selected != null) {
            BlockPos[] poses = ItemBlueprint.getPos(selected.slotWidget.getHandle().getStack());
            if (poses != null) {
                result += String.format("from: %d, %d, %d\n", poses[0].getX(), poses[0].getY(), poses[0].getZ());
                result += String.format("to: %d, %d, %d", poses[0].getX(), poses[0].getY(), poses[0].getZ());
            }
        }
        return result;
    }

    private String status() {
        return "status: " + (selected == null ? "" : ItemBlueprint.isRaw(selected.slotWidget.getHandle().getStack()) ?
                TextFormatting.YELLOW + "raw" : TextFormatting.GREEN + "template");
    }

    public void onSelected(Group selected) {
        if (this.selected != selected) {
            this.selected = selected;
            if (selected != null && isRemote()) {
                if (sceneWidget != null) {
                    removeWidget(sceneWidget);
                }
                ItemStack itemStack = selected.slotWidget.getHandle().getStack();
                if (ItemBlueprint.isRaw(itemStack)) {
                    BlockPos[] poses = ItemBlueprint.getPos(itemStack);
                    World world = table.getWorld();
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
                        this.addWidget(sceneWidget = new SceneWidget(34, 34, 94, 94, world)
                                .setRenderedCore(rendered, null)
                                .setRenderFacing(false));
                    }
                }
            }
        }
    }

    public class Group extends WidgetGroup {
        boolean isSelected;
        DraggableScrollableWidgetGroup containers;
        SlotWidget slotWidget;

        public Group(DraggableScrollableWidgetGroup containers, IItemHandler handler, int i) {
            super(0, containers.widgets.size() * 20, containers.getSize().width, 20);
            this.containers = containers;
            containers.addWidget(this);
            this.addWidget(slotWidget = new SlotWidget(handler, i, 0, 0, false, true));
            this.addWidget(new LabelWidget(30, 5, () -> slotWidget.getHandle().getStack().getDisplayName()).setTextColor(0xff000000));
        }

        @Override
        public Widget mouseClicked(int mouseX, int mouseY, int button) {
            if (isMouseOverElement(mouseX, mouseY)) {
                for (Widget widget : containers.widgets) {
                    if (widget instanceof Group) {
                        ((Group) widget).isSelected = false;
                    }
                }
                isSelected = true;
                BlueprintTableWidget.this.onSelected(this);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
            if (isSelected) {
                DrawerHelper.drawSolidRect(getPosition().x, getPosition().y, getSize().width, getSize().height, 0xafffffff);
            }
            if (isMouseOverElement(mouseX, mouseY)) {
                DrawerHelper.drawBorder(getPosition().x + 2, getPosition().y + 2, getSize().width - 4, getSize().height - 4, 0xffff0000, 2);
            }
            super.drawInBackground(mouseX, mouseY, partialTicks);
        }
    }
}
