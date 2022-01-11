package io.github.cleanroommc.multiblocked.client.particle;

import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleHandler {

    public static void addHitEffects(IBlockState iblockstate, World world, RayTraceResult target, ParticleManager manager, TextureAtlasSprite atlasSprite) {
        BlockPos pos = target.getBlockPos();
        EnumFacing side = target.sideHit;
        if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, pos);
            double d0 = (double)i + Multiblocked.RNG.nextFloat() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
            double d1 = (double)j + Multiblocked.RNG.nextFloat() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
            double d2 = (double)k + Multiblocked.RNG.nextFloat() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

            if (side == EnumFacing.DOWN) {
                d1 = (double)j + axisalignedbb.minY - 0.10000000149011612D;
            }
            else if (side == EnumFacing.UP) {
                d1 = (double)j + axisalignedbb.maxY + 0.10000000149011612D;
            }
            else if (side == EnumFacing.NORTH) {
                d2 = (double)k + axisalignedbb.minZ - 0.10000000149011612D;
            }
            else if (side == EnumFacing.SOUTH) {
                d2 = (double)k + axisalignedbb.maxZ + 0.10000000149011612D;
            }
            else if (side == EnumFacing.WEST)
            {
                d0 = (double)i + axisalignedbb.minX - 0.10000000149011612D;
            }
            else if (side == EnumFacing.EAST)
            {
                d0 = (double)i + axisalignedbb.maxX + 0.10000000149011612D;
            }
            Particle particle = (new CustomParticleDigging(world, d0, d1, d2, 0.0D, 0.0D, 0.0D, iblockstate))
                    .setBlockPos(pos)
                    .multiplyVelocity(0.2F)
                    .multipleParticleScaleBy(0.6F);
            particle.setParticleTexture(atlasSprite);
            manager.addEffect(particle);
        }
    }

    public static void addBlockDestroyEffects(IBlockState state, World world, BlockPos pos, ParticleManager manager, TextureAtlasSprite atlasSprite) {
        state = state.getActualState(world, pos);
        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 4; ++k) {
                for (int l = 0; l < 4; ++l) {
                    double d0 = ((double)j + 0.5D) / 4.0D;
                    double d1 = ((double)k + 0.5D) / 4.0D;
                    double d2 = ((double)l + 0.5D) / 4.0D;
                    Particle particle = (new CustomParticleDigging(world, pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2, d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, state))
                            .setBlockPos(pos);
                    particle.setParticleTexture(atlasSprite);
                    manager.addEffect(particle);
                }
            }
        }
    }

    public static void addBlockRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity, TextureAtlasSprite atlasSprite) {
        double posX = entity.posX + (Multiblocked.RNG.nextFloat() - 0.5) * entity.width;
        double posY = entity.getEntityBoundingBox().minY + 0.1;
        double posZ = entity.posZ + (Multiblocked.RNG.nextFloat() - 0.5) * entity.width;
        ParticleManager manager = Minecraft.getMinecraft().effectRenderer;

        Particle particle = new CustomParticleDigging(world, posX, posY, posZ, -entity.motionX * 4.0, 1.5, -entity.motionZ * 4.0, state);
        particle.setParticleTexture(atlasSprite);
        manager.addEffect(particle);
    }
}
