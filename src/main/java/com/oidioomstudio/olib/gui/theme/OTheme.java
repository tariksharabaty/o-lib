package com.oidioomstudio.olib.gui.theme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Visual styling system using Minecraft's default button sprites and authentic UI elements.
 */
public final class OTheme {
    // Official Minecraft Button Sprites (uses active texture pack or default vanilla)
    public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
        Identifier.withDefaultNamespace("widget/button"),
        Identifier.withDefaultNamespace("widget/button_disabled"),
        Identifier.withDefaultNamespace("widget/button_highlighted")
    );

    // Text colors (Following standard title & sentence case)
    public static final int TEXT_PRIMARY = 0xFFE0E0E0;
    public static final int TEXT_MUTED = 0xFFA0A0A8;
    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_DISABLED = 0xFFA0A0A0;
    public static final int TEXT_HIGHLIGHT = 0xFFFFFF99;
    public static final int TEXT_GREEN = 0xFF55FF55;
    public static final int TEXT_RED = 0xFFFF5555;
    public static final int ACCENT_LIGHT_BLUE = 0xFF55FFFF; // Standard Minecraft Aqua / Light Blue

    // Selected tab border
    public static final int TAB_SELECTED_BORDER = 0xFFFFFFFF;

    // Green / Red LED indicator square (from Image 2)
    public static final int INDICATOR_GREEN_FILL = 0xFF55FF55;
    public static final int INDICATOR_GREEN_LIGHT = 0xFF88FF88;
    public static final int INDICATOR_GREEN_BORDER = 0xFF1E661E;

    public static final int INDICATOR_RED_FILL = 0xFFFF5555;
    public static final int INDICATOR_RED_LIGHT = 0xFFFF8888;
    public static final int INDICATOR_RED_BORDER = 0xFF661E1E;

    // Search popup box colors
    public static final int POPUP_BG = 0xF018181C;
    public static final int POPUP_BORDER = 0xFFFFFFFF;
    public static final int POPUP_BORDER_NORMAL = 0xFF555560;

    // Standard Layout dimensions
    public static final int LABEL_WIDTH = 350;
    public static final int WIDGET_WIDTH = 380;
    public static final int RESET_WIDTH = 70;
    public static final int RESET_HEIGHT = 20;

    private OTheme() {}

    /**
     * Renders a button using Minecraft's official vanilla button sprites.
     */
    public static void drawDefaultButton(GuiGraphicsExtractor extractor, int x, int y, int width, int height,
                                         boolean hovered, boolean enabled) {
        Identifier sprite = BUTTON_SPRITES.get(enabled, hovered);
        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    /**
     * Renders a category tab button. Selected tab gets dark stone background + crisp white border to stand out distinctly.
     */
    public static void drawTabButton(GuiGraphicsExtractor extractor, int x, int y, int width, int height,
                                     boolean selected, boolean hovered) {
        if (selected) {
            // Selected tab: Clean dark stone texture without white border
            extractor.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_SPRITES.disabled(), x, y, width, height);
        } else {
            drawDefaultButton(extractor, x, y, width, height, hovered, true);
        }
    }

    /**
     * Renders a boolean toggle button using standard active stone texture for both states,
     * with text color (green for true, red for false) indicating the state.
     */
    public static void drawToggleButton(GuiGraphicsExtractor extractor, int x, int y, int width, int height,
                                       boolean value, boolean hovered, boolean enabled) {
        if (!enabled) {
            drawDefaultButton(extractor, x, y, width, height, false, false);
            return;
        }

        // Standard active stone button background for both true and false
        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_SPRITES.get(true, hovered), x, y, width, height);
        if (hovered) {
            drawBorder(extractor, x, y, width, height, TAB_SELECTED_BORDER);
        }
    }

    /**
     * Draws the square LED indicator from image 2 (bright green or red).
     */
    public static void drawIndicator(GuiGraphicsExtractor extractor, int x, int y, int size, boolean active) {
        int fill = active ? INDICATOR_GREEN_FILL : INDICATOR_RED_FILL;
        int light = active ? INDICATOR_GREEN_LIGHT : INDICATOR_RED_LIGHT;
        int border = active ? INDICATOR_GREEN_BORDER : INDICATOR_RED_BORDER;

        // Outer border
        drawBorder(extractor, x, y, size, size, border);

        // Core fill
        extractor.fill(x + 1, y + 1, x + size - 1, y + size - 1, fill);

        // Top-left pixel highlight
        extractor.horizontalLine(x + 1, x + size - 2, y + 1, light);
        extractor.verticalLine(x + 1, y + 1, y + size - 2, light);
    }

    /**
     * Draws a 1-pixel rectangle border around the given bounding box.
     */
    public static void drawBorder(GuiGraphicsExtractor extractor, int x, int y, int width, int height, int color) {
        extractor.horizontalLine(x, x + width - 1, y, color);
        extractor.horizontalLine(x, x + width - 1, y + height - 1, color);
        extractor.verticalLine(x, y, y + height - 1, color);
        extractor.verticalLine(x + width - 1, y, y + height - 1, color);
    }

    /**
     * Draws a crisp 7x7 pixel-art magnifying glass icon compatible with all Minecraft renderers.
     */
    public static void drawSearchIcon(GuiGraphicsExtractor extractor, int x, int y, int color) {
        // Lens circle (5x5 outline)
        extractor.fill(x + 1, y, x + 4, y + 1, color);         // Top edge
        extractor.fill(x + 1, y + 4, x + 4, y + 5, color);     // Bottom edge
        extractor.fill(x, y + 1, x + 1, y + 4, color);         // Left edge
        extractor.fill(x + 4, y + 1, x + 5, y + 4, color);     // Right edge

        // Diagonal handle (3 pixels)
        extractor.fill(x + 4, y + 4, x + 5, y + 5, color);
        extractor.fill(x + 5, y + 5, x + 6, y + 6, color);
        extractor.fill(x + 6, y + 6, x + 7, y + 7, color);
    }
}
