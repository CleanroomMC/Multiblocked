package io.github.cleanroommc.multiblocked.persistence;

import io.github.cleanroommc.multiblocked.api.pattern.MultiblockState;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiblockWorldSavedData extends WorldSavedData {

    @SideOnly(Side.CLIENT)
    public static Set<BlockPos> modelDisabled = new HashSet<>();

    private static WeakReference<World> worldRef;

    public static MultiblockWorldSavedData getOrCreate(World world) {
        MapStorage perWorldStorage = world.getPerWorldStorage();
        String name = getName(world);
        worldRef = new WeakReference<>(world);
        MultiblockWorldSavedData mbwsd = (MultiblockWorldSavedData) perWorldStorage.getOrLoadData(MultiblockWorldSavedData.class, name);
        worldRef = null;
        if (mbwsd == null) {
            perWorldStorage.setData(name, mbwsd = new MultiblockWorldSavedData(name));
        }
        return mbwsd;
    }

    private static String getName(World world) {
        return "Multiblocked" + world.provider.getDimensionType().getSuffix();
    }

    public final Map<BlockPos, MultiblockState> mapping;
    public final Map<ChunkPos, Set<MultiblockState>> chunkPosMapping;
    public final Set<ComponentTileEntity<?>> loading;

    public MultiblockWorldSavedData(String name) { // Also constructed Reflectively by MapStorage
        super(name);
        this.mapping = new Object2ObjectOpenHashMap<>();
        this.chunkPosMapping = new HashMap<>();
        this.loading = new ObjectOpenHashSet<>();
    }

    public Set<MultiblockState> getControllerInChunk(ChunkPos chunkPos) {
        return chunkPosMapping.getOrDefault(chunkPos, Collections.emptySet());
    }

    public Collection<MultiblockState> getInstances() {
        return mapping.values();
    }

    public Collection<ComponentTileEntity<?>> getLoadings() {
        return loading;
    }

    public void addMapping(MultiblockState state) {
        this.mapping.put(state.getController().getPos(), state);
        for (BlockPos blockPos : state.getCache()) {
            chunkPosMapping.computeIfAbsent(new ChunkPos(blockPos), c->new HashSet<>()).add(state);
        }
        addLoading(state.getController());
        setDirty(true);
    }

    public void removeMapping(MultiblockState state) {
        this.mapping.remove(state.getController().getPos());
        for (Set<MultiblockState> set : chunkPosMapping.values()) {
            set.remove(state);
        }
        setDirty(true);
    }

    public void addLoading(ComponentTileEntity<?> state) {
        loading.add(state);
    }

    public void removeLoading(ComponentTileEntity<?> state) {
        loading.remove(state);
    }

    @SideOnly(Side.CLIENT)
    public static void removeDisableModel(Collection<BlockPos> pos) {
        modelDisabled.removeAll(pos);
    }

    @SideOnly(Side.CLIENT)
    public static void addDisableModel(Collection<BlockPos> pos) {
        modelDisabled.addAll(pos);
    }

    @SideOnly(Side.CLIENT)
    public static boolean isModelDisabled(BlockPos pos) {
        return modelDisabled.contains(pos);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        for (String key : nbt.getKeySet()) {
            BlockPos pos = BlockPos.fromLong(Long.parseLong(key));
            MultiblockState state = new MultiblockState(worldRef.get(), pos);
            state.deserialize(new PacketBuffer(Unpooled.copiedBuffer(nbt.getByteArray(key))));
            this.mapping.put(pos, state);
            for (BlockPos blockPos : state.getCache()) {
                chunkPosMapping.computeIfAbsent(new ChunkPos(blockPos), c->new HashSet<>()).add(state);
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        this.mapping.forEach((pos, state) -> {
            ByteBuf byteBuf = Unpooled.buffer();
            state.serialize(new PacketBuffer(byteBuf));
            compound.setByteArray(String.valueOf(pos.toLong()), Arrays.copyOfRange(byteBuf.array(), 0, byteBuf.writerIndex()));
        });
        return compound;
    }
}
