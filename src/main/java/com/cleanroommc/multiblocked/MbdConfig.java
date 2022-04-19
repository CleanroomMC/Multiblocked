package com.cleanroommc.multiblocked;

import net.minecraftforge.common.config.Config;

@Config(modid = Multiblocked.MODID)
public final class MbdConfig {
    @Config.Comment({"location of the mbd scripts and resources.", "Default: {.../config}/multiblocked"})
    @Config.RequiresMcRestart
    public static String location = "multiblocked";
}
