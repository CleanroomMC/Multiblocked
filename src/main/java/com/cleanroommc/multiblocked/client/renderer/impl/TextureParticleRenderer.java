package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.particle.AbstractParticle;
import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import com.cleanroommc.multiblocked.client.renderer.ICustomRenderer;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.function.Supplier;

public class TextureParticleRenderer extends ParticleRenderer implements ICustomRenderer {
    public final static TextureParticleRenderer INSTANCE = new TextureParticleRenderer(null);

    public ResourceLocation texture;
    public boolean isShader = false;
    public float scale = 1;
    public int light = -1;

    public TextureParticleRenderer(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected AbstractParticle createParticle(ComponentTileEntity<?> te, double x, double y, double z) {
        CommonParticle particle = isShader ? new ShaderTextureParticle(te.getWorld(), x, y, z) : new CommonParticle(te.getWorld(), x, y, z);
        particle.setScale(scale);
        if (light >= 0) {
            particle.setLightingMap(light, light);
        }
        particle.setTexture(texture);
        return particle;
    }

    @Override
    public String getType() {
        return "tparticle";
    }

    @Override
    public IRenderer fromJson(Gson gson, JsonObject jsonObject) {
        TextureParticleRenderer renderer = new TextureParticleRenderer(gson.fromJson(jsonObject.get("texture"), ResourceLocation.class));
        renderer.isBackLayer = jsonObject.get("isBackLayer").getAsBoolean();
        renderer.isAddBlend = jsonObject.get("isAddBlend").getAsBoolean();
        renderer.isShader = jsonObject.get("isShader").getAsBoolean();
        renderer.scale = jsonObject.get("scale").getAsFloat();
        renderer.light = jsonObject.get("light").getAsInt();
        renderer.renderRange = jsonObject.get("renderRange").getAsInt();
        return renderer;
    }

    @Override
    public JsonObject toJson(Gson gson, JsonObject jsonObject) {
        jsonObject = super.toJson(gson, jsonObject);
        jsonObject.add("texture", gson.toJsonTree(texture, ResourceLocation.class));
        jsonObject.addProperty("isShader", isShader);
        jsonObject.addProperty("scale", scale);
        jsonObject.addProperty("light", light);
        return jsonObject;
    }

    @Override
    public Supplier<IRenderer> createConfigurator(WidgetGroup parent, DraggableScrollableWidgetGroup group, IRenderer current) {
        TextFieldWidget tfw = new TextFieldWidget(1,1,150,20,true, null, null);
        group.addWidget(tfw);
        TextureParticleRenderer renderer = new TextureParticleRenderer(new ResourceLocation(""));
        if (current instanceof  TextureParticleRenderer) {
            renderer.texture = ((TextureParticleRenderer) current).texture;
            renderer.light = ((TextureParticleRenderer) current).light;
            renderer.scale = ((TextureParticleRenderer) current).scale;
            renderer.isShader =((TextureParticleRenderer) current).isShader;
            renderer.isAddBlend = ((TextureParticleRenderer) current).isAddBlend;
            renderer.isBackLayer = ((TextureParticleRenderer) current).isBackLayer;
            renderer.renderRange = ((TextureParticleRenderer) current).renderRange;
        }
        File png = new File(Multiblocked.location, "assets/multiblocked/textures");
        File shader = new File(Multiblocked.location, "assets/multiblocked/shaders");
        group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(parent, "select a texture/shader file", renderer.isShader ? shader : png, true,
                renderer.isShader ? DialogWidget.suffixFilter(".frag") : DialogWidget.suffixFilter(".png"), r -> {
                    if (r != null && r.isFile()) {
                        if (renderer.isShader) {
                            tfw.setCurrentString("multiblocked:" + r.getPath().replace(shader.getPath(), "").substring(1).replace(".frag", "").replace('\\', '/'));
                        } else {
                            tfw.setCurrentString("multiblocked:" + r.getPath().replace(png.getPath(), "textures").replace('\\', '/'));
                        }
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("multiblocked.gui.tips.file_selector"));
        tfw.setCurrentString(renderer.texture.toString());
        group.addWidget(createBoolSwitch(1, 25, "isShader", "multiblocked.gui.predicate.particle.0", renderer.isShader, r->{
            if (renderer.isShader != r) {
                renderer.isShader = r;
                tfw.setCurrentString("");
            }
        }));
        group.addWidget(createBoolSwitch(1, 40, "isAddBlend", "multiblocked.gui.predicate.particle.1", renderer.isAddBlend, r -> renderer.isAddBlend = r));
        group.addWidget(createBoolSwitch(1, 55, "isBackLayer", "multiblocked.gui.predicate.particle.2", renderer.isBackLayer, r -> renderer.isBackLayer = r));
        group.addWidget(new TextFieldWidget(1,75,70,10,true, null, num->renderer.scale = Float.parseFloat(num))
                .setNumbersOnly(0f, 100f).setCurrentString(renderer.scale+"").setHoverTooltip("multiblocked.gui.predicate.particle.3"));
        group.addWidget(new LabelWidget(75, 75, "multiblocked.gui.label.scale"));
        group.addWidget(new TextFieldWidget(1,90,70,10,true, null, num->renderer.light = Integer.parseInt(num))
                .setNumbersOnly(-1, 15).setCurrentString(renderer.light+"").setHoverTooltip("multiblocked.gui.predicate.particle.4"));
        group.addWidget(new LabelWidget(75, 90, "multiblocked.gui.label.light"));
        group.addWidget(new TextFieldWidget(1,105,70,10,true, null, num->renderer.renderRange = Integer.parseInt(num))
                .setNumbersOnly(-1, 1000).setCurrentString(renderer.renderRange+"").setHoverTooltip("multiblocked.gui.predicate.particle.5"));
        group.addWidget(new LabelWidget(75, 105, "multiblocked.gui.label.render_range"));
        return () -> {
            if (tfw.getCurrentString().isEmpty()) {
                return null;
            } else {
                TextureParticleRenderer newRenderer = new TextureParticleRenderer(new ResourceLocation(tfw.getCurrentString()));
                newRenderer.light = renderer.light;
                newRenderer.scale = renderer.scale;
                newRenderer.isShader = renderer.isShader;
                newRenderer.isAddBlend = renderer.isAddBlend;
                newRenderer.isBackLayer = renderer.isBackLayer;
                newRenderer.renderRange = renderer.renderRange;
                return newRenderer;
            }
        };
    }
}
