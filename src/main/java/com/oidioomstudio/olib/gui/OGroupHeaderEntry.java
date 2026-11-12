package com.oidioomstudio.olib.gui;

import com.oidioomstudio.olib.api.OConfigGroup;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

/**
 * Renders a stylish subcategory/group section header in {@link OOptionList}.
 */
public class OGroupHeaderEntry extends OBaseListEntry {
    private final OConfigGroup group;

    public OGroupHeaderEntry(OConfigGroup group) {
        this.group = group;
    }

    public OConfigGroup getGroup() {
        return group;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return Collections.emptyList();
    }

    @Override
    public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY, boolean hovering, float partialTicks) {
        int x = this.getContentX();
        int y = this.getContentY();
        int right = this.getContentRight();
        int height = this.getContentHeight();

        Font font = Minecraft.getInstance().font;

        // Group Header banner dimensions
        int bannerY = y + 2;
        int bannerH = height - 4;

        // 1. Subtle translucent stone backdrop
        extractor.fill(x, bannerY, right, bannerY + bannerH, 0x55141416);

        // 2. Left light blue accent indicator bar
        extractor.fill(x, bannerY, x + 3, bannerY + bannerH, OTheme.ACCENT_LIGHT_BLUE);

        // 3. Subtle bottom border
        extractor.fill(x, bannerY + bannerH - 1, right, bannerY + bannerH, 0x3355555A);

        // 4. Header title text (light blue / aqua, not bold, slightly larger ~1.15x)
        Component titleComp = group.getTitle().copy().withStyle(ChatFormatting.AQUA);
        int textX = x + 10;
        float scale = 1.15f;
        float textY = y + (height - (font.lineHeight * scale)) / 2.0f;

        extractor.pose().pushMatrix();
        extractor.pose().translate(textX, textY);
        extractor.pose().scale(scale, scale);
        extractor.text(font, titleComp, 0, 0, OTheme.ACCENT_LIGHT_BLUE);
        extractor.pose().popMatrix();

        // 5. Header tooltip on hover (No inline description next to subheader per design rule)
        if (group.getDescription() != null) {
            if (mouseX >= x && mouseX <= right && mouseY >= y && mouseY <= y + height) {
                extractor.setComponentTooltipForNextFrame(font, List.of(
                    group.getTitle().copy().withStyle(ChatFormatting.AQUA),
                    group.getDescription().copy().withStyle(ChatFormatting.GRAY)
                ), mouseX, mouseY);
            }
        }
    }
}
