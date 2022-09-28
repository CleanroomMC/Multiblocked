package com.cleanroommc.multiblocked;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Loader;

@Config(modid = Multiblocked.MODID)
public final class MbdConfig {

    public static NaturesAura naturesAura = Loader.isModLoaded(Multiblocked.MODID_NA) ? new NaturesAura() : null;

    @Config.Comment({"location of the mbd scripts and resources.", "Default: {.../config}/multiblocked"})
    @Config.RequiresMcRestart
    public static String location = "multiblocked";

    @Config.Comment({"If true, built-in parts (Item Input Bus, Item Output Bus, etc.) are enabled.", "Default: true"})
    @Config.RequiresMcRestart
    public static boolean enableBuiltInComponents = true;

    public static class NaturesAura {
        @Config.Comment({"set the radius of aura value consumption.", "Default: 20"})
        public int radius = 20;
    }

}
