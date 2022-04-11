package com.cleanroommc.multiblocked.client.renderer.scene;

import com.cleanroommc.multiblocked.client.particle.ParticleManager;
import com.cleanroommc.multiblocked.client.util.EntityCamera;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.PositionedRect;
import com.cleanroommc.multiblocked.util.Size;
import com.cleanroommc.multiblocked.util.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.cleanroommc.multiblocked.util.Vector3.X;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23
 * @Description: Abstract class, and extend a lot of features compared with the original one.
 */
@SuppressWarnings("ALL")
@SideOnly(Side.CLIENT)
public abstract class WorldSceneRenderer {
    protected static final FloatBuffer MODELVIEW_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer PROJECTION_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    protected static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer OBJECT_POS_BUFFER = ByteBuffer.allocateDirect(3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public final World world;
    public final Map<Collection<BlockPos>, ISceneRenderHook> renderedBlocksMap;
    protected VertexBuffer[] vertexBuffers;
    protected boolean useCache;
    protected boolean needCompile;
    protected ParticleManager particleManager;
    protected EntityCamera viewEntity;

    private Consumer<WorldSceneRenderer> beforeRender;
    private Consumer<WorldSceneRenderer> afterRender;
    private Consumer<RayTraceResult> onLookingAt;
    protected int clearColor;
    private RayTraceResult lastTraceResult;
    private Vector3f eyePos = new Vector3f(0, 0, 10f);
    private Vector3f lookAt = new Vector3f(0, 0, 0);
    private Vector3f worldUp = new Vector3f(0, 1, 0);

    public WorldSceneRenderer(World world) {
        this.world = world;
        renderedBlocksMap = new LinkedHashMap<>();
    }

    public WorldSceneRenderer setParticleManager(ParticleManager particleManager) {
        if (particleManager == null) {
            this.particleManager = null;
            this.viewEntity = null;
            return this;
        }
        this.particleManager = particleManager;
        this.viewEntity = new EntityCamera(world);
        setCameraLookAt(eyePos, lookAt, worldUp);
        return this;
    }

    public WorldSceneRenderer useCacheBuffer(boolean useCache) {
        if (useCache || !OpenGlHelper.useVbo() || !Minecraft.getMinecraft().isCallingFromMinecraftThread()) return this;
        deleteCacheBuffer();
        if (useCache) {
            this.vertexBuffers = new VertexBuffer[BlockRenderLayer.values().length];
            for (int j = 0; j < BlockRenderLayer.values().length; ++j) {
                this.vertexBuffers[j] = new VertexBuffer(DefaultVertexFormats.BLOCK);
            }
            needCompile = true;
        }
        this.useCache = useCache;
        return this;
    }

    public WorldSceneRenderer deleteCacheBuffer() {
        if (useCache) {
            for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
                if (this.vertexBuffers[i] != null) {
                    this.vertexBuffers[i].deleteGlBuffers();
                }
            }
        }
        useCache = false;
        needCompile = true;
        return this;
    }

    public WorldSceneRenderer needCompileCache() {
        needCompile = true;
        return this;
    }

    public WorldSceneRenderer setBeforeWorldRender(Consumer<WorldSceneRenderer> callback) {
        this.beforeRender = callback;
        return this;
    }

