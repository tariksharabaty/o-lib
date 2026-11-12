package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Standard button using Minecraft's default button sprites with perfectly centered text.
 */
public class StoneButtonWidget extends AbstractWidget {
    private final Runnable onPress;

    public StoneButtonWidget(int x, int y, int width, int height, Component message, Runnable onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.active && this.visible && this.isHovered()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (onPress != null) {
                onPress.run();
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
        int textColor = !this.active ? OTheme.TEXT_DISABLED : (hovered ? OTheme.TEXT_WHITE : OTheme.TEXT_PRIMARY);
        int textY = this.getY() + (this.height - 8) / 2;

        Component label = this.getMessage();
        int maxW = this.width - 6;
        boolean trimmed = false;
        if (font.width(label) > maxW && maxW > 10) {
            String str = label.getString();
            while (!str.isEmpty() && font.width(str + "..") > maxW) {
                str = str.substring(0, str.length() - 1);
            }
            label = Component.literal(str + "..");
            trimmed = true;
        }

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
