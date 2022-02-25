package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.client.renderer.scene.ISceneRenderHook;
import io.github.cleanroommc.multiblocked.client.renderer.scene.ImmediateWorldSceneRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.scene.WorldSceneRenderer;
import io.github.cleanroommc.multiblocked.client.util.RenderUtils;
import io.github.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.util.BlockPosFace;
import io.github.cleanroommc.multiblocked.util.Vector3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class SceneWidget extends WidgetGroup {
    @SideOnly(Side.CLIENT)
    protected WorldSceneRenderer renderer;
    @SideOnly(Side.CLIENT)
    protected TrackedDummyWorld dummyWorld;
    protected boolean dragging;
    protected boolean renderFacing = true;
    protected int lastMouseX;
    protected int lastMouseY;
    protected int currentMouseX;
    protected int currentMouseY;
    protected Vector3f center;
    protected float rotationYaw = 45;
    protected float rotationPitch = 45;
    protected float zoom = 5;
    protected BlockPosFace clickPosFace;
    protected BlockPosFace hoverPosFace;
    protected BlockPosFace selectedPosFace;
    protected BiConsumer<BlockPos, EnumFacing> onSelected;
    protected Set<BlockPos> core;

    public SceneWidget(int x, int y, int width, int height, World world) {
        super(x, y, width, height);
        if (Multiblocked.isClient() && world != null) {
            createScene(world);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void createScene(World world) {
        core = new HashSet<>();
        dummyWorld = new TrackedDummyWorld(world);
        dummyWorld.setRenderFilter(pos -> renderer.renderedBlocksMap.keySet().stream().anyMatch(c -> c.contains(pos)));
        renderer = new ImmediateWorldSceneRenderer(dummyWorld);
        center = new Vector3f();
        renderer.setOnLookingAt(ray -> {});
        renderer.setAfterWorldRender(this::renderBlockOverLay);
        renderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        clickPosFace = null;
        hoverPosFace = null;
        selectedPosFace = null;
    }

    @SideOnly(Side.CLIENT)
    public WorldSceneRenderer getRenderer() {
        return renderer;
    }

    @SideOnly(Side.CLIENT)
    public TrackedDummyWorld getDummyWorld() {
        return dummyWorld;
    }

    public SceneWidget setOnSelected(BiConsumer<BlockPos, EnumFacing> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public SceneWidget setClearColor(int color) {
        if (Multiblocked.isClient()) {
            renderer.setClearColor(color);
        }
        return this;
    }

    public SceneWidget setRenderFacing(boolean renderFacing) {
        this.renderFacing = renderFacing;
        return this;
    }

    public SceneWidget setRenderedCore(Collection<BlockPos> blocks, ISceneRenderHook renderHook) {
        if (Multiblocked.isClient()) {
            core.clear();
            core.addAll(blocks);
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (BlockPos vPos : blocks) {
                minX = Math.min(minX, vPos.getX());
                minY = Math.min(minY, vPos.getY());
                minZ = Math.min(minZ, vPos.getZ());
                maxX = Math.max(maxX, vPos.getX());
                maxY = Math.max(maxY, vPos.getY());
                maxZ = Math.max(maxZ, vPos.getZ());
            }
            center = new Vector3f((minX + maxX) / 2f + 0.5F, (minY + maxY) / 2f + 0.5F, (minZ + maxZ) / 2f + 0.5F);
            renderer.addRenderedBlocks(core, renderHook);
            this.zoom = (float) (3.5 * Math.sqrt(Math.max(Math.max(Math.max(maxX - minX + 1, maxY - minY + 1), maxZ - minZ + 1), 1)));
            renderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }
        return this;
    }

    @SideOnly(Side.CLIENT)
    public void renderBlockOverLay(WorldSceneRenderer renderer) {
        hoverPosFace = null;
        if (isMouseOverElement(currentMouseX, currentMouseY)) {
            RayTraceResult hit = renderer.getLastTraceResult();
            if (hit != null) {
                if (core.contains(hit.getBlockPos())) {
                    hoverPosFace = new BlockPosFace(hit.getBlockPos(), hit.sideHit);
                } else {
                    Vector3 hitPos = new Vector3(hit.hitVec);
                    World world = renderer.world;
                    Vec3d eyePos = new Vec3d(renderer.getEyePos().x, renderer.getEyePos().y, renderer.getEyePos().z);
                    hitPos.multiply(2); // Double view range to ensure pos can be seen.
                    Vec3d endPos = new Vec3d((hitPos.x - eyePos.x), (hitPos.y - eyePos.y), (hitPos.z - eyePos.z));
                    double min = Float.MAX_VALUE;
                    for (BlockPos pos : core) {
                        IBlockState blockState = world.getBlockState(pos);
                        if (blockState.getBlock() == Blocks.AIR) {
                            continue;
                        }
                        hit = blockState.collisionRayTrace(world, pos, eyePos, endPos);
                        if (hit != null && hit.typeOfHit != RayTraceResult.Type.MISS) {
                            double dist = eyePos.squareDistanceTo(new Vec3d(hit.getBlockPos()));
                            if (dist < min) {
                                min = dist;
                                hoverPosFace = new BlockPosFace(hit.getBlockPos(), hit.sideHit);
                            }
                        }
                    }
                }
            }
        }
        BlockPosFace tmp = dragging ? clickPosFace : hoverPosFace;
        if (selectedPosFace != null || tmp != null) {
            GlStateManager.pushMatrix();
            RenderUtils.useLightMap(240, 240, () -> {
                GlStateManager.disableDepth();
                if (selectedPosFace != null) {
                    drawFacingBorder(selectedPosFace, 0xff00ff00);
                }
                if (tmp != null && !tmp.equals(selectedPosFace)) {
                    drawFacingBorder(tmp, 0xffffffff);
                }
                GlStateManager.enableDepth();
            });
            GlStateManager.popMatrix();
        }
        if (selectedPosFace == null) return;
        RenderUtils.renderBlockOverLay(selectedPosFace.pos, 0, 0, 0.6f, 1.01f);
    }

    protected void drawFacingBorder(BlockPosFace posFace, int color) {
        if (!renderFacing) return;
        GlStateManager.pushMatrix();
        RenderUtils.moveToFace(posFace.pos.getX(), posFace.pos.getY(), posFace.pos.getZ(), posFace.facing);
        RenderUtils.rotateToFace(posFace.facing, null);
        GlStateManager.scale(1f / 16, 1f / 16, 0);
        GlStateManager.translate(-8, -8, 0);
        DrawerHelper.drawBorder(1, 1, 14, 14, color, 1);
        GlStateManager.popMatrix();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            selectedPosFace = new BlockPosFace(buffer.readBlockPos(), buffer.readEnumValue(EnumFacing.class));
            if (onSelected != null) {
                onSelected.accept(selectedPosFace.pos, selectedPosFace.facing);
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        Widget widget;
        if ((widget = super.mouseClicked(mouseX, mouseY, button)) != null) {
            return widget;
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            clickPosFace = hoverPosFace;
            return this;
        }
        dragging = false;
        return null;
    }

    @Override
    public Widget mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            zoom = (float) MathHelper.clamp(zoom + (wheelDelta < 0 ? 0.5 : -0.5), 3, 999);
            if (renderer != null) {
                renderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
            }
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public Widget mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (dragging) {
            rotationPitch += mouseX - lastMouseX + 360;
            rotationPitch = rotationPitch % 360;
            rotationYaw = (float) MathHelper.clamp(rotationYaw + (mouseY - lastMouseY), -89.9, 89.9);
            lastMouseY = mouseY;
            lastMouseX = mouseX;
            if (renderer != null) {
                renderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
            }
            return this;
        }
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    public Widget mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
        if (hoverPosFace != null && hoverPosFace.equals(clickPosFace)) {
            selectedPosFace = hoverPosFace;
            writeClientAction(-1, buffer -> {
                buffer.writeBlockPos(selectedPosFace.pos);
                buffer.writeEnumValue(selectedPosFace.facing);
            });
            if (onSelected != null) {
                onSelected.accept(selectedPosFace.pos, selectedPosFace.facing);
            }
        }
        clickPosFace = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        if (renderer != null) {
            renderer.render(x, y, width, height, mouseX, mouseY);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks);
        currentMouseX = mouseX;
        currentMouseY = mouseY;
    }
}
