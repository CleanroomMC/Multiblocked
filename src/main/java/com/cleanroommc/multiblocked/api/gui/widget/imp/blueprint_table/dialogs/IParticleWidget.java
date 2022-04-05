package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.*;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.IParticle;
import com.cleanroommc.multiblocked.client.particle.LaserBeamParticle;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.util.Vector3;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Vector3f;
import java.util.Arrays;
import java.util.Random;

public class IParticleWidget extends DialogWidget {
    public SceneWidget sceneWidget;

    public IParticleWidget(WidgetGroup parent) {
        super(parent, true);
        this.addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ColorRectTexture(0xaf000000)));
        TrackedDummyWorld world = new TrackedDummyWorld();
        BlockPos[] poses = new BlockPos[]{
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
//        ResourceLocation texture = new ResourceLocation(Multiblocked.MODID, "textures/fx/fx.png");
//        Random rand = Multiblocked.RNG;
//        for (int i = 0; i < 20; i++) {
//            CommonParticle particle = new CommonParticle(sceneWidget.getDummyWorld(), 0.5, 2, 0.5, (rand.nextFloat() * 2 - 1) * 0, 1, (rand.nextFloat() * 2 - 1)*0);
//            particle.isBackLayer = true;
//            particle.setScale(1);
//            particle.setTexturesCount(2);
//            particle.setGravity(0.3f);
//            particle.setTexturesIndex(rand.nextInt(2), rand.nextInt(2));
//            particle.setLightingMap(15, 15);
//            particle.setLife(60);
//            particle.setTexture(texture);
//            sceneWidget.getParticleManager().addEffect(particle);
//        }

//        sceneWidget.getParticleManager().clearAllEffects(true);
//        IParticle particle = new LaserBeamParticle(sceneWidget.getDummyWorld(), new Vector3(0.5, 1, -1), new Vector3(0.5, 3.5, 2.5))
//                .setEmit(0.1f)
//                .setHeadWidth(0.3f)
//                .setBody(new ResourceLocation(Multiblocked.MODID,"textures/fx/laser.png")) // create a beam particle and set its texture.
//                .setHead(new ResourceLocation(Multiblocked.MODID,"textures/fx/laser_start.png")) // create a beam particle and set its texture.
//                .setAddBlend(true);
//        sceneWidget.getParticleManager().addEffect(particle);

        sceneWidget.getParticleManager().clearAllEffects(true);
        ResourceLocation texture = new ResourceLocation(Multiblocked.MODID, "start");
        ShaderTextureParticle particle = new ShaderTextureParticle(sceneWidget.getDummyWorld(), 0.5, 2, 0.5);
        particle.setBackLayer(true);
        particle.setScale(16);
        particle.setLightingMap(15, 15);
        particle.setImmortal();
        particle.setTexture(texture);

        CommonParticle particle2 = new CommonParticle(sceneWidget.getDummyWorld(), 0.5, 2, 0.5);
        particle2.setBackLayer(true);
        particle2.setScale(16);
        particle2.setLightingMap(15, 15);
        particle2.setImmortal();
        particle2.setTexture(new ResourceLocation(Multiblocked.MODID, "textures/fx/fx.png"));

        sceneWidget.getParticleManager().addEffect(particle);
    }

}
