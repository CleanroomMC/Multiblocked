package com.cleanroommc.multiblocked.persistence;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.pattern.MultiblockState;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.cleanroommc.multiblocked.util.world.DummyWorld;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiblockWorldSavedData extends WorldSavedData {

    // ********************************* dummy ********************************* //

    private static final MultiblockWorldSavedData DUMMY = new MultiblockWorldSavedData("dummy"){
        @Override
        public void addMapping(MultiblockState state) {
        }

        @Override
        public void addLoading(ComponentTileEntity<?> tileEntity) {
        }

        @Override
        public void createSearchingThread() {
        }
    };

    @SideOnly(Side.CLIENT)
    public static Set<BlockPos> modelDisabled;
    @SideOnly(Side.CLIENT)
    public static Map<BlockPos, Collection<BlockPos>> multiDisabled;

    public final static ThreadLocal<Boolean> isBuildingChunk = ThreadLocal.withInitial(()-> Boolean.FALSE);

    static {
        if (Multiblocked.isClient()) {
            modelDisabled = new HashSet<>();
            multiDisabled = new HashMap<>();
        }
    }

    private static WeakReference<World> worldRef;

    public static MultiblockWorldSavedData getOrCreate(World world) {
        if (world instanceof DummyWorld) {
            return DUMMY;
        }
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
    public final Map<BlockPos, ComponentTileEntity<?>> loading;

    public MultiblockWorldSavedData(String name) { // Also constructed Reflectively by MapStorage
        super(name);
        this.mapping = new Object2ObjectOpenHashMap<>();
        this.chunkPosMapping = new HashMap<>();
        this.loading = new Object2ObjectOpenHashMap<>();
    }

    public static void clearDisabled() {
        modelDisabled.clear();
        multiDisabled.clear();
    }

    public Collection<MultiblockState> getControllerInChunk(ChunkPos chunkPos) {
        return new ArrayList<>(chunkPosMapping.getOrDefault(chunkPos, Collections.emptySet()));
    }

    public Collection<ComponentTileEntity<?>> getLoadings() {
        return loading.values();
    }

    public void addMapping(MultiblockState state) {
        this.mapping.put(state.controllerPos, state);
        for (BlockPos blockPos : state.getCache()) {
            chunkPosMapping.computeIfAbsent(new ChunkPos(blockPos), c->new HashSet<>()).add(state);
        }
        setDirty(true);
    }

    public void removeMapping(MultiblockState state) {
        this.mapping.remove(state.controllerPos);
        for (Set<MultiblockState> set : chunkPosMapping.values()) {
            set.remove(state);
        }
        setDirty(true);
    }

    public void addLoading(ComponentTileEntity<?> tileEntity) {
        ComponentTileEntity<?> last = loading.put(tileEntity.getPos(), tileEntity);
        if (last != tileEntity) {
            if (last instanceof IAsyncThreadUpdate) {
                asyncComponents.remove(last);
                if (asyncComponents.isEmpty()) {
                    releaseSearchingThread();
                }
            }
            if (tileEntity instanceof IAsyncThreadUpdate) {
                asyncComponents.add((ControllerTileEntity) tileEntity);
                createSearchingThread();
            }
        }
    }

    public void removeLoading(BlockPos componentPos) {
        ComponentTileEntity<?> component = loading.remove(componentPos);
        if (component instanceof IAsyncThreadUpdate) {
            asyncComponents.remove(component);
            if (asyncComponents.isEmpty()) {
                releaseSearchingThread();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void removeDisableModel(BlockPos controllerPos) {
        Collection<BlockPos> poses = multiDisabled.remove(controllerPos);
        if (poses == null) return;
        modelDisabled.clear();
        multiDisabled.values().forEach(modelDisabled::addAll);
        updateRenderChunk(poses);
    }

    @SideOnly(Side.CLIENT)
    private static void updateRenderChunk(Collection<BlockPos> poses) {
        World world = Minecraft.getMinecraft().world;
        if (world != null) {
            for (BlockPos pos : poses) {
                world.markBlockRangeForRenderUpdate(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void addDisableModel(BlockPos controllerPos, Collection<BlockPos> poses) {
        multiDisabled.put(controllerPos, poses);
        modelDisabled.addAll(poses);
        updateRenderChunk(poses);
    }

    @SideOnly(Side.CLIENT)
    public static boolean isModelDisabled(BlockPos pos) {
        if (isBuildingChunk.get()) {
            return modelDisabled.contains(pos);
        }
        return false;
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

    // ********************************* thread for searching ********************************* //
    private final CopyOnWriteArrayList<IAsyncThreadUpdate> asyncComponents = new CopyOnWriteArrayList<>();
    private Thread thread;
    private long periodID = Multiblocked.RNG.nextLong();
    private float tps = 4;

    public void createSearchingThread() {
        if (thread != null && !thread.isInterrupted()) return;
        thread = new Thread(this::searchingTask);
        thread.start();
    }

    private void searchingTask() {
        long tpsST = System.currentTimeMillis();
        while (!Thread.interrupted()) {
            long st = System.currentTimeMillis();
            try {
                for (IAsyncThreadUpdate asyncComponent : asyncComponents) {
                    asyncComponent.asyncThreadLogic(periodID);
                }
            } catch (Throwable e) {
                Multiblocked.LOGGER.error("asyncThreadLogic error: {}", e.getMessage());
            }
            periodID++;
            long et = System.currentTimeMillis();
            if (periodID % 20 == 0) {
                tps = Math.min((et - tpsST) / 1250f, 4);
                tpsST = et;
            }
            long dur = (et - st);
            if (dur < 250) {
                try {
                    Thread.sleep(Math.min(250, 250 - dur));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public void releaseSearchingThread() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
    }

    public long getPeriodID() {
        return periodID;
    }

    public float getTPS () {
        return tps;
    }

}
