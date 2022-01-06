package io.github.cleanroommc.multiblocked.persistence;

import io.github.cleanroommc.multiblocked.network.MultiblockedNetworking;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import io.github.cleanroommc.multiblocked.api.framework.structure.MultiblockInstance;
import io.github.cleanroommc.multiblocked.network.packet.PacketSyncMultiblockWorldSavedData;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;

public class MultiblockWorldSavedData extends WorldSavedData {

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

    private final Map<BlockPos, MultiblockInstance> mapping;

    public MultiblockWorldSavedData(String name) { // Also constructed Reflectively by MapStorage
        super(name);
        this.mapping = new Object2ObjectOpenHashMap<>();
    }

    public Map<BlockPos, MultiblockInstance> getMapping() {
        return mapping;
    }

    public Collection<MultiblockInstance> getInstances() {
        return mapping.values();
    }

    public void addMapping(World world, BlockPos pos, MultiblockInstance multiblock) {
        this.mapping.put(pos, multiblock);
        setDirty(true);
        MultiblockedNetworking.sendToWorld(new PacketSyncMultiblockWorldSavedData(pos, multiblock, false), world);
    }

    public void removeMapping(World world, BlockPos pos) {
        MultiblockInstance multiblock = this.mapping.remove(pos);
        setDirty(true);
        MultiblockedNetworking.sendToWorld(new PacketSyncMultiblockWorldSavedData(pos, multiblock, true), world);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        for (String key : nbt.getKeySet()) {
            BlockPos pos = BlockPos.fromLong(Long.parseLong(key));
            MultiblockInstance multiblock = new MultiblockInstance();
            multiblock.deserializeNBT(nbt.getCompoundTag(key));
            if (multiblock.fastValidate(worldRef.get())) {
                this.mapping.put(pos, multiblock);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        this.mapping.forEach((pos, multiblock) -> compound.setTag(String.valueOf(pos.toLong()), multiblock.serializeNBT()));
        return compound;
    }

}
