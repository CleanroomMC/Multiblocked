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
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;

import java.io.File;
import java.util.function.Supplier;


public class OBJRenderer extends IModelRenderer {
    public final static OBJRenderer INSTANCE = new OBJRenderer();

    private OBJRenderer() {
        super();
    }

    public OBJRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    public String getType() {
        return "obj";
    }

    @Override
    protected IModel getModel() {
        try {
            return OBJLoader.INSTANCE.loadModel(modelLocation);
        } catch (Exception e) {
            Multiblocked.LOGGER.error(e);
        }
        return ModelLoaderRegistry.getMissingModel();
    }

    @Override
    public IRenderer fromJson(Gson gson, JsonObject jsonObject) {
        return new OBJRenderer(gson.fromJson(jsonObject.get("modelLocation"), ResourceLocation.class));
    }

    @Override
    public Supplier<IRenderer> createConfigurator(WidgetGroup parent, DraggableScrollableWidgetGroup group, IRenderer current) {
        TextFieldWidget tfw = new TextFieldWidget(1,1,150,20,true, null, null);
        group.addWidget(tfw);
        if (current instanceof OBJRenderer) {
            tfw.setCurrentString(((OBJRenderer) current).modelLocation.toString());
        }
        File path = new File(Multiblocked.location, "assets/multiblocked/obj");
        group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(parent, "select an obj model", path, true,
                DialogWidget.suffixFilter(".obj"), r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:obj/" + r.getPath().replace(path.getPath(), "").substring(1).replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("multiblocked.gui.tips.file_selector"));

        return () -> {
            if (tfw.getCurrentString().isEmpty()) {
                return null;
            } else {
                return new OBJRenderer(new ResourceLocation(tfw.getCurrentString()));
            }
        };
    }
}
