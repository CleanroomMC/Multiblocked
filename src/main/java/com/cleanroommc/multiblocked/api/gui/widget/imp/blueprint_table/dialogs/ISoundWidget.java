package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.api.sound.ComponentSound;
import com.cleanroommc.multiblocked.api.sound.SoundState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public class ISoundWidget extends DialogWidget implements SearchComponentWidget.IWidgetSearch<SoundEvent> {
    public Consumer<SoundState> onSave;
    private SoundState sound;
    @SideOnly(Side.CLIENT)
    private ComponentSound componentSound;
    private final DraggableScrollableWidgetGroup soundList;
    private final WidgetGroup settings;

    public ISoundWidget(WidgetGroup parent, SoundState soundState, Consumer<SoundState> onSave) {
        super(parent, true);
        this.onSave = onSave;
        this.sound = soundState.copy();
        this.addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ColorRectTexture(0xaf000000)));

        this.addWidget(new ImageWidget(35, 45, 150 - 20, 170, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        this.addWidget(soundList = new DraggableScrollableWidgetGroup(35, 49, 150 - 20, 170 - 8)
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1)));

        this.addWidget(new ButtonWidget(180, 55, 40, 20, this::play)
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("Play", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.tips.play"));
        this.addWidget(new ButtonWidget(230, 55, 40, 20, cd -> {
            if (componentSound != null) {
                componentSound.stopSound();
            }
            onSave.accept(this.sound);
            super.close();
        })
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.tips.save_1", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.tips.save"));

        this.addWidget(settings = new WidgetGroup(0, 0, getSize().width, getSize().height));
        this.addWidget(new SearchComponentWidget<>(35, 22, 150 - 20, 20, this));
        updateStatusList();
    }

    protected void updateStatusList() {
        soundList.clearAllWidgets();
        SelectableWidgetGroup selected = new SelectableWidgetGroup(5, 1 + soundList.widgets.size() * 22, soundList.getSize().width - 10, 20);
        selected.setSelectedTexture(-2, 0xff00aa00)
                .setOnSelected(W -> {
                    sound = SoundState.EMPTY;
                    updateSettings();
                })
                .addWidget(new ImageWidget(0, 0, 120, 20, new ColorRectTexture(0x4faaaaaa)))
                .addWidget(new ImageWidget(2, 0, soundList.getSize().width - 14, 20, new TextTexture("NULL").setWidth(soundList.getSize().width - 14).setType(TextTexture.TextType.ROLL)));
        soundList.addWidget(selected);
        for (SoundEvent soundEvent : ForgeRegistries.SOUND_EVENTS.getValues()) {
            SelectableWidgetGroup group = new SelectableWidgetGroup(5, 1 + soundList.widgets.size() * 22, soundList.getSize().width - 10, 20);
            group.setSelectedTexture(-2, 0xff00aa00)
                    .setOnSelected(W -> {
                        sound = sound.copy(soundEvent.getRegistryName());
                        updateSettings();
                    })
                    .addWidget(new ImageWidget(0, 0, 120, 20, new ColorRectTexture(0x4faaaaaa)))
                    .addWidget(new ImageWidget(2, 0, soundList.getSize().width - 14, 20, new TextTexture(getName(soundEvent)).setWidth(soundList.getSize().width - 14).setType(TextTexture.TextType.ROLL)));
            soundList.addWidget(group);
            if (soundEvent.getRegistryName().equals(sound.sound)) {
                selected = group;
            }
        }
        soundList.setSelected(selected);
    }

    private String getName(SoundEvent soundEvent) {
        if (isRemote()) {
            SoundEventAccessor eventAccessor = Minecraft.getMinecraft().getSoundHandler().getAccessor(soundEvent.getSoundName());
            if (eventAccessor != null) {
                ITextComponent textComponent = eventAccessor.getSubtitle();
                if (textComponent != null) {
                    return textComponent.getFormattedText();
                }
            }
        }
        return soundEvent.getRegistryName().toString();
    }

    private void updateSettings() {
        settings.clearAllWidgets();
        if (sound != SoundState.EMPTY) {
            settings.addWidget(GuiUtils.createBoolSwitch(181, 80, "loop", "multiblocked.gui.widget.sound.loop", sound.loop, r -> sound.loop = r));
            settings.addWidget(GuiUtils.createIntField(181, 95, "delay", "multiblocked.gui.widget.sound.delay", sound.delay, 0, Integer.MAX_VALUE, r -> sound.delay = r));
            settings.addWidget(GuiUtils.createFloatField(181, 110, "volume", "multiblocked.gui.widget.sound.volume", sound.volume, 0f, 1f, r -> sound.volume = r));
            settings.addWidget(GuiUtils.createFloatField(181, 125, "pitch", "multiblocked.gui.widget.sound.pitch", sound.pitch, 0, Float.MAX_VALUE, r -> sound.pitch = r));
        }
    }


    @Override
    public void close() {
        if (componentSound != null) {
            componentSound.stopSound();
        }
        super.close();
    }

    private void play(ClickData clickData) {
        if (componentSound != null) {
            componentSound.stopSound();
        }
        componentSound = sound.playGUISound();
    }

    @Override
    public String resultDisplay(SoundEvent sound) {
        return getName(sound);
    }

    @Override
    public void selectResult(SoundEvent sound) {
        int index = ForgeRegistries.SOUND_EVENTS.getValues().indexOf(sound);
        soundList.setScrollYOffset(index * 22 + 1);
    }

    @Override
    public void search(String word, Consumer<SoundEvent> find) {
        for (SoundEvent sound : ForgeRegistries.SOUND_EVENTS.getValues()) {
            if (sound.getRegistryName().toString().toLowerCase().contains(word.toLowerCase()) || getName(sound).toLowerCase().contains(word.toLowerCase())) {
                find.accept(sound);
            }
        }
    }
}
