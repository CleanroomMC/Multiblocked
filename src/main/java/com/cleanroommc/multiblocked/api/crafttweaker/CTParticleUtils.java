package com.cleanroommc.multiblocked.api.crafttweaker;

import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTLaserParticle;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTTextureParticle;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IParticleWidget;
import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.LaserBeamParticle;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import com.cleanroommc.multiblocked.util.Vector3;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.util.Position3f;
import crafttweaker.api.world.IWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.multiblocked.MBDParticle")
public class CTParticleUtils {

    @ZenMethod
    @SideOnly(Side.CLIENT)
    public static ICTTextureParticle texture(IWorld world, double x, double y, double z, @Optional boolean isShader) {
        return isShader ? new ShaderTextureParticle(CraftTweakerMC.getWorld(world), x, y, z) : new CommonParticle(CraftTweakerMC.getWorld(world), x, y, z);
    }

    @ZenMethod
    @SideOnly(Side.CLIENT)
    public static ICTTextureParticle texture(IWorld world, Position3f position, @Optional boolean isShader) {
        return isShader ? new ShaderTextureParticle(CraftTweakerMC.getWorld(world), position.getX(), position.getY(), position.getZ()) : new CommonParticle(CraftTweakerMC.getWorld(world), position.getX(), position.getY(), position.getZ());
    }

    @ZenMethod
    @SideOnly(Side.CLIENT)
    public static ICTLaserParticle laser(IWorld world, double sX, double sY, double sZ, double eX, double eY, double eZ) {
        return new LaserBeamParticle(CraftTweakerMC.getWorld(world), new Vector3(sX, sY, sZ), new Vector3(eX, eY, eZ));
    }

    @ZenMethod
    @SideOnly(Side.CLIENT)
    public static ICTLaserParticle laser(IWorld world, Position3f startPos, Position3f endPos) {
        return new LaserBeamParticle(CraftTweakerMC.getWorld(world), new Vector3(startPos.getX(), startPos.getY(), startPos.getZ()), new Vector3(endPos.getX(), endPos.getY(), endPos.getZ()));
    }

    @ZenMethod
    @SideOnly(Side.CLIENT)
    public static IWorld getWorld() {
        World world = IParticleWidget.getWorld();
        return CraftTweakerMC.getIWorld(world == null ? Minecraft.getMinecraft().world : world);
    }

}
