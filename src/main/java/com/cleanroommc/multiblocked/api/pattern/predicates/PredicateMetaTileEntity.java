package com.cleanroommc.multiblocked.api.pattern.predicates;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.PhantomSlotWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class PredicateMetaTileEntity extends SimplePredicate {
    public String metaTileEntityId = "gregtech:item_bus.export.lv";

    public PredicateMetaTileEntity() {
        super("mte");
    }

    public PredicateMetaTileEntity(String metaTileEntityId) {
        this();
        this.metaTileEntityId = metaTileEntityId;
        buildPredicate();
    }

    @Override
    public SimplePredicate buildPredicate() {
        ResourceLocation location = new ResourceLocation(metaTileEntityId);
        predicate = state -> {
          if (state.getTileEntity() instanceof MetaTileEntityHolder) {
              MetaTileEntityHolder holder = (MetaTileEntityHolder) state.getTileEntity();
              return holder.getMetaTileEntity() != null && holder.getMetaTileEntity().metaTileEntityId.equals(location);
          }
          return false;
        };
        candidates = () -> {
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(location);
            if (sampleMetaTileEntity != null) {
                holder.setMetaTileEntity(sampleMetaTileEntity.createMetaTileEntity(holder));
                return new BlockInfo[]{new BlockInfo(GregTechAPI.MACHINE.getDefaultState(), holder, sampleMetaTileEntity.getStackForm())};
            }
            return new BlockInfo[]{new BlockInfo(Blocks.BARRIER)};
        };
        return this;
    }

    @Override
    public List<WidgetGroup> getConfigWidget(List<WidgetGroup> groups) {
        super.getConfigWidget(groups);
        WidgetGroup group = new WidgetGroup(0, 0, 145, 20);
        groups.add(group);
        IItemHandlerModifiable handler = new ItemStackHandler(1);
        TextFieldWidget textFieldWidget = new TextFieldWidget(0, 10, 120, 20, true, null, s -> {
            if (s != null && !s.isEmpty()) {
                metaTileEntityId = s;
                MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(new ResourceLocation(metaTileEntityId));
                if (sampleMetaTileEntity != null) {
                    handler.setStackInSlot(0, sampleMetaTileEntity.getStackForm());
                }
                buildPredicate();
            }
        }).setCurrentString(metaTileEntityId);
        group.addWidget(new PhantomSlotWidget(handler, 0, 125, 11)
                .setClearSlotOnRightClick(true)
                .setChangeListener(() -> {
                    ItemStack stack = handler.getStackInSlot(0);
                    if (stack.isEmpty() || !(stack.getItem() instanceof MachineItemBlock)) {
                        metaTileEntityId = "";
                        buildPredicate();
                        handler.setStackInSlot(0, ItemStack.EMPTY);
                    } else {
                        MetaTileEntity mte = GregTechAPI.MTE_REGISTRY.getObjectById(stack.getItemDamage());
                        metaTileEntityId = mte == null ? "" : mte.metaTileEntityId.toString();
                    }
                    textFieldWidget.setCurrentString(metaTileEntityId);
                }).setBackgroundTexture(new ColorBorderTexture(1, -1)));
        group.addWidget(new LabelWidget(0, 0, "multiblocked.gui.label.metaTileEntity_registry_name"));
        group.addWidget(textFieldWidget);
        MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(new ResourceLocation(metaTileEntityId));
        if (sampleMetaTileEntity != null) {
            handler.setStackInSlot(0, sampleMetaTileEntity.getStackForm());
        }
        return groups;
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("metaTileEntityId", metaTileEntityId);
        return super.toJson(jsonObject);
    }

    @Override
    public void fromJson(Gson gson, JsonObject jsonObject) {
        metaTileEntityId = JsonUtils.getString(jsonObject, "metaTileEntityId", "");
        super.fromJson(gson, jsonObject);
    }
}
