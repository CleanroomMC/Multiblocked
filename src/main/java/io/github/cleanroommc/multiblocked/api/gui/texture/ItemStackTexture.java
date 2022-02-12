package io.github.cleanroommc.multiblocked.api.gui.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackTexture implements IGuiTexture{
    private final ItemStack[] itemStack;
    private int index = 0;
    private int ticks = 0;

    public ItemStackTexture(ItemStack... itemStacks) {
        this.itemStack = itemStacks;
    }

    public ItemStackTexture(Item... items) {
        this.itemStack = new ItemStack[items.length];
        for(int i = 0; i < items.length; i++) {
            itemStack[i] = new ItemStack(items[i]);
        }
    }

    @Override
    public void updateTick() {
        if(itemStack.length > 1 && ++ticks % 20 == 0)
            if(++index == itemStack.length)
                index = 0;
    }

    @Override
    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.scale(width / 16f, height / 16f, 0.0001);
        GlStateManager.translate(x * 16 / width, y * 16 / height, 0);
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(itemStack[index], 0, 0);
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }
}
