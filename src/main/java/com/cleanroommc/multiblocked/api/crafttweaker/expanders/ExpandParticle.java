package com.cleanroommc.multiblocked.api.crafttweaker.expanders;

import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTLaserParticle;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTParticle;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTTextureParticle;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenExpansion;

@ZenRegister
@ZenExpansion("mods.multiblocked.client.Particle")
public class ExpandParticle {

    @ZenCaster
    public static ICTTextureParticle asTexture(ICTParticle particle) {
        return particle instanceof ICTTextureParticle ? (ICTTextureParticle) particle : null;
    }

    @ZenCaster
    public static ICTLaserParticle asLaser(ICTParticle particle) {
        return particle instanceof ICTLaserParticle ? (ICTLaserParticle) particle : null;
    }

}
