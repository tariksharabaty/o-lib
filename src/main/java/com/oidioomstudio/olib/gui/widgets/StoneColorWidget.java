package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.api.options.ColorOption;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StoneColorWidget extends AbstractWidget {
    private final ColorOption option;
    private final Runnable onChange;

    private static final int[] PRESET_COLORS = {
        0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55,
        0xFF55FFFF, 0xFF5555FF, 0xFFFF55FF, 0xFFAAAAAA, 0xFF555555
    };

    public StoneColorWidget(int x, int y, int width, int height, ColorOption option, Runnable onChange) {
        super(x, y, width, height, Component.empty());
        this.option = option;
        this.onChange = onChange;
        this.updateMessage();
    }

    public void updateMessage() {
        this.setMessage(Component.literal(option.getHexFormatted()));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.active && this.visible && this.isHovered()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            Minecraft mc = Minecraft.getInstance();
            mc.setScreenAndShow(new com.oidioomstudio.olib.gui.OColorPickerScreen(mc.gui.screen(), option, () -> {
                updateMessage();
                if (onChange != null) {
                    onChange.run();
                }
            }));
            return true;
        }
        return false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered();
        OTheme.drawDefaultButton(extractor, this.getX(), this.getY(), this.width, this.height, hovered, this.active);

        // Color Swatch Preview Box on the left
        int swatchSize = 12;
        int swatchX = this.getX() + 8;
        int swatchY = this.getY() + (this.height - swatchSize) / 2;
        int color = option.getValue();

        com.oidioomstudio.olib.gui.OColorPickerScreen.drawCheckerboard(extractor, swatchX, swatchY, swatchSize, swatchSize, 3);
        extractor.fill(swatchX, swatchY, swatchX + swatchSize, swatchY + swatchSize, color);
        OTheme.drawBorder(extractor, swatchX, swatchY, swatchSize, swatchSize, 0xFFFFFFFF);

        Font font = Minecraft.getInstance().font;
        int textColor = hovered ? OTheme.TEXT_WHITE : OTheme.TEXT_PRIMARY;
        int textY = this.getY() + (this.height - 8) / 2;
        int textCenterX = this.getX() + (this.width + swatchSize + 8) / 2;

        Component label = this.getMessage();
        int maxW = this.width - swatchSize - 16;
        boolean trimmed = false;
        if (font.width(label) > maxW && maxW > 10) {
            String str = label.getString();
            while (!str.isEmpty() && font.width(str + "..") > maxW) {
                str = str.substring(0, str.length() - 1);
            }
            label = Component.literal(str + "..");
            trimmed = true;
        }

        extractor.centeredText(font, label, textCenterX, textY, textColor);

        if (hovered && trimmed) {
            extractor.setComponentTooltipForNextFrame(font, java.util.List.of(this.getMessage()), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
