package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.api.options.EnumOption;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StoneEnumWidget extends AbstractWidget {
    private final EnumOption<?> option;
    private final Runnable onChange;

    public StoneEnumWidget(int x, int y, int width, int height, EnumOption<?> option, Runnable onChange) {
        super(x, y, width, height, Component.empty());
        this.option = option;
        this.onChange = onChange;
        this.updateMessage();
    }

    public void updateMessage() {
        this.setMessage(option.getCurrentDisplayName());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.active && this.visible && this.isHovered()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (event.x() < this.getX() + 32) {
                option.cyclePrevious();
            } else {
                option.cycle();
            }
            updateMessage();
            if (onChange != null) {
                onChange.run();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered();
        OTheme.drawDefaultButton(extractor, this.getX(), this.getY(), this.width, this.height, hovered, this.active);

        Font font = Minecraft.getInstance().font;
        int textColor = hovered ? OTheme.TEXT_WHITE : OTheme.TEXT_PRIMARY;
        int textY = this.getY() + (this.height - 8) / 2;

        // Left and right cycle arrows
        extractor.text(font, Component.literal("◀"), this.getX() + 8, textY, hovered ? 0xFFFFFFFF : 0xFFA0A0A0);
        extractor.text(font, Component.literal("▶"), this.getX() + this.width - 14, textY, hovered ? 0xFFFFFFFF : 0xFFA0A0A0);

        Component label = this.getMessage();
        int maxW = this.width - 32;
        boolean trimmed = false;
        if (font.width(label) > maxW && maxW > 10) {
            String str = label.getString();
            while (!str.isEmpty() && font.width(str + "..") > maxW) {
                str = str.substring(0, str.length() - 1);
            }
            label = Component.literal(str + "..");
            trimmed = true;
        }

        // Centered value text
        extractor.centeredText(font, label, this.getX() + this.width / 2, textY, textColor);

        if (hovered && trimmed) {
            extractor.setComponentTooltipForNextFrame(font, java.util.List.of(this.getMessage()), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
