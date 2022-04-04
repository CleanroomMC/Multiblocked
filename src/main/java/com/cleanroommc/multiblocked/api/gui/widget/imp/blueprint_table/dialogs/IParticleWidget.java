package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Vector3f;
import java.util.Arrays;

public class IParticleWidget extends DialogWidget {
    public SceneWidget sceneWidget;

    public IParticleWidget(WidgetGroup parent) {
        super(parent, true);
        this.addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ColorRectTexture(0xaf000000)));
        TrackedDummyWorld world = new TrackedDummyWorld();
        BlockPos[] poses = new BlockPos[]{
                BlockPos.ORIGIN,
                BlockPos.ORIGIN.offset(EnumFacing.NORTH),
                BlockPos.ORIGIN.offset(EnumFacing.EAST),
                BlockPos.ORIGIN.offset(EnumFacing.WEST),
                BlockPos.ORIGIN.offset(EnumFacing.SOUTH)};
        for (BlockPos pos : poses) {
            world.addBlock(pos, new BlockInfo(Blocks.STONE));
        }
        this.addWidget(new ImageWidget(35, 59, 138, 138, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        this.addWidget(sceneWidget = new SceneWidget(35, 59,  138, 138, world)
                .setRenderedCore(Arrays.asList(poses.clone()), null)
                .useParticle()
                .setCenter(new Vector3f(0.5f, 2.5f, 0.5f))
                .setRenderSelect(false)
                .setRenderFacing(false));
        this.addWidget(new ButtonWidget(285, 55, 40, 20, this::onUpdate)
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("update", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("update"));
    }

    private void onUpdate(ClickData clickData) {
        sceneWidget.getParticleManager().clearAllEffects(true);
        ResourceLocation texture = new ResourceLocation(Multiblocked.MODID, "textures/fx/particles.png");
        CommonParticle particle = new CommonParticle(sceneWidget.getDummyWorld(), 0, 3.5f, 0);
        particle.isBackLayer = true;
        particle.setScale(16);
        particle.setGravity(0);
        particle.setLife(20000);
        particle.setTexture(texture);
        sceneWidget.getParticleManager().addEffect(particle);
    }

}
