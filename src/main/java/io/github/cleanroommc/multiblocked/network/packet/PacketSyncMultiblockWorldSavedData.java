package io.github.cleanroommc.multiblocked.network.packet;

import io.github.cleanroommc.multiblocked.events.Listeners;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.framework.structure.Multiblock;
import io.github.cleanroommc.multiblocked.api.framework.structure.MultiblockInstance;

import java.util.Collections;
import java.util.Map;

public class PacketSyncMultiblockWorldSavedData implements IMessage {

    /**
     * 0: setMapping
     * 1: add updateMapping
     * 2: remove updateMapping
     */
    private byte mode;
    private Map<BlockPos, MultiblockInstance> mapping;

    public PacketSyncMultiblockWorldSavedData() { }

    public PacketSyncMultiblockWorldSavedData(Map<BlockPos, MultiblockInstance> mapping) {
        this.mapping = mapping;
        this.mode = 0;
    }

    public PacketSyncMultiblockWorldSavedData(BlockPos pos, MultiblockInstance mapping, boolean remove) {
        this.mapping = Collections.singletonMap(pos, mapping);
        this.mode = (byte) (remove ? 2 : 1);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.mode = buf.readByte();
        int count = buf.readInt();
        this.mapping = new Object2ObjectOpenHashMap<>();
        for (int i = 0; i < count; i++) {
            BlockPos pos = BlockPos.fromLong(buf.readLong());
            String multiblockKey = ByteBufUtils.readUTF8String(buf);
            Multiblock multiblock = Multiblock.get(multiblockKey);
            if (multiblock == null) {
                Multiblocked.LOGGER.fatal("Server and Client mismatch! {} Multiblock exists on the server but not on the client!", multiblockKey);
            } else {
                this.mapping.put(pos, new MultiblockInstance(multiblock, pos, EnumFacing.VALUES[buf.readByte()]));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.mode);
        buf.writeInt(this.mapping.size());
        this.mapping.forEach(((pos, multiblock) -> {
            buf.writeLong(pos.toLong());
            ByteBufUtils.writeUTF8String(buf, multiblock.getMultiblock().getUnlocalizedName());
            buf.writeByte(multiblock.getFacing().getIndex());
        }));
    }

    public static class Handler implements IMessageHandler<PacketSyncMultiblockWorldSavedData, IMessage> {

        @Override
        public IMessage onMessage(PacketSyncMultiblockWorldSavedData message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    if (message.mapping != null) {
                        if (message.mode == 0) {
                            Listeners.setMultiblockMapping(message.mapping);
                        } else {
                            message.mapping.entrySet().stream()
                                    .findFirst()
                                    .ifPresent(entry -> Listeners.updateMultiblockMapping(entry.getKey(), entry.getValue(), message.mode == 2));
                        }
                    }
                });
            }
            return null;
        }

    }

}
