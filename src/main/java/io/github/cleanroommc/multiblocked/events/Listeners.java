package io.github.cleanroommc.multiblocked.events;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.framework.structure.Multiblock;
import io.github.cleanroommc.multiblocked.api.framework.structure.MultiblockInstance;
import io.github.cleanroommc.multiblocked.api.framework.structure.MultiblockInstance.Status;
import io.github.cleanroommc.multiblocked.api.framework.structure.definition.NameDefinition;
import io.github.cleanroommc.multiblocked.network.MultiblockedNetworking;
import io.github.cleanroommc.multiblocked.network.packet.PacketSyncMultiblockWorldSavedData;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;

import java.util.Collection;
import java.util.Map;

public class Listeners {

    @SideOnly(Side.CLIENT) private static Int2ObjectMap<Map<BlockPos, MultiblockInstance>> multiblockInstances;

    @SideOnly(Side.CLIENT)
    public static void validateMapping() {
        if (multiblockInstances == null) {
            multiblockInstances = new Int2ObjectArrayMap<>(1);
        }
        int dim = Minecraft.getMinecraft().world.provider.getDimension();
        if (!multiblockInstances.containsKey(dim)) {
            multiblockInstances.put(dim, new Object2ReferenceOpenHashMap<>());
        }
    }

    @SideOnly(Side.CLIENT)
    public static void voidMapping() {
        multiblockInstances = null;
    }

    @SideOnly(Side.CLIENT)
    public static void setMultiblockMapping(Map<BlockPos, MultiblockInstance> newMapping) {
        validateMapping();
        multiblockInstances.put(Minecraft.getMinecraft().world.provider.getDimension(), new Object2ReferenceOpenHashMap<>(newMapping));
    }

    @SideOnly(Side.CLIENT)
    public static void updateMultiblockMapping(BlockPos pos, MultiblockInstance multiblock, boolean toRemove) {
        validateMapping();
        Map<BlockPos, MultiblockInstance> mapping = multiblockInstances.get(Minecraft.getMinecraft().world.provider.getDimension());
        if (toRemove) {
            MultiblockInstance multiblockRemoved = mapping.remove(pos);
            if (multiblockRemoved == null) {
                Multiblocked.LOGGER.warn("Client tried to remove a ghost mapping from BlockPos: {}, Multiblock: {}", pos, multiblock.getMultiblock());
            }
        } else {
            mapping.put(pos, multiblock);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void renderMultiblockNames(RenderWorldLastEvent event) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        Entity entity = renderManager.renderViewEntity;
        if (entity == null || entity.world == null || multiblockInstances == null) {
            return;
        }
        Map<BlockPos, MultiblockInstance> mapping = multiblockInstances.get(entity.world.provider.getDimension());
        if (mapping == null || mapping.isEmpty()) {
            return;
        }
        // TODO: Fairly optimizable, check frustrum etc
        mapping.forEach((pos, multiblock) -> {
            Pair<Vec3i, NameDefinition> name = multiblock.getMultiblock().getNameTag();
            if (name != null && entity.getDistanceSq(pos) <= 4096D) {
                // BlockPos nameplatePos = Utils.rotate(pos, name.getLeft(), multiblock.getFacing());
                BlockPos nameplatePos = multiblock.getNameTagPos();
                EntityRenderer.drawNameplate(
                        renderManager.getFontRenderer(),
                        multiblock.getMultiblock().getNametagText(multiblock),
                        (float) (nameplatePos.getX() - renderManager.renderPosX + 0.5),
                        (float) (nameplatePos.getY() - renderManager.renderPosY + 0.5),
                        (float) (nameplatePos.getZ() - renderManager.renderPosZ + 0.5),
                        0,
                        renderManager.playerViewY, renderManager.playerViewX,
                        renderManager.options.thirdPersonView == 2,
                        false);
            }
        });
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            if (multiblockInstances != null) {
                multiblockInstances.remove(event.getWorld().provider.getDimension()); // Remove client-side cache
            }
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()); // Pre-load
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()).getInstances()
                    .forEach(multiblock -> {
                        ChunkPos pos = event.getChunk().getPos();
                        if (multiblock.isInChunk(pos)) {
                            multiblock.notify$();
                        }
                    });
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()).getInstances()
                    .forEach(multiblock -> {
                        ChunkPos pos = event.getChunk().getPos();
                        if (multiblock.isInChunk(pos)) {
                            multiblock.setStatus(Status.UNLOADED);
                        }
                    });
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        if (!world.isRemote && event.getEntity() instanceof EntityPlayer) {
            MultiblockedNetworking.sendToPlayer(new PacketSyncMultiblockWorldSavedData(MultiblockWorldSavedData.getOrCreate(world).getMapping()), (EntityPlayerMP) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void formMultiblockAttempt(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        IBlockState state = world.getBlockState(event.getPos());
        Collection<Multiblock> candidates = Multiblock.getCandidates(state, stack);
        EnumFacing checkAgainst = event.getFace().getOpposite();
        for (Multiblock candidate : candidates) {
            if (candidate.check(world, event.getPos(), checkAgainst)) {
                EntityPlayer player = event.getEntityPlayer();
                event.setCanceled(true);
                player.swingArm(event.getHand());
                ITextComponent formedMsg = new TextComponentTranslation("multiblocked.multiblock.formed", candidate.getLocalizedName());
                player.sendStatusMessage(formedMsg, true);
                if (!player.isCreative() && candidate.toConsumeCatalyst()) {
                    stack.shrink(1);
                }
                MultiblockWorldSavedData.getOrCreate(world).addMapping(world, event.getPos(), new MultiblockInstance(candidate, event.getPos(), checkAgainst));
                return;
            }
        }
    }

}
