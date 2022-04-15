package com.cleanroommc.multiblocked.api.pattern.predicates;

import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.BlockSelectorWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PredicateStates extends SimplePredicate {
    public IBlockState[] states = new IBlockState[0];

    public PredicateStates() {
        super("states");
    }
    
    public PredicateStates(IBlockState... states) {
        this();
        this.states = states;
        buildPredicate();
    }

    @Override
    public SimplePredicate buildPredicate() {
        states = Arrays.stream(states).filter(Objects::nonNull).toArray(IBlockState[]::new);
        if (states.length == 0) states = new IBlockState[]{Blocks.BARRIER.getDefaultState()};
        predicate = state -> ArrayUtils.contains(states, state.getBlockState());
        candidates = () -> Arrays.stream(states).map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
        return this;
    }

    @Override
    public List<WidgetGroup> getConfigWidget(List<WidgetGroup> groups) {
        super.getConfigWidget(groups);
        WidgetGroup group = new WidgetGroup(0, 0, 182, 100);
        groups.add(group);
        DraggableScrollableWidgetGroup container = new DraggableScrollableWidgetGroup(0, 25, 182, 80).setBackground(new ColorRectTexture(0xffaaaaaa));
        group.addWidget(container);
        List<IBlockState> blockList = new ArrayList<>(Arrays.asList(states));
        for (IBlockState blockState : blockList) {
            addBlockSelectorWidget(blockList, container, blockState);
        }
        group.addWidget(new LabelWidget(0, 6, ()->"BlockState Settings").setTextColor(-1).setDrop(true));
        group.addWidget(new ButtonWidget(162, 0, 20, 20, cd -> {
            blockList.add(null);
            addBlockSelectorWidget(blockList, container, null);
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/add.png")).setHoverBorderTexture(1, -1).setHoverTooltip("add a new blockstate"));
        return groups;
    }

    private void addBlockSelectorWidget(List<IBlockState> blockList, DraggableScrollableWidgetGroup container, IBlockState blockState) {
        BlockSelectorWidget bsw = new BlockSelectorWidget(0, container.widgets.size() * 21 + 1, true);
        container.addWidget(bsw);
        bsw.addWidget(new ButtonWidget(163, 1, 18, 18, cd -> {
            int index = (bsw.getSelfPosition().y - 1) / 21;
            blockList.remove(index);
            updateStates(blockList);
            for (int i = index + 1; i < container.widgets.size(); i++) {
                container.widgets.get(i).addSelfPosition(0, -21);
            }
            container.waitToRemoved(bsw);
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/remove.png")).setHoverBorderTexture(1, -1).setHoverTooltip("remove"));
        if (blockState != null) {
            bsw.setBlock(blockState);
        }
        bsw.setOnBlockStateUpdate(state->{
            int index = (bsw.getSelfPosition().y - 1) / 21;
            blockList.set(index, state);
            updateStates(blockList);
        });
    }

    private void updateStates(List<IBlockState> blockList) {
        states = blockList.stream().filter(Objects::nonNull).toArray(IBlockState[]::new);
        buildPredicate();
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.add("states", Multiblocked.GSON.toJsonTree(states));
        return super.toJson(jsonObject);
    }

    @Override
    public void fromJson(Gson gson, JsonObject jsonObject) {
        states = gson.fromJson(jsonObject.get("states"), IBlockState[].class);
        super.fromJson(gson, jsonObject);
    }
}
