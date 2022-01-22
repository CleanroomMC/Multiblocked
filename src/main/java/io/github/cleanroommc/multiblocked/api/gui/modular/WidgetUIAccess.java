package io.github.cleanroommc.multiblocked.api.gui.modular;

import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;

public interface WidgetUIAccess {

    void notifySizeChange();

    void notifyWidgetChange();

    boolean attemptMergeStack(ItemStack itemStack, boolean fromContainer, boolean simulate);

    void sendSlotUpdate(SlotWidget slot);

    void sendHeldItemUpdate();

    void writeClientAction(Widget widget, int id, Consumer<PacketBuffer> payloadWriter);

    void writeUpdateInfo(Widget widget, int id, Consumer<PacketBuffer> payloadWriter);

}
