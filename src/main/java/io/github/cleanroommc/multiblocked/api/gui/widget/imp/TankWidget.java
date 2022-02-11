package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.api.gui.ingredient.IIngredientSlot;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TankWidget extends Widget implements IIngredientSlot {

    public final IFluidTank fluidTank;

    protected boolean alwaysShowFull;
    protected boolean allowClickFilling;
    protected boolean allowClickEmptying;
    protected IGuiTexture background;
    protected IGuiTexture overlay;

    protected FluidStack lastFluidInTank;
    protected int lastTankCapacity;

    public TankWidget(IFluidTank fluidTank, int x, int y, int width, int height, boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidTank;
        this.allowClickFilling = allowClickContainerFilling;
        this.allowClickEmptying = allowClickContainerEmptying;
    }

    @Override
    public TankWidget setClientSideWidget() {
        super.setClientSideWidget();
        this.lastFluidInTank = fluidTank != null ? fluidTank.getFluid() != null ? fluidTank.getFluid().copy() : null : null;
        this.lastTankCapacity = fluidTank != null ? fluidTank.getCapacity() : 0;
        return this;
    }

    public TankWidget setAlwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public TankWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public TankWidget setOverlay(IGuiTexture overlay) {
        this.overlay = overlay;
        return this;
    }

    public TankWidget setContainerClicking(boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        this.allowClickFilling = allowClickContainerFilling;
        this.allowClickEmptying = allowClickContainerEmptying;
        return this;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidInTank;
        }
        return null;
    }

    public String getFormattedFluidAmount() {
        return String.format("%,d", lastFluidInTank == null ? 0 : lastFluidInTank.amount);
    }

    public String getFluidLocalizedName() {
        return lastFluidInTank == null ? "" : lastFluidInTank.getLocalizedName();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position pos = getPosition();
        Size size = getSize();
        if (background != null) {
            background.draw(mouseX, mouseY, pos.x, pos.y, size.width, size.height);
        }

        if (lastFluidInTank != null) {
            GlStateManager.disableBlend();
            FluidStack stackToDraw = lastFluidInTank;
            int drawAmount = alwaysShowFull ? lastFluidInTank.amount : lastTankCapacity;
            if (alwaysShowFull && lastFluidInTank.amount == 0) {
                stackToDraw = lastFluidInTank.copy();
                stackToDraw.amount = 1;
                drawAmount = 1;
            }
            DrawerHelper.drawFluidForGui(stackToDraw, drawAmount, pos.x + 1, pos.y + 1, size.width - 2, size.height - 2);

            if (alwaysShowFull) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1);
                String s = TextFormattingUtil.formatLongToCompactString(lastFluidInTank.amount, 4) + "L";
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
                GlStateManager.popMatrix();
            }

            GlStateManager.enableBlend();
            GlStateManager.color(1, 1, 1, 1);
        }
        if (overlay != null) {
            overlay.draw(mouseX, mouseY, pos.x, pos.y, size.width, size.height);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)) {
            List<String> tooltips = new ArrayList<>();
            if (lastFluidInTank != null) {
                Fluid fluid = lastFluidInTank.getFluid();
                tooltips.add(fluid.getLocalizedName(lastFluidInTank));
                tooltips.add(I18n.format("multiblocked.fluid.amount", lastFluidInTank.amount, lastTankCapacity));
                tooltips.add(I18n.format("multiblocked.fluid.temperature", fluid.getTemperature(lastFluidInTank)));
                tooltips.add(I18n.format(fluid.isGaseous(lastFluidInTank) ? "multiblocked.fluid.state_gas" : "multiblocked.fluid.state_liquid"));
            } else {
                tooltips.add(I18n.format("multiblocked.fluid.empty"));
                tooltips.add(I18n.format("multiblocked.fluid.amount", 0, lastTankCapacity));
            }

            if (gui != null) {
                DrawerHelper.drawHoveringText(ItemStack.EMPTY, tooltips, 300, mouseX, mouseY, gui.getScreenWidth(), gui.getScreenHeight());
            }

            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void updateScreen() {
        if (isClientSideWidget) {
            FluidStack fluidStack = fluidTank.getFluid();
            if (fluidTank.getCapacity() != lastTankCapacity) {
                this.lastTankCapacity = fluidTank.getCapacity();
            }
            if (fluidStack == null && lastFluidInTank != null) {
                this.lastFluidInTank = null;

            } else if (fluidStack != null) {
                if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                    this.lastFluidInTank = fluidStack.copy();
                } else if (fluidStack.amount != lastFluidInTank.amount) {
                    this.lastFluidInTank.amount = fluidStack.amount;
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        FluidStack fluidStack = fluidTank.getFluid();
        if (fluidTank.getCapacity() != lastTankCapacity) {
            this.lastTankCapacity = fluidTank.getCapacity();
            writeUpdateInfo(0, buffer -> buffer.writeVarInt(lastTankCapacity));
        }
        if (fluidStack == null && lastFluidInTank != null) {
            this.lastFluidInTank = null;
            writeUpdateInfo(1, buffer -> {
            });
        } else if (fluidStack != null) {
            if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
                NBTTagCompound fluidStackTag = fluidStack.writeToNBT(new NBTTagCompound());
                writeUpdateInfo(2, buffer -> buffer.writeCompoundTag(fluidStackTag));
            } else if (fluidStack.amount != lastFluidInTank.amount) {
                this.lastFluidInTank.amount = fluidStack.amount;
                writeUpdateInfo(3, buffer -> buffer.writeVarInt(lastFluidInTank.amount));
            }
        }
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        this.lastTankCapacity = fluidTank.getCapacity();
        buffer.writeVarInt(lastTankCapacity);
        FluidStack fluidStack = fluidTank.getFluid();
        if (fluidStack == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            this.lastFluidInTank = fluidStack.copy();
            buffer.writeCompoundTag(fluidStack.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void readInitialData(PacketBuffer buffer) {
        this.lastTankCapacity = buffer.readVarInt();
        if (buffer.readBoolean()) {
            readUpdateInfo(2, buffer);
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarInt();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            NBTTagCompound fluidStackTag;
            try {
                fluidStackTag = buffer.readCompoundTag();
            } catch (IOException ignored) {
                return;
            }
            this.lastFluidInTank = FluidStack.loadFluidStackFromNBT(fluidStackTag);
        } else if (id == 3 && lastFluidInTank != null) {
            this.lastFluidInTank.amount = buffer.readVarInt();
        } else if (id == 4) {
            ItemStack currentStack = gui.entityPlayer.inventory.getItemStack();
            int newStackSize = buffer.readVarInt();
            currentStack.setCount(newStackSize);
            gui.entityPlayer.inventory.setItemStack(currentStack);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            boolean isShiftKeyDown = buffer.readBoolean();
            int clickResult = tryClickContainer(isShiftKeyDown);
            if (clickResult >= 0) {
                writeUpdateInfo(4, buf -> buf.writeVarInt(clickResult));
            }
        }
    }

    private int tryClickContainer(boolean isShiftKeyDown) {
        EntityPlayer player = gui.entityPlayer;
        ItemStack currentStack = player.inventory.getItemStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
            return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;

        if (allowClickFilling && fluidTank.getFluidAmount() > 0) {
            boolean performedFill = false;
            FluidStack initialFluid = fluidTank.getFluid();
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack,
                        (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryFillContainer(currentStack, (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedFill = true;
            }
            if (performedFill && initialFluid != null) {
                SoundEvent soundevent = initialFluid.getFluid().getFillSound(initialFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                gui.entityPlayer.inventory.setItemStack(currentStack);
                return currentStack.getCount();
            }
        }

        if (allowClickEmptying) {
            boolean performedEmptying = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack,
                        (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryEmptyContainer(currentStack, (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedEmptying = true;
            }
            FluidStack filledFluid = fluidTank.getFluid();
            if (performedEmptying && filledFluid != null) {
                SoundEvent soundevent = filledFluid.getFluid().getEmptySound(filledFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                gui.entityPlayer.inventory.setItemStack(currentStack);
                return currentStack.getCount();
            }
        }

        return -1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if ((allowClickEmptying || allowClickFilling) && isMouseOverElement(mouseX, mouseY)) {
            ItemStack currentStack = gui.entityPlayer.inventory.getItemStack();
            if (button == 0 && currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                boolean isShiftKeyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                writeClientAction(1, writer -> writer.writeBoolean(isShiftKeyDown));
                playButtonClickSound();
                return this;
            }
        }
        return null;
    }
}
