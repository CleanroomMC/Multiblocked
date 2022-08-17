package com.cleanroommc.multiblocked.api.block;

import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.client.model.IModelSupplier;
import com.cleanroommc.multiblocked.client.model.SimpleStateMapper;
import com.cleanroommc.multiblocked.client.particle.MCParticleHandler;
import com.cleanroommc.multiblocked.client.renderer.ComponentRenderer;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.util.RayTraceUtils;
import com.cleanroommc.multiblocked.util.Vector3;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import moze_intel.projecte.gameObjs.CreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class BlockComponent extends Block implements IModelSupplier, ITileEntityProvider {
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(Multiblocked.MODID, "component_block"), "normal");
    public final ComponentDefinition definition;

    public BlockComponent(ComponentDefinition definition) {
        super(Material.IRON);
        setHardness(1.5f);
        setSoundType(SoundType.METAL);
        setResistance(10.0F);
        if (definition != null) {
            for (CreativeTabs tab : CreativeTabs.CREATIVE_TAB_ARRAY) {
                if (tab.getTabLabel().equals(definition.properties.tabGroup)) {
                    setCreativeTab(tab);
                    break;
                }
            }
            setRegistryName(definition.location);
            setTranslationKey(definition.location.getNamespace() + "." + definition.location.getPath());
            setHardness(definition.properties.destroyTime);
            setHarvestLevel("pickaxe", definition.properties.harvestLevel);
            setResistance(definition.properties.explosionResistance);
        }
        this.definition = definition;
    }

    @Override
    public boolean isCollidable() {
        return definition.properties.hasCollision;
    }

    @Override
    public boolean causesSuffocation(@Nonnull IBlockState state) {
        return definition.properties.isOpaque;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public ComponentTileEntity<?> getComponent(IBlockAccess blockAccess, BlockPos pos) {
        TileEntity instance = blockAccess.getTileEntity(pos);
        return instance instanceof ComponentTileEntity<?> ? ((ComponentTileEntity<?>) instance) : null;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        ComponentTileEntity<?> componentTileEntity = getComponent(worldIn, pos);
        if (componentTileEntity != null) {
            if (componentTileEntity.isValidFrontFacing(EnumFacing.UP)) {
                componentTileEntity.setFrontFacing(EnumFacing.getDirectionFromEntityLiving(pos, placer));
            } else {
                componentTileEntity.setFrontFacing(placer.getHorizontalFacing().getOpposite());
            }
            if (placer instanceof EntityPlayer) {
                componentTileEntity.setOwner(placer.getUniqueID());
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return definition.getStackForm();
    }

    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        ComponentTileEntity<?> instance = getComponent(worldIn, pos);
        if (instance != null) {
            for (AxisAlignedBB boundingBox : instance.getCollisionBoundingBox()) {
                AxisAlignedBB offset = boundingBox.offset(pos);
                if (offset.intersects(entityBox)) collidingBoxes.add(offset);
            }
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        ComponentTileEntity<?> instance = getComponent(worldIn, pos);
        if (instance != null) {
            return RayTraceUtils.rayTraceClosest(pos, new Vector3(start), new Vector3(end), instance.getCollisionBoundingBox());
        }
        return this.rayTrace(pos, start, end, blockState.getBoundingBox(worldIn, pos));
    }

    @Override
    public boolean rotateBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing axis) {
        ComponentTileEntity<?> instance = getComponent(world, pos);
        return instance != null && instance.setFrontFacing(axis);
    }

    @Nullable
    @Override
    public EnumFacing[] getValidRotations(@Nonnull World world, @Nonnull BlockPos pos) {
        ComponentTileEntity<?> instance = getComponent(world, pos);
        return instance == null ? null : Arrays.stream(EnumFacing.VALUES)
                .filter(instance::isValidFrontFacing)
                .toArray(EnumFacing[]::new);
    }

    protected final ThreadLocal<ComponentTileEntity<?>> componentBroken = new ThreadLocal<>();


    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        componentBroken.set(getComponent(worldIn, pos));
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
        componentBroken.set(te instanceof ComponentTileEntity ? (ComponentTileEntity<?>) te : componentBroken.get());
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        componentBroken.set(null);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        ComponentTileEntity<?> instance = componentBroken.get();
        if (instance == null) return;
        instance.onDrops(drops, harvesters.get());
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ComponentTileEntity<?> instance = getComponent(worldIn, pos);
        if (instance != null) {
            return instance.onRightClick(playerIn, hand, facing, hitX, hitY, hitZ);
        }
        return true;
    }

    @Override
    public void onBlockClicked(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EntityPlayer playerIn) {
        ComponentTileEntity<?> instance = getComponent(worldIn, pos);
        if (instance != null) {
            instance.onLeftClick(playerIn);
        }
    }

    @Override
    public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        ComponentTileEntity<?> instance = getComponent(world, pos);
        return instance != null && instance.canConnectRedstone(side == null ? null : side.getOpposite());
    }

    @Override
    public boolean shouldCheckWeakPower(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return false;
    }

    @Override
    public int getWeakPower(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        ComponentTileEntity<?> instance = getComponent(blockAccess, pos);
        return instance == null ? 0 : instance.getOutputRedstoneSignal(side.getOpposite());
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        ComponentTileEntity<?> instance = getComponent(worldIn, pos);
        if (instance != null) {
            instance.onNeighborChanged();
        }
    }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return definition == null || definition.properties.isOpaque;
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return definition.properties.isOpaque;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return isOpaqueCube(state);
    }

    @Override
    public int getPackedLightmapCoords(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        if (isOpaqueCube(state)) return super.getPackedLightmapCoords(state, source, pos);
        int i = source.getCombinedLight(pos, 0);
        int j = source.getCombinedLight(pos.up(), 0);
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (Math.max(k, l)) | (Math.max(i1, j1)) << 16;
    }

    @Override
    public int getLightOpacity(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return isOpaqueCube(state) ? 255 : 0;
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        items.add(new ItemStack(MbdComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location), 1));
    }

    @Override
    public boolean canEntityDestroy(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        return !(entity instanceof EntityWither || entity instanceof EntityWitherSkull);
    }

    @Override
    @Nonnull
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        if (Multiblocked.isClient()) {
            return ComponentRenderer.COMPONENT_RENDER_TYPE;
        } else {
            return EnumBlockRenderType.MODEL;
        }
    }

    @Override
    public boolean shouldSideBeRendered(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return !isOpaqueCube(blockState) || super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleTexture(World world, BlockPos blockPos) {
        ComponentTileEntity<?> tileEntity = getComponent(world, blockPos);
        if (tileEntity != null) {
            IRenderer renderer = tileEntity.getRenderer();
            if (renderer != null) {
                return renderer.getParticleTexture();
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(@Nonnull IBlockState state, @Nonnull World worldObj, RayTraceResult target, @Nonnull ParticleManager manager) {
        TextureAtlasSprite atlasSprite = getParticleTexture(worldObj, target.getBlockPos());
        if (atlasSprite == null) return true;
        MCParticleHandler.addHitEffects(state, worldObj, target, manager, atlasSprite);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ParticleManager manager) {
        TextureAtlasSprite atlasSprite = getParticleTexture(world, pos);
        if (atlasSprite == null) return true;
        MCParticleHandler.addBlockDestroyEffects(world.getBlockState(pos), world, pos, manager, atlasSprite);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addRunningEffects(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if (world.isRemote) {
            TextureAtlasSprite atlasSprite = getParticleTexture(world, pos);
            if (atlasSprite == null) return true;
            MCParticleHandler.addBlockRunningEffects(state, world, pos, entity, atlasSprite);
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, new SimpleStateMapper(MODEL_LOCATION));
        for (IBlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state), MODEL_LOCATION);
        }
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return definition.createNewTileEntity(worldIn);
    }

    @Override
    @ParametersAreNonnullByDefault
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof ComponentTileEntity) {
            ComponentTileEntity<?> component = (ComponentTileEntity<?>) tileEntity;
            return component.getDefinition().getStatus(component.getStatus()).getLightEmissive();
        } else {
            return super.getLightValue(state, world, pos);
        }
    }
}
