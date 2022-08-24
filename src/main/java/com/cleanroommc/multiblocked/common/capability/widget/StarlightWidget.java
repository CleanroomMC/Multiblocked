package com.cleanroommc.multiblocked.common.capability.widget;

import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.common.recipe.content.Starlight;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.item.useables.ItemShiftingStar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.StringJoiner;

/**
 * @author youyihj
 */
public class StarlightWidget extends ContentWidget<Starlight> {

    @Nullable
    @Override
    public Object getJEIIngredient(Starlight content) {
        return null;
    }

    @Override
    protected void onContentUpdate() {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add(LocalizationUtils.format("multiblocked.gui.trait.starlight.starlight", content.getValue()));
        IConstellation constellation = content.getConstellation();
        if (constellation != null) {
            stringJoiner.add(LocalizationUtils.format("multiblocked.gui.trait.starlight.constellation", LocalizationUtils.format(constellation.getUnlocalizedName())));
        } else {
            stringJoiner.add(LocalizationUtils.format("multiblocked.gui.trait.starlight.no_constellation"));
        }
        ItemStack texture = ItemShiftingStar.createStack(constellation instanceof IMajorConstellation ? ((IMajorConstellation) constellation) : null);
        setBackground(new ItemStackTexture(texture));
        setHoverTooltip(stringJoiner.toString());
    }

    @Override
    public void openConfigurator(WidgetGroup dialog) {
        super.openConfigurator(dialog);
        int x = 5;
        int y = 25;
        dialog.addWidget(new LabelWidget(x, y + 3, "Starlight: "));
        dialog.addWidget(new TextFieldWidget(125 - 60, y, 60, 15, true, null, number -> {
            content = new Starlight(Integer.parseInt(number), content.getConstellation());
            onContentUpdate();
        }).setNumbersOnly(1, Integer.MAX_VALUE).setCurrentString(content.getValue()+""));
        dialog.addWidget(new LabelWidget(x, y + 23, "Constellation: "));
        dialog.addWidget(new TextFieldWidget(125 - 60, y + 20, 60, 15, true, null, number -> {
            IConstellation constellation = ConstellationRegistry.getConstellationByName(number);
            content = new Starlight(content.getValue(), constellation);
            onContentUpdate();
        }).setCurrentString(content.getConstellation() == null ? "null" : content.getConstellation().getUnlocalizedName()));
    }

    @Override
    protected void drawHookBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        GlStateManager.scale(0.5, 0.5, 1);
        GlStateManager.disableDepth();
        String s = TextFormattingUtil.formatLongToCompactString(content.getValue(), 4);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (position.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 21, (position.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
        GlStateManager.scale(2, 2, 1);
    }
}
