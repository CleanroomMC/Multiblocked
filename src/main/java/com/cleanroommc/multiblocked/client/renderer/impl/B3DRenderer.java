package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.b3d.B3DLoader;

import java.io.File;
import java.util.function.Supplier;


public class B3DRenderer extends IModelRenderer {
    public final static B3DRenderer INSTANCE = new B3DRenderer();

    private B3DRenderer() {
        super();
    }

    public B3DRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    public String getType() {
        return "b3d";
    }

    @Override
    protected IModel getModel() {
        try {
            return B3DLoader.INSTANCE.loadModel(modelLocation);
        } catch (Exception e) {
            Multiblocked.LOGGER.error(e);
        }
        return null;
    }

    @Override
    public IRenderer fromJson(Gson gson, JsonObject jsonObject) {
        return new B3DRenderer(gson.fromJson(jsonObject.get("modelLocation"), ResourceLocation.class));
    }

    @Override
    public Supplier<IRenderer> createConfigurator(WidgetGroup parent, DraggableScrollableWidgetGroup group, IRenderer current) {
        TextFieldWidget tfw = new TextFieldWidget(1,1,150,20,true, null, null);
        group.addWidget(tfw);
        File path = new File(Multiblocked.location, "assets/multiblocked/b3d");
        group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(parent, "select a b3d model", path, true,
                DialogWidget.suffixFilter(".b3d"), r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:b3d/" + r.getPath().replace(path.getPath(), "").substring(1).replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("select file"));
        if (current instanceof B3DRenderer) {
            tfw.setCurrentString(((B3DRenderer) current).modelLocation.toString());
        }
        return () -> {
            if (tfw.getCurrentString().isEmpty()) {
                return null;
            } else {
                return new B3DRenderer(new ResourceLocation(tfw.getCurrentString()));
            }
        };
    }
}
