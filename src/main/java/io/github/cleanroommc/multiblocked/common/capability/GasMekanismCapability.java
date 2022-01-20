package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.proxy.GasMekanismCapabilityProxy;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class GasMekanismCapability extends MultiblockCapability {
    public static final GasMekanismCapability CAP = new GasMekanismCapability();

    private GasMekanismCapability() {
        super("mek_gas");
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, null);
    }

    @Override
    public GasMekanismCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new GasMekanismCapabilityProxy(tileEntity);
    }
}
