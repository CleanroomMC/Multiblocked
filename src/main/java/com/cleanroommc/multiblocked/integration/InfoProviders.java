package com.cleanroommc.multiblocked.integration;

import com.cleanroommc.multiblocked.integration.provider.RecipeProgressInfoProvider;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;

public class InfoProviders {

    public static void registerTOP() {
        ITheOneProbe theOneProbe = TheOneProbe.theOneProbeImp;
        theOneProbe.registerProvider(new RecipeProgressInfoProvider());
    }
}
