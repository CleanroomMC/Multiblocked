package com.cleanroommc.multiblocked.api.capability;

import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumMap;

public class CommonCapabilityProxyHolder implements ICapabilityProxyHolder{
    protected Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> capabilities;

    public CommonCapabilityProxyHolder(World world, BlockPos pos, MultiblockCapability<?>... capability) {
        TileEntity te = world.getTileEntity(pos);
        if (te != null) {
            capabilities = Tables.newCustomTable(new EnumMap<>(IO.class), Object2ObjectOpenHashMap::new);
            for (MultiblockCapability<?> cap : capability) {
                if (cap.isBlockHasCapability(IO.BOTH, te)) {
                    capabilities.put(IO.BOTH, cap, new Long2ObjectOpenHashMap<>());
                    capabilities.get(IO.BOTH, cap).put(te.getPos().toLong(), cap.createProxy(IO.BOTH, te));
                    continue;
                }
                if (cap.isBlockHasCapability(IO.IN, te)) {
                    capabilities.put(IO.IN, cap, new Long2ObjectOpenHashMap<>());
                    capabilities.get(IO.IN, cap).put(te.getPos().toLong(), cap.createProxy(IO.IN, te));
                } else if (cap.isBlockHasCapability(IO.OUT, te)) {
                    capabilities.put(IO.OUT, cap, new Long2ObjectOpenHashMap<>());
                    capabilities.get(IO.OUT, cap).put(te.getPos().toLong(), cap.createProxy(IO.OUT, te));
                }
            }
        }
    }

    @Override
    public Table<IO, MultiblockCapability<?>, Long2ObjectOpenHashMap<CapabilityProxy<?>>> getCapabilities() {
        return capabilities;
    }
}
