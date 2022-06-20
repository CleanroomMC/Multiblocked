package com.cleanroommc.multiblocked.api.gui.factory;

import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUIContainer;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUIGuiContainer;
import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.network.s2c.SPacketUIOpen;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class UIFactory<T> {
    public final int uiFactoryId;
    public static final Int2ObjectMap<UIFactory<?>> FACTORIES = new Int2ObjectOpenHashMap<>();

    public UIFactory(){
        uiFactoryId = FACTORIES.size();
    }
    
    public static void register(UIFactory<?> factory) {
        FACTORIES.put(factory.uiFactoryId, factory);
    }

    public final boolean openUI(T holder, EntityPlayerMP player) {
        if (player instanceof FakePlayer) {
            return false;
        }
        ModularUI uiTemplate = createUITemplate(holder, player);
        if (uiTemplate == null) return false;
        uiTemplate.initWidgets();

        player.getNextWindowId();
        player.closeContainer();
        int currentWindowId = player.currentWindowId;

        PacketBuffer serializedHolder = new PacketBuffer(Unpooled.buffer());
        writeHolderToSyncData(serializedHolder, holder);
        ModularUIContainer container = new ModularUIContainer(uiTemplate);
        container.windowId = currentWindowId;
        //accumulate all initial updates of widgets in open packet
        uiTemplate.guiWidgets.values().forEach(w -> w.writeInitialData(serializedHolder));

        MultiblockedNetworking.sendToPlayer(new SPacketUIOpen(uiFactoryId, serializedHolder, currentWindowId), player);

        container.addListener(player);
        player.openContainer = container;

        //and fire forge event only in the end
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
        return true;
    }

    @SideOnly(Side.CLIENT)
    public final void initClientUI(PacketBuffer serializedHolder, int windowId) {
        T holder = readHolderFromSyncData(serializedHolder);
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP entityPlayer = minecraft.player;

        ModularUI uiTemplate = createUITemplate(holder, entityPlayer);
        if (uiTemplate == null) return;
        uiTemplate.initWidgets();
        ModularUIGuiContainer ModularUIGuiContainer = new ModularUIGuiContainer(uiTemplate);
        ModularUIGuiContainer.inventorySlots.windowId = windowId;
        uiTemplate.guiWidgets.values().forEach(w -> w.readInitialData(serializedHolder));
        minecraft.addScheduledTask(() -> {
            minecraft.displayGuiScreen(ModularUIGuiContainer);
            minecraft.player.openContainer.windowId = windowId;
        });
    }

    protected abstract ModularUI createUITemplate(T holder, EntityPlayer entityPlayer);

    @SideOnly(Side.CLIENT)
    protected abstract T readHolderFromSyncData(PacketBuffer syncData);

    protected abstract void writeHolderToSyncData(PacketBuffer syncData, T holder);

}
