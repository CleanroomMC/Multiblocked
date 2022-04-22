package com.cleanroommc.multiblocked.api.pattern.predicates;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.api.pattern.MultiblockState;
import com.cleanroommc.multiblocked.api.pattern.error.PatternStringError;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JsonUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PredicateAnyCapability extends SimplePredicate {
    public String capability = "item";

    public PredicateAnyCapability() {
        super("capability");
    }
    
    public PredicateAnyCapability(MultiblockCapability<?> capability) {
        this();
        this.capability = capability.name;
        buildPredicate();
    }

    @Override
    public SimplePredicate buildPredicate() {
        MultiblockCapability<?> capability = MbdCapabilities.get(this.capability);
        if (capability == null) {
            predicate = state -> false;
            candidates = () -> new BlockInfo[] {new BlockInfo(Blocks.BARRIER)};
            return this;
        }
        predicate = state -> state.getBlockState().getBlock() == capability.getAnyBlock() || checkCapability(io, capability, state);
        candidates = () -> new BlockInfo[]{BlockInfo.fromBlockState(capability.getAnyBlock().getDefaultState())};
        toolTips = new ArrayList<>();
        toolTips.add(String.format("Any Capability: %s IO: %s", capability.name, io == null ? "NULL" : io.name()));
        return this;
    }

    private static boolean checkCapability(IO io, MultiblockCapability<?> capability, MultiblockState state) {
        if (io != null) {
            TileEntity tileEntity = state.getTileEntity();
            if (tileEntity != null && capability.isBlockHasCapability(io, tileEntity)) {
                Map<Long, EnumMap<IO, Set<MultiblockCapability<?>>>> capabilities = state.getMatchContext().getOrCreate("capabilities", Long2ObjectOpenHashMap::new);
                capabilities.computeIfAbsent(state.getPos().toLong(), l-> new EnumMap<>(IO.class))
                        .computeIfAbsent(io, x->new HashSet<>())
                        .add(capability);
                return true;
            }
        }
        state.setError(new PatternStringError(LocalizationUtils.format("multiblocked.pattern.error.capability", LocalizationUtils.format(capability.getUnlocalizedName()), io == null ? "NULL" : io.name())));
        return false;
    }

    @Override
    public List<WidgetGroup> getConfigWidget(List<WidgetGroup> groups) {
        super.getConfigWidget(groups);
        WidgetGroup group = new WidgetGroup(0, 0, 100, 20);
        groups.add(group);
        group.addWidget(new SelectorWidget(0, 0, 120, 20, new ArrayList<>(
                MbdCapabilities.CAPABILITY_REGISTRY.keySet()), -1)
                .setValue(capability)
                .setOnChanged(capability-> {
                    this.capability = capability;
                    buildPredicate();
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("Capability"));
        return groups;
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("capability", capability);
        return super.toJson(jsonObject);
    }

    @Override
    public void fromJson(Gson gson, JsonObject jsonObject) {
        capability = JsonUtils.getString(jsonObject, "capability", "");
        super.fromJson(gson, jsonObject);
    }
}
