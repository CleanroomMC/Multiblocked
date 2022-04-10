package com.cleanroommc.multiblocked.api.pattern.predicates;

import com.cleanroommc.multiblocked.Multiblocked;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;

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
        predicate = state -> state.getBlockState().getBlock() == capability.getAnyBlock(io) || checkCapability(io, capability, state);
        candidates = () -> new BlockInfo[]{new BlockInfo(capability.getAnyBlock(io))};
        return this;
    }

    private static boolean checkCapability(IO io, MultiblockCapability<?> capability, MultiblockState state) {
        TileEntity tileEntity = state.getTileEntity();
        if (tileEntity != null && capability.isBlockHasCapability(io, tileEntity)) {
            Map<Long, EnumMap<IO, Set<MultiblockCapability<?>>>> capabilities = state.getMatchContext().getOrCreate("capabilities", Long2ObjectOpenHashMap::new);
            capabilities.computeIfAbsent(state.getPos().toLong(), l-> new EnumMap<>(IO.class))
                    .computeIfAbsent(io, x->new HashSet<>())
                    .add(capability);
            return true;
        }
        if (Multiblocked.isClient()) {
            state.setError(new PatternStringError(
                    I18n.format("multiblocked.pattern.error.capability", I18n.format(capability.getUnlocalizedName()), io.name())));
        } else {
            state.setError(new PatternStringError("find no io_capability: " + io.name() + "_" + capability.name));
        }
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
        jsonObject.add("capability", new JsonPrimitive(capability));
        return super.toJson(jsonObject);
    }
}
