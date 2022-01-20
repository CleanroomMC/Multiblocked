package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.common.capability.proxy.AspectThaumcraftCapabilityProxy;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.IAspectContainer;

import javax.annotation.Nonnull;

public class AspectThaumcraftCapability extends MultiblockCapability {

    public AspectThaumcraftCapability() {
        super("tc6_aspect");
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof IAspectContainer;
    }

    @Override
    public AspectThaumcraftCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new AspectThaumcraftCapabilityProxy(tileEntity);
    }
}