    public WorldSceneRenderer setAfterWorldRender(Consumer<WorldSceneRenderer> callback) {
        this.afterRender = callback;
        return this;
    }

    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks, ISceneRenderHook renderHook) {
        if (blocks != null) {
            this.renderedBlocksMap.put(blocks, renderHook);
        }
        return this;
    }

    public WorldSceneRenderer setOnLookingAt(Consumer<RayTraceResult> onLookingAt) {
        this.onLookingAt = onLookingAt;
        return this;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setClearColor(int clearColor) {
        this.clearColor = clearColor;
    }

    public RayTraceResult getLastTraceResult() {
        return lastTraceResult;
    }

    public void render(float x, float y, float width, float height, int mouseX, int mouseY) {
        // setupCamera
        PositionedRect positionedRect = getPositionedRect((int)x, (int)y, (int)width, (int)height);
        PositionedRect mouse = getPositionedRect(mouseX, mouseY, 0, 0);
        mouseX = mouse.position.x;
        mouseY = mouse.position.y;
        setupCamera(positionedRect);
        // render TrackedDummyWorld
        drawWorld();
        // check lookingAt
        this.lastTraceResult = null;
        if (onLookingAt != null && mouseX > positionedRect.position.x && mouseX < positionedRect.position.x + positionedRect.size.width
                && mouseY > positionedRect.position.y && mouseY < positionedRect.position.y + positionedRect.size.height) {
            Vector3f hitPos = unProject(mouseX, mouseY);
            RayTraceResult result = rayTrace(hitPos);
            if (result != null) {
                this.lastTraceResult = null;
                this.lastTraceResult = result;
                onLookingAt.accept(result);
            }
        }
        // resetCamera
        resetCamera();
    }

    public Vector3f getEyePos() {
        return eyePos;
    }

    public Vector3f getLookAt() {
        return lookAt;
    }

    public Vector3f getWorldUp() {
        return worldUp;
    }

    public void setCameraLookAt(Vector3f eyePos, Vector3f lookAt, Vector3f worldUp) {
        this.eyePos = eyePos;
        this.lookAt = lookAt;
        this.worldUp = worldUp;
        if (viewEntity != null) {

            Vector3 xzProduct = new Vector3(lookAt.x - eyePos.x, 0, lookAt.z - eyePos.z);
            double angleYaw = Math.toDegrees(xzProduct.angle(Vector3.Z));
            if (xzProduct.angle(X) < Math.PI / 2) {
                angleYaw = -angleYaw;
            }
            double anglePitch = Math.toDegrees(new Vector3(lookAt).subtract(new Vector3(eyePos)).angle(Vector3.Y)) - 90;
            viewEntity.setLocationAndAngles(eyePos.x, eyePos.y, eyePos.z, (float) angleYaw, (float) anglePitch);
        }
    }

    public void setCameraLookAt(Vector3f lookAt, double radius, double rotationPitch, double rotationYaw) {
        Vector3 vecX = new Vector3(Math.cos(rotationPitch), 0, Math.sin(rotationPitch));
        Vector3 vecY = new Vector3(0, Math.tan(rotationYaw) * vecX.mag(),0);
        Vector3 pos = vecX.copy().add(vecY).normalize().multiply(radius);
        setCameraLookAt(pos.add(lookAt.x, lookAt.y, lookAt.z).vector3f(), lookAt, worldUp);
//        viewEntity.setPositionAndRotation(eyePos.x, eyePos.y, eyePos.z, (float) Math.toDegrees(rotationPitch), (float) Math.toDegrees(rotationYaw));

    }

    protected PositionedRect getPositionedRect(int x, int y, int width, int height) {
        return new PositionedRect(new Position(x, y), new Size(width, height));
    }

    protected void setupCamera(PositionedRect positionedRect) {
        int x = positionedRect.getPosition().x;
        int y = positionedRect.getPosition().y;
        int width = positionedRect.getSize().width;
        int height = positionedRect.getSize().height;

        GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        //setup viewport and clear GL buffers
        GlStateManager.viewport(x, y, width, height);

        clearView(x, y, width, height);

        //setup projection matrix to perspective
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        float aspectRatio = width / (height * 1.0f);
        GLU.gluPerspective(60.0f, aspectRatio, 0.1f, 10000.0f);

        //setup modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GLU.gluLookAt(eyePos.x, eyePos.y, eyePos.z, lookAt.x, lookAt.y, lookAt.z, worldUp.x, worldUp.y, worldUp.z);
    }

    protected void clearView(int x, int y, int width, int height) {
        int i = (clearColor & 0xFF0000) >> 16;
        int j = (clearColor & 0xFF00) >> 8;
        int k = (clearColor & 0xFF);
        GlStateManager.clearColor(i / 255.0f, j / 255.0f, k / 255.0f, (clearColor >> 24) / 255.0f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    protected void resetCamera() {
        //reset viewport
        Minecraft minecraft = Minecraft.getMinecraft();
        GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);

        //reset projection matrix
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();

        //reset modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableDepth();

        //reset attributes
        GlStateManager.popAttrib();
    }

    protected void drawWorld() {
        if (beforeRender != null) {
            beforeRender.accept(this);
        }

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        boolean checkDisabledModel = world == mc.world || (world instanceof TrackedDummyWorld && ((TrackedDummyWorld) world).proxyWorld == mc.world);
        float particleTicks = mc.getRenderPartialTicks();
        if (useCache) {
            renderCacheBuffer(mc, oldRenderLayer, particleTicks, checkDisabledModel);
        } else {
            BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
            try { // render block in each layer
                for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                    int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
                    ForgeHooksClient.setRenderLayer(layer);
                    if (pass == 1) {
                        renderTESR(0, particleTicks, checkDisabledModel);
                    }
                    renderedBlocksMap.forEach((renderedBlocks, hook)->{
                        if (hook != null) {
                            hook.apply(false, pass, layer);
                        } else {
                            setDefaultPassRenderState(pass);
                        }
                        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                        renderBlocks(checkDisabledModel, blockrendererdispatcher, layer, buffer, renderedBlocks);

                        Tessellator.getInstance().draw();
                        Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
                    });
                }
            } finally {
                ForgeHooksClient.setRenderLayer(oldRenderLayer);
            }
            renderTESR(1, particleTicks, checkDisabledModel);
        }
        GlStateManager.shadeModel(7425);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        if (afterRender != null) {
            afterRender.accept(this);
        }
    }

    private void renderCacheBuffer(Minecraft mc, BlockRenderLayer oldRenderLayer, float particleTicks, boolean checkDisabledModel) {
        if (needCompile) {
            BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
            try { // render block in each layer
                for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                    ForgeHooksClient.setRenderLayer(layer);
                    BufferBuilder buffer = new BufferBuilder(262144);
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                    renderedBlocksMap.forEach((renderedBlocks, hook) -> renderBlocks(checkDisabledModel, blockrendererdispatcher, layer, buffer, renderedBlocks));
                    buffer.reset();
                    vertexBuffers[layer.ordinal()].bufferData(buffer.getByteBuffer());
                }
            } finally {
                ForgeHooksClient.setRenderLayer(oldRenderLayer);
            }
            needCompile = false;
        } else {
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
                if (pass == 1) {
                    renderTESR(0, particleTicks, checkDisabledModel);
                }

                GlStateManager.glEnableClientState(32884);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.glEnableClientState(32888);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.glEnableClientState(32888);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.glEnableClientState(32886);

                VertexBuffer vbo = vertexBuffers[layer.ordinal()];
                setDefaultPassRenderState(pass);
                vbo.bindBuffer();
                this.setupArrayPointers();
                vbo.drawArrays(7);
                OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
                GlStateManager.resetColor();

                for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
                    VertexFormatElement.EnumUsage enumUsage = vertexformatelement.getUsage();
                    int k1 = vertexformatelement.getIndex();

                    switch (enumUsage) {
                        case POSITION:
                            GlStateManager.glDisableClientState(32884);
                            break;
                        case UV:
                            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
                            GlStateManager.glDisableClientState(32888);
                            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                            break;
                        case COLOR:
                            GlStateManager.glDisableClientState(32886);
                            GlStateManager.resetColor();
                    }
                }

            }
            renderTESR(1, particleTicks, checkDisabledModel);
        }
    }

    private void renderBlocks(boolean checkDisabledModel, BlockRendererDispatcher blockrendererdispatcher, BlockRenderLayer layer, BufferBuilder buffer, Collection<BlockPos> renderedBlocks) {
        for (BlockPos pos : renderedBlocks) {
            if (checkDisabledModel && MultiblockWorldSavedData.modelDisabled.contains(pos)) {
                continue;
            }
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.AIR) continue;
            state = state.getActualState(world, pos);
            if (block.canRenderInLayer(state, layer)) {
                blockrendererdispatcher.renderBlock(state, pos, world, buffer);
            }
        }
    }

    private void setupArrayPointers() {
        GlStateManager.glVertexPointer(3, 5126, 28, 0);
        GlStateManager.glColorPointer(4, 5121, 28, 12);
        GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private void renderTESR(final int pass, float particle, boolean checkDisabledModel) {
        if (particleManager != null) {
            particleManager.renderParticles(pass == 0, viewEntity, particle);
        }
        // render TESR
        RenderHelper.enableStandardItemLighting();
        ForgeHooksClient.setRenderPass(pass);
        renderedBlocksMap.forEach((renderedBlocks, hook)->{
            if (hook != null) {
                hook.apply(true, pass, null);
            } else {
                setDefaultPassRenderState(pass);
            }
            for (BlockPos pos : renderedBlocks) {
                if (checkDisabledModel && MultiblockWorldSavedData.modelDisabled.contains(pos)) {
                    continue;
                }
                TileEntity tile = world.getTileEntity(pos);
                if (tile != null) {
                    if (tile.shouldRenderInPass(pass)) {
                        TileEntityRendererDispatcher.instance.render(tile, pos.getX(), pos.getY(), pos.getZ(), particle);
                    }
                }
            }
        });
        ForgeHooksClient.setRenderPass(-1);
        RenderHelper.disableStandardItemLighting();

    }

    public static void setDefaultPassRenderState(int pass) {
        GlStateManager.color(1, 1, 1, 1);
        if (pass == 0) { // SOLID
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.shadeModel(7424);
        } else { // TRANSLUCENT
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.depthMask(false);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.shadeModel(7425);
        }
    }

    public RayTraceResult rayTrace(Vector3f hitPos) {
        Vec3d startPos = new Vec3d(this.eyePos.x, this.eyePos.y, this.eyePos.z);
        hitPos.scale(2); // Double view range to ensure pos can be seen.
        Vec3d endPos = new Vec3d((hitPos.x - startPos.x), (hitPos.y - startPos.y), (hitPos.z - startPos.z));
        return this.world.rayTraceBlocks(startPos, endPos);
    }

    public Vector3f project(BlockPos pos) {
        //read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        //rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        //call gluProject with retrieved parameters
        GLU.gluProject(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        //rewind buffers after read by gluProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        //rewind buffer after write by gluProject
        OBJECT_POS_BUFFER.rewind();

        //obtain position in Screen
        float winX = OBJECT_POS_BUFFER.get();
        float winY = OBJECT_POS_BUFFER.get();
        float winZ = OBJECT_POS_BUFFER.get();

        //rewind buffer after read
        OBJECT_POS_BUFFER.rewind();

        return new Vector3f(winX, winY, winZ);
    }

    public Vector3f unProject(int mouseX, int mouseY) {
        //read depth of pixel under mouse
        GL11.glReadPixels(mouseX, mouseY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER);

        //rewind buffer after write by glReadPixels
        PIXEL_DEPTH_BUFFER.rewind();

        //retrieve depth from buffer (0.0-1.0f)
        float pixelDepth = PIXEL_DEPTH_BUFFER.get();

        //rewind buffer after read
        PIXEL_DEPTH_BUFFER.rewind();

        //read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        //rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        //call gluUnProject with retrieved parameters
        GLU.gluUnProject(mouseX, mouseY, pixelDepth, MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        //rewind buffers after read by gluUnProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        //rewind buffer after write by gluUnProject
        OBJECT_POS_BUFFER.rewind();

        //obtain absolute position in world
        float posX = OBJECT_POS_BUFFER.get();
        float posY = OBJECT_POS_BUFFER.get();
        float posZ = OBJECT_POS_BUFFER.get();

        //rewind buffer after read
        OBJECT_POS_BUFFER.rewind();

        return new Vector3f(posX, posY, posZ);
    }

    /***
     * For better performance, You'd better handle the event {@link #setOnLookingAt(Consumer)} or {@link #getLastTraceResult()}
     * @param mouseX xPos in Texture
     * @param mouseY yPos in Texture
     * @return RayTraceResult Hit
     */
    protected RayTraceResult screenPos2BlockPosFace(int mouseX, int mouseY, int x, int y, int width, int height) {
        // render a frame
        GlStateManager.enableDepth();
        setupCamera(getPositionedRect(x, y, width, height));

        drawWorld();

        Vector3f hitPos = unProject(mouseX, mouseY);
        RayTraceResult result = rayTrace(hitPos);

        resetCamera();

        return result;
    }

    /***
     * For better performance, You'd better do project in {@link #setAfterWorldRender(Consumer)}
     * @param pos BlockPos
     * @param depth should pass Depth Test
     * @return x, y, z
     */
    protected Vector3f blockPos2ScreenPos(BlockPos pos, boolean depth, int x, int y, int width, int height){
        // render a frame
        GlStateManager.enableDepth();
        setupCamera(getPositionedRect(x, y, width, height));

        drawWorld();
        Vector3f winPos = project(pos);

        resetCamera();

        return winPos;
    }

}
