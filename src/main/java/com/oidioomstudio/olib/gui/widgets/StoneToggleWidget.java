package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.api.options.BooleanOption;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StoneToggleWidget extends AbstractWidget {
    private final BooleanOption option;
    private final Runnable onChange;

    public StoneToggleWidget(int x, int y, int width, int height, BooleanOption option, Runnable onChange) {
        super(x, y, width, height, Component.empty());
        this.option = option;
        this.onChange = onChange;
        this.updateMessage();
    }

    public void updateMessage() {
        this.setMessage(option.getValue() ? option.getTrueLabel() : option.getFalseLabel());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.active && this.visible && this.isHovered()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            option.toggle();
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
        boolean val = option.getValue();
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        Font font = Minecraft.getInstance().font;
        int textY = y + (h - font.lineHeight) / 2;

        if (option.isActionButton()) {
            // Action / Menu opening button: Standard stone button texture and clean white text
            OTheme.drawDefaultButton(extractor, x, y, w, h, hovered, this.active);
            int textColor = !this.active ? OTheme.TEXT_DISABLED : (hovered ? OTheme.TEXT_WHITE : 0xFFEDEDED);

            Component label = this.getMessage();
            int maxW = w - 6;
            boolean trimmed = false;
            if (font.width(label) > maxW && maxW > 10) {
                String str = label.getString();
                while (!str.isEmpty() && font.width(str + "..") > maxW) {
                    str = str.substring(0, str.length() - 1);
                }
                label = Component.literal(str + "..");
                trimmed = true;
            }
            extractor.centeredText(font, label, x + w / 2, textY, textColor);
            if (hovered && trimmed) {
                extractor.setComponentTooltipForNextFrame(font, java.util.List.of(this.getMessage()), mouseX, mouseY);
            }
            return;
        }

        // 1. Draw toggle button (Dark texture for passive/OFF, standard stone button texture for active/ON)
        OTheme.drawToggleButton(extractor, x, y, w, h, val, hovered, this.active);

        // 2. Pixel-perfect centered text
        // Custom labels (e.g., "Envantere" / "Yere Düşür") render in clean white text.
        // Default on/off toggles (Açık/Kapalı) render in Green/Red.
        int textColor;
        if (!this.active) {
            textColor = OTheme.TEXT_DISABLED;
        } else if (option.hasCustomLabels()) {
            textColor = hovered ? OTheme.TEXT_WHITE : 0xFFEDEDED;
        } else if (val) {
            textColor = hovered ? 0xFF88FF88 : 0xFF55FF55; // Green
        } else {
            textColor = hovered ? 0xFFFF8888 : 0xFFFF5555; // Red
        }

        Component label = this.getMessage();
        int maxW = w - 6;
        boolean trimmed = false;
        if (font.width(label) > maxW && maxW > 10) {
            String str = label.getString();
            while (!str.isEmpty() && font.width(str + "..") > maxW) {
                str = str.substring(0, str.length() - 1);
            }
            label = Component.literal(str + "..");
            trimmed = true;
        }

        extractor.centeredText(font, label, x + w / 2, textY, textColor);

        if (hovered && trimmed) {
            extractor.setComponentTooltipForNextFrame(font, java.util.List.of(this.getMessage()), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
