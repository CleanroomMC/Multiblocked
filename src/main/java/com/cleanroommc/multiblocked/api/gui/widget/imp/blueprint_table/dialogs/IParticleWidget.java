package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.api.crafttweaker.CTHelper;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextBoxWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.client.particle.ParticleManager;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.util.FileUtility;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

public class IParticleWidget extends DialogWidget {
    private final static String HELP =
            "import mods.multiblocked.MBDParticle;\n" +
                    "\n" +
                    "§2import§r crafttweaker.world.IWorld;\n" +
                    "§2import§r crafttweaker.util.IRandom;\n" +
                    "\n" +
                    "§3val§r world = MBDParticle.getWorld();\n" +
                    "§3val§r rand = world.random;\n" +
                    "\n" +
                    "§3val§r particle = MBDParticle.texture(world, 0.5, 2, 0.5, true);\n" +
                    "particle.setTexture(\"multiblocked:start\");\n" +
                    "particle.setBackLayer(true);\n" +
                    "particle.setScale(16);\n" +
                    "particle.setLightingMap(15, 15);\n" +
                    "particle.setImmortal();\n" +
                    "particle.create();\n";

    @SideOnly(Side.CLIENT)
    private static SceneWidget GLOBAL;
    private final SceneWidget sceneWidget;
    private final TextFieldWidget textFieldWidget;
    private final TextBoxWidget textBox;
    private final DraggableScrollableWidgetGroup tfGroup;

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
                .setCenter(new Vector3f(0.5f, 2.5f, 0.5f))
                .setRenderSelect(false)
                .setRenderFacing(false));
        this.addWidget(new ButtonWidget(305, 55, 40, 20, this::execute)
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("update", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.tips.execute"));

        String init = "import mods.multiblocked.MBDRegistry;";
        try {
            InputStream inputstream = IParticleWidget.class.getResourceAsStream("/assets/multiblocked/demo/particle.zs");
            init = FileUtility.readInputStream(inputstream);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        textFieldWidget = new TextFieldWidget(181, 55, 120, 20, true, null, null)
                .setAllowEnter(true)
                .setCurrentString(init);
        tfGroup = new DraggableScrollableWidgetGroup(181, 80, 170, 120)
                .setBackground(new ColorRectTexture(0x3faaaaaa))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1));
        textBox = new TextBoxWidget(0, 0, 165, Collections.singletonList(init)).setFontColor(-1).setShadow(true);
        tfGroup.addWidget(textBox);
        this.addWidget(tfGroup);
        this.addWidget(textFieldWidget);

        this.addWidget(new ButtonWidget(305, 15, 40, 20, cd -> new DialogWidget(this, true)
                .addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ColorRectTexture(0xdf000000)))
                .addWidget(new TextBoxWidget(2, 2, getSize().width - 4, Collections.singletonList(HELP)).setFontColor(-1).setShadow(true))
                .addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ColorBorderTexture(1, -1))))
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("help", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.tips.help"));
    }

    public static ParticleManager getParticleManager() {
        return GLOBAL == null ? null : GLOBAL.getParticleManager();
    }

    public static World getWorld() {
        return GLOBAL == null ? null : GLOBAL.getDummyWorld();
    }

    private void execute(ClickData clickData) {
        sceneWidget.getParticleManager().clearAllEffects(true);
        String script = textFieldWidget.getCurrentString();

        GLOBAL = sceneWidget;
        if (!CTHelper.executeDynamicScript(script)) {
            script = CTHelper.getError() == null ? "error" : CTHelper.getError();
        }
        GLOBAL = null;

        textBox.setContent(Collections.singletonList(script));
        tfGroup.computeMax();
    }

}
