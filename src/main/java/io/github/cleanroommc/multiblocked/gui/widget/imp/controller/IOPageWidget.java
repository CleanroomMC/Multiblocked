package io.github.cleanroommc.multiblocked.gui.widget.imp.controller;

import com.google.common.collect.Table;
import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import io.github.cleanroommc.multiblocked.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.gui.widget.imp.ButtonWidget;
import io.github.cleanroommc.multiblocked.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.gui.widget.imp.SceneWidget;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IOPageWidget extends WidgetGroup {
    private static final ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/io_page.png");
    private static final IGuiTexture BACKGROUND = PAGE.getSubTexture(0, 0, 176 / 256.0, 1);
    private static final IGuiTexture RIGHT_BUTTON = PAGE.getSubTexture(176 / 256.0, 84 / 256.0, 5 / 256.0, 17 / 256.0);
    private static final IGuiTexture RIGHT_BUTTON_HOVER = PAGE.getSubTexture(181 / 256.0, 84 / 256.0, 5 / 256.0, 17 / 256.0);
    private static final IGuiTexture LEFT_BUTTON = PAGE.getSubTexture(176 / 256.0, 101 / 256.0, 5 / 256.0, 17 / 256.0);
    private static final IGuiTexture LEFT_BUTTON_HOVER = PAGE.getSubTexture(181 / 256.0, 101 / 256.0, 5 / 256.0, 17 / 256.0);
    private static final Map<IO, IGuiTexture> LINE_0_MAP;
    private static final Map<IO, IGuiTexture> LINE_1_MAP;

    static {
        LINE_0_MAP = new EnumMap<>(IO.class);
        LINE_0_MAP.put(IO.IN, PAGE.getSubTexture(211 / 256.0, 0, 45 / 256.0, 28 / 256.0));
        LINE_0_MAP.put(IO.OUT, PAGE.getSubTexture(211 / 256.0, 28 / 256.0, 45 / 256.0, 28 / 256.0));
        LINE_0_MAP.put(IO.BOTH, PAGE.getSubTexture(211 / 256.0, 56 / 256.0, 45 / 256.0, 28 / 256.0));

        LINE_1_MAP = new EnumMap<>(IO.class);
        LINE_1_MAP.put(IO.IN, PAGE.getSubTexture(186 / 256.0, 0, 4 / 256.0, 35 / 256.0));
        LINE_1_MAP.put(IO.OUT, PAGE.getSubTexture(190 / 256.0, 0, 4 / 256.0, 35 / 256.0));
        LINE_1_MAP.put(IO.BOTH, PAGE.getSubTexture(194 / 256.0, 0, 4 / 256.0, 35 / 256.0));
    }

    private final ControllerTileEntity controller;
    private final ImageWidget[][] lines;
    private final TextTexture[] labels;

    private Map<Long, EnumMap<IO, Set<MultiblockCapability>>> capabilityMap;
    @SideOnly(Side.CLIENT)
    private Map<MultiblockCapability, IO> capabilitySettings;
    private BlockPos pos;
    int index;

    public IOPageWidget(ControllerTileEntity controller) {
        super(20, 0, 176, 256);
        this.controller = controller;
        if (controller.state.cache == null) {
            controller.checkPattern();
        }
        capabilityMap = controller.state.getMatchContext().get("capabilities");
        capabilityMap = capabilityMap == null ? new HashMap<>() : capabilityMap;
        if (controller.isRemote()) {
            capabilitySettings = new HashMap<>();
        }
        lines = new ImageWidget[2][3];
        addWidget(lines[0][0] = new ImageWidget(14, 172, 45, 28));
        addWidget(lines[0][1] = new ImageWidget(66, 172, 45, 28));
        addWidget(lines[0][2] = new ImageWidget(118, 172, 45, 28));

        addWidget(lines[1][0] = new ImageWidget(34, 202, 4, 35));
        addWidget(lines[1][1] = new ImageWidget(86, 202, 4, 35));
        addWidget(lines[1][2] = new ImageWidget(138, 202, 4, 35));

        addWidget(new SceneWidget(6, 6, 164, 143)
                .setRenderedCore(controller.state.getCache(), null)
                .setOnSelected(this::onPosSelected));
        addWidget(new ButtonWidget(4, 156, 5, 17, LEFT_BUTTON, this::onLeftClick).setHoverTexture(LEFT_BUTTON_HOVER));
        addWidget(new ButtonWidget(167, 156, 5, 17, RIGHT_BUTTON, this::onRightClick).setHoverTexture(RIGHT_BUTTON_HOVER));
        labels = new TextTexture[3];
        addWidget(new ImageWidget(11, 156, 50, 15).setImage(labels[0] = new TextTexture("Empty", -1)
                .setType(TextTexture.TextType.ROLL)
                .setWidth(50)
                .setDropShadow(true)));
        addWidget(new ImageWidget(63, 156, 50, 15).setImage(labels[1] = new TextTexture("Empty", -1)
                .setType(TextTexture.TextType.ROLL)
                .setWidth(50)
                .setDropShadow(true)));
        addWidget(new ImageWidget(115, 156, 50, 15).setImage(labels[2] = new TextTexture("Empty", -1)
                .setType(TextTexture.TextType.ROLL)
                .setWidth(50)
                .setDropShadow(true)));

        addWidget(new ButtonWidget(30, 208, 12, 12, IGuiTexture.EMPTY, cd -> click(cd, index, IO.IN)).setHoverTexture(new ColorRectTexture(0x4fffffff)));
        addWidget(new ButtonWidget(30, 229, 12, 12, IGuiTexture.EMPTY, cd -> click(cd, index, IO.OUT)).setHoverTexture(new ColorRectTexture(0x4fffffff)));
        addWidget(new ButtonWidget(82, 208, 12, 12, IGuiTexture.EMPTY, cd -> click(cd, index + 1, IO.IN)).setHoverTexture(new ColorRectTexture(0x4fffffff)));
        addWidget(new ButtonWidget(82, 229, 12, 12, IGuiTexture.EMPTY, cd -> click(cd, index + 1, IO.OUT)).setHoverTexture(new ColorRectTexture(0x4fffffff)));
        addWidget(new ButtonWidget(134, 208, 12, 12, IGuiTexture.EMPTY, cd -> click(cd, index + 2, IO.IN)).setHoverTexture(new ColorRectTexture(0x4fffffff)));
        addWidget(new ButtonWidget(134, 229, 12, 12, IGuiTexture.EMPTY, cd -> click(cd, index + 2, IO.OUT)).setHoverTexture(new ColorRectTexture(0x4fffffff)));

    }

    private void refresh() {
        List<MultiblockCapability> values = new ArrayList<>(capabilitySettings.keySet());
        for (int i = index; i < index + 3; i++) {
            MultiblockCapability capability = null;
            if (i < values.size()) {
                capability = values.get(i);
                labels[i - index].updateText(capability.name);
            } else {
                labels[i - index].updateText("Empty");
            }
            lines[0][i - index].setImage(LINE_0_MAP.get(capabilitySettings.get(capability)));
            lines[1][i - index].setImage(LINE_1_MAP.get(capabilitySettings.get(capability)));
        }
    }

    private void click(ClickData clickData, int index, IO io) {
        if (clickData.isRemote) {
            EnumMap<IO, Set<MultiblockCapability>> enumMap;
            if (pos != null && capabilityMap.containsKey(pos.toLong())) {
                enumMap = capabilityMap.get(pos.toLong());
            } else {
                return;
            }
            List<MultiblockCapability> values = new ArrayList<>(capabilitySettings.keySet());
            if (index >= 0 && index < values.size()) {
                MultiblockCapability capability = values.get(index);
                IO original = capabilitySettings.get(capability);
                boolean find = false;
                if (enumMap.get(io) != null && enumMap.get(io).contains(capability)) {
                    find = true;
                }
                if (enumMap.get(IO.BOTH) != null && enumMap.get(IO.BOTH).contains(capability)) {
                    find = true;
                }
                if (!find) return;
                if (original == null ) {
                    capabilitySettings.put(capability, io);
                } else if (original == io) {
                    capabilitySettings.put(capability, null);
                } else if (original == IO.BOTH) {
                    capabilitySettings.put(capability, io == IO.IN ? IO.OUT : IO.IN);
                } else {
                    capabilitySettings.put(capability, IO.BOTH);
                }
                writeClientAction(-1, buffer -> {
                    buffer.writeString(capability.name);
                    buffer.writeBoolean(original != null);
                    if (original != null) {
                        buffer.writeEnumValue(original);
                    }
                    IO newIO = capabilitySettings.get(capability);
                    buffer.writeBoolean(newIO != null);
                    if (newIO != null) {
                        buffer.writeEnumValue(newIO);
                    }
                });
                refresh();
            }
        }
    }

    private void onRightClick(ClickData clickData) {
        if (clickData.isRemote) {
            List<MultiblockCapability> values = new ArrayList<>(capabilitySettings.keySet());
            if (index < values.size() - 3) {
                index ++;
                refresh();
            }
        }
    }

    private void onLeftClick(ClickData clickData) {
        if (clickData.isRemote) {
            if (index > 0) {
                index --;
                refresh();
            }
        }
    }

    private void onPosSelected(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        if (!isRemote()) {
            if (!capabilityMap.containsKey(pos.toLong()) || controller.getCapabilities() == null) return;
            writeUpdateInfo(-1, buffer -> {
                long posLong = pos.toLong();
                Table<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilities = controller.getCapabilities();
                List<Tuple<IO, MultiblockCapability>> real = new ArrayList<>();
                for (Map.Entry<IO, Set<MultiblockCapability>> entry : capabilityMap.get(pos.toLong()).entrySet()) {
                    Set<MultiblockCapability> set = entry.getValue();
                    for (MultiblockCapability capability : set) {
                        if (capabilities.contains(entry.getKey(), capability) && capabilities.get(entry.getKey(), capability).containsKey(posLong)) {
                            real.add(new Tuple<>(entry.getKey(), capability));
                        }
                        if (entry.getKey() == IO.BOTH) {
                            if (capabilities.contains(IO.IN, capability) && capabilities.get(IO.IN, capability).containsKey(posLong)) {
                                real.add(new Tuple<>(IO.IN, capability));
                            }
                            if (capabilities.contains(IO.OUT, capability) && capabilities.get(IO.OUT, capability).containsKey(posLong)) {
                                real.add(new Tuple<>(IO.OUT, capability));
                            }
                        }
                    }
                }
                buffer.writeVarInt(real.size());
                for (Tuple<IO, MultiblockCapability> tuple : real) {
                    buffer.writeEnumValue(tuple.getFirst());
                    buffer.writeString(tuple.getSecond().name);
                }
            });
        } else {
            capabilitySettings.clear();
            if (capabilityMap.containsKey(pos.toLong())) {
                capabilityMap.get(pos.toLong()).values().stream().flatMap(Collection::stream).forEach(capability -> capabilitySettings.put(capability, null));
            } else {
                refresh();
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -1) {
            int size = buffer.readVarInt();
            for (int i = size; i > 0; i--) {
                IO io = buffer.readEnumValue(IO.class);
                MultiblockCapability capability = MultiblockCapabilities.CAPABILITY_REGISTRY.get(buffer.readString(Short.MAX_VALUE));
                capabilitySettings.put(capability, io);
            }
            refresh();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            MultiblockCapability capability = MultiblockCapabilities.CAPABILITY_REGISTRY.get(buffer.readString(Short.MAX_VALUE));
            Table<IO, MultiblockCapability, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilities = controller.getCapabilities();
            if (buffer.readBoolean()) {
                IO io = buffer.readEnumValue(IO.class);
                capabilities.get(io, capability).remove(pos.toLong());
                if (capabilities.get(io, capability).isEmpty()) {
                    capabilities.remove(io, capability);
                }
            }
            if (buffer.readBoolean()) {
                IO io = buffer.readEnumValue(IO.class);
                TileEntity entity = controller.getWorld().getTileEntity(pos);
                if (entity != null && capability.isBlockHasCapability(io, entity)) {
                    if (!capabilities.contains(io, capability)) {
                        capabilities.put(io, capability, new Long2ObjectOpenHashMap<>());
                    }
                    capabilities.get(io, capability).put(pos.toLong(), capability.createProxy(io, entity));
                }
            }
            controller.markDirty();
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        BACKGROUND.draw(x, y, width, height);
        super.drawInBackground(mouseX, mouseY, partialTicks);
    }
}
