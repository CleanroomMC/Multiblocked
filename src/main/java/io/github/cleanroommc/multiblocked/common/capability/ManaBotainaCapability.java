package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.proxy.ManaBotainaCapabilityProxy;
import net.minecraft.tileentity.TileEntity;
import vazkii.botania.api.mana.IManaReceiver;

import javax.annotation.Nonnull;

public class ManaBotainaCapability extends MultiblockCapability {
    public static final ManaBotainaCapability CAP = new ManaBotainaCapability();

    private ManaBotainaCapability() {
        super("bot_mana");
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof IManaReceiver;
    }

    @Override
    public ManaBotainaCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new ManaBotainaCapabilityProxy(tileEntity);
    }
}
