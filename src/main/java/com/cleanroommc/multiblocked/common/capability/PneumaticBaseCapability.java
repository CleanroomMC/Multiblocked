package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.block.Blockss;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

/**
 * @author youyihj
 */
public abstract class PneumaticBaseCapability<T> extends MultiblockCapability<T> {

    protected PneumaticBaseCapability(String name, int color) {
        super(name, color);
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof IPneumaticMachine;
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[] {new BlockInfo(Blockss.PRESSURE_CHAMBER_WALL)};
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public abstract CapabilityTrait createTrait();

    public static abstract class Proxy<K> extends CapabilityProxy<K> {

        public Proxy(MultiblockCapability<? super K> capability, TileEntity tileEntity) {
            super(capability, tileEntity);
        }

        public IAirHandler getAirHandler() {
            IPneumaticMachine machine = (IPneumaticMachine) getTileEntity();
            if (machine != null) {
                for (EnumFacing value : EnumFacing.values()) {
                    IAirHandler airHandler = machine.getAirHandler(value);
                    if (airHandler != null) return airHandler;
                }
            }
            return null;
        }

        int air;
        int volume;

        @Override
        protected boolean hasInnerChanged() {
            IAirHandler airHandler = getAirHandler();
            if (airHandler == null || airHandler.getAir() == air || airHandler.getVolume() == volume) {
                return false;
            }
            air = airHandler.getAir();
            volume = airHandler.getVolume();
            return true;
        }
    }
}
