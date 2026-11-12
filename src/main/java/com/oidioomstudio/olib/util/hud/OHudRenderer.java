package com.oidioomstudio.olib.util.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * High-performance HUD rendering helper for Oidioom Studio in-game overlays.
 */
public final class OHudRenderer {
    public enum HudAlignment {
        TOP_LEFT("olib.hud.top_left"),
        TOP_RIGHT("olib.hud.top_right"),
        BOTTOM_LEFT("olib.hud.bottom_left"),
        BOTTOM_RIGHT("olib.hud.bottom_right"),
        CENTER("olib.hud.center");

        private final String translationKey;

        HudAlignment(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    private OHudRenderer() {}

    /**
     * Renders a multi-line HUD card with automated positioning and optional stylish backdrop.
     */
    public static void renderHudCard(GuiGraphicsExtractor graphics, Font font, List<Component> lines,
                                     HudAlignment alignment, int offsetX, int offsetY,
                                     boolean drawBackground, int backgroundColor, int borderColor) {
        if (lines == null || lines.isEmpty()) return;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // Calculate maximum line width and total content height
        int maxLineWidth = 0;
        for (Component line : lines) {
            maxLineWidth = Math.max(maxLineWidth, font.width(line));
        }

        int paddingX = drawBackground ? 6 : 0;
        int paddingY = drawBackground ? 4 : 0;
        int cardWidth = maxLineWidth + (paddingX * 2);
        int cardHeight = (lines.size() * (font.lineHeight + 2)) - 2 + (paddingY * 2);

        int posX;
        int posY;

        switch (alignment) {
            case TOP_RIGHT -> {
                posX = screenWidth - cardWidth - offsetX;
                posY = offsetY;
            }
            case BOTTOM_LEFT -> {
                posX = offsetX;
                posY = screenHeight - cardHeight - offsetY - 20; // Above hotbar
            }
            case BOTTOM_RIGHT -> {
                posX = screenWidth - cardWidth - offsetX;
                posY = screenHeight - cardHeight - offsetY - 20;
            }
            case CENTER -> {
                posX = (screenWidth - cardWidth) / 2 + offsetX;
                posY = (screenHeight / 2) + 24 + offsetY;
            }
            default -> { // TOP_LEFT
                posX = offsetX;
                posY = offsetY;
            }
        }

        // Draw backdrop card if enabled
        if (drawBackground) {
            graphics.fill(posX, posY, posX + cardWidth, posY + cardHeight, backgroundColor);
            if (borderColor != 0) {
                graphics.horizontalLine(posX, posX + cardWidth - 1, posY, borderColor);
                graphics.horizontalLine(posX, posX + cardWidth - 1, posY + cardHeight - 1, borderColor);
                graphics.verticalLine(posX, posY, posY + cardHeight - 1, borderColor);
                graphics.verticalLine(posX + cardWidth - 1, posY, posY + cardHeight - 1, borderColor);
            }
        }

        // Render each line of text
        int currentY = posY + paddingY;
        for (Component line : lines) {
            graphics.text(font, line, posX + paddingX, currentY, 0xFFFFFFFF);
            currentY += font.lineHeight + 2;
        }
    }
}
