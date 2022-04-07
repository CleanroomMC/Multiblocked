package com.cleanroommc.multiblocked.api.crafttweaker;

import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTLaserParticle;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTTextureParticle;
import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.LaserBeamParticle;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import com.cleanroommc.multiblocked.util.Vector3;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.util.Position3f;
import crafttweaker.api.world.IWorld;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.multiblocked.MBDParticle")
public class CTParticleUtils {

    @ZenMethod
    public static ICTTextureParticle texture(IWorld world, double x, double y, double z, @Optional boolean isShader) {
        if (world.isRemote()) {
            return isShader ? new ShaderTextureParticle(CraftTweakerMC.getWorld(world), x, y, z) : new CommonParticle(CraftTweakerMC.getWorld(world), x, y, z);
        }
        return null;
    }

    @ZenMethod
    public static ICTTextureParticle texture(IWorld world, Position3f position, @Optional boolean isShader) {
        if (world.isRemote()) {
            return isShader ? new ShaderTextureParticle(CraftTweakerMC.getWorld(world), position.getX(), position.getY(), position.getZ()) : new CommonParticle(CraftTweakerMC.getWorld(world), position.getX(), position.getY(), position.getZ());
        }
        return null;
    }

    @ZenMethod
    public static ICTTextureParticle texture(IWorld world, double x, double y, double z, double xSpeedIn, double ySpeedIn, double zSpeedIn, @Optional boolean isShader) {
        if (world.isRemote()) {
            return isShader ? new ShaderTextureParticle(CraftTweakerMC.getWorld(world), x, y, z, xSpeedIn, ySpeedIn, zSpeedIn) : new CommonParticle(CraftTweakerMC.getWorld(world), x, y, z, xSpeedIn, ySpeedIn, zSpeedIn);
        }
        return null;
    }

    @ZenMethod
    public static ICTTextureParticle texture(IWorld world, Position3f position, Position3f speed, @Optional boolean isShader) {
        if (world.isRemote()) {
            return isShader ? new ShaderTextureParticle(CraftTweakerMC.getWorld(world), position.getX(), position.getY(), position.getZ(), speed.getX(), speed.getY(), speed.getZ()) :
                    new CommonParticle(CraftTweakerMC.getWorld(world), position.getX(), position.getY(), position.getZ(), speed.getX(), speed.getY(), speed.getZ());
        }
        return null;
    }

    @ZenMethod
    public static ICTLaserParticle laser(IWorld world, double sX, double sY, double sZ, double eX, double eY, double eZ) {
        if (world.isRemote()) {
            return new LaserBeamParticle(CraftTweakerMC.getWorld(world), new Vector3(sX, sX, sZ), new Vector3(eX, eY, eZ));
        }
        return null;
    }

    @ZenMethod
    public static ICTLaserParticle laser(IWorld world, Position3f startPos, Position3f endPos) {
        if (world.isRemote()) {
            return new LaserBeamParticle(CraftTweakerMC.getWorld(world), new Vector3(startPos.getX(), startPos.getY(), startPos.getZ()), new Vector3(endPos.getX(), endPos.getY(), endPos.getZ()));
        }
        return null;
    }

}
