package com.oidioomstudio.olib.gui;

import com.oidioomstudio.olib.api.options.ColorOption;
import com.oidioomstudio.olib.gui.theme.OTheme;
import com.oidioomstudio.olib.gui.widgets.StoneButtonWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Advanced Litematica / Masa style Color Picker Screen (Image 2).
 * Features:
 * - 2D Saturation / Value Gradient box with draggable crosshair handle
 * - Full spectrum Hue rainbow slider
 * - Alpha transparency slider with checkerboard underlay
 * - Large live color preview swatch
 * - Interactive Hex input box (#AARRGGBB) with bi-directional synchronization
 */
public class OColorPickerScreen extends Screen {
    private final Screen parent;
    private final ColorOption option;
    private final Runnable onChange;

    // HSV & Alpha state
    private float hue = 0.0f;        // 0.0f to 360.0f
    private float saturation = 1.0f; // 0.0f to 1.0f
    private float value = 1.0f;      // 0.0f to 1.0f
    private int alpha = 255;         // 0 to 255

    // Dragging state
    private enum DragTarget { NONE, SV_AREA, HUE_BAR, ALPHA_BAR }
    private DragTarget dragTarget = DragTarget.NONE;

    // Dialog layout coordinates
    private int dialogX;
    private int dialogY;
    private static final int DIALOG_WIDTH = 340;
    private static final int DIALOG_HEIGHT = 150;

    // Sub-regions
    private int swatchX, swatchY, swatchW, swatchH;
    private int svX, svY, svW, svH;
    private int hueX, hueY, hueW, hueH;
    private int alphaX, alphaY, alphaW, alphaH;

    // Hex input
    private EditBox hexInput;
    private boolean updatingHexFromSliders = false;

    public OColorPickerScreen(Screen parent, ColorOption option, Runnable onChange) {
        super(Component.translatable("olib.color_picker.title"));
        this.parent = parent;
        this.option = option;
        this.onChange = onChange;

        // Parse initial color
        int initialColor = option.getValue();
        this.alpha = (initialColor >> 24) & 0xFF;
        float[] hsv = rgbToHsv(initialColor);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    @Override
    protected void init() {
        super.init();

        this.dialogX = (this.width - DIALOG_WIDTH) / 2;
        this.dialogY = (this.height - DIALOG_HEIGHT) / 2;

        // Layout regions based on Image 2:
        // Top Left: Large Color Preview Swatch
        this.swatchX = dialogX + 8;
        this.swatchY = dialogY + 8;
        this.swatchW = 86;
        this.swatchH = 50;

        // Top Right: 2D Saturation / Value Gradient Area
        this.svX = dialogX + 100;
        this.svY = dialogY + 8;
        this.svW = DIALOG_WIDTH - 108; // 232px
        this.svH = 50;

        // Middle: Hue Rainbow Slider
        this.hueX = dialogX + 8;
        this.hueY = dialogY + 64;
        this.hueW = DIALOG_WIDTH - 16; // 324px
        this.hueH = 14;

        // Bottom: Alpha Slider
        this.alphaX = dialogX + 8;
        this.alphaY = dialogY + 84;
        this.alphaW = DIALOG_WIDTH - 16; // 324px
        this.alphaH = 14;

        // Bottom Bar: Hex Input Box & Done Button
        int hexW = 90;
        int hexH = 18;
        int hexX = dialogX + DIALOG_WIDTH - hexW - 8;
        int hexY = dialogY + 114;

        Font font = Minecraft.getInstance().font;
        this.hexInput = new EditBox(font, hexX, hexY, hexW, hexH, Component.literal("#"));
        this.hexInput.setMaxLength(9); // e.g. #FFFFFFFF
        this.updateHexInputText();

        this.hexInput.setResponder(text -> {
            if (updatingHexFromSliders) return;
            String clean = text.startsWith("#") ? text.substring(1) : text;
            try {
                if (clean.length() == 6) {
                    int rgb = (int) Long.parseLong(clean, 16);
                    float[] hsv = rgbToHsv(rgb);
                    this.hue = hsv[0];
                    this.saturation = hsv[1];
                    this.value = hsv[2];
                    this.syncColorToOption();
                } else if (clean.length() == 8) {
                    long argb = Long.parseLong(clean, 16);
                    this.alpha = (int) ((argb >> 24) & 0xFF);
                    float[] hsv = rgbToHsv((int) (argb & 0x00FFFFFF));
                    this.hue = hsv[0];
                    this.saturation = hsv[1];
                    this.value = hsv[2];
                    this.syncColorToOption();
                }
            } catch (NumberFormatException ignored) {}
        });

        this.addRenderableWidget(this.hexInput);

        // Done button on the bottom left (auto-sized to text)
        Component doneText = Component.translatable("olib.config.done");
        int doneW = Math.max(70, this.font.width(doneText) + 16);
        this.addRenderableWidget(new StoneButtonWidget(dialogX + 8, dialogY + 112, doneW, 20,
            doneText, () -> onClose()));
    }

    private int getCurrentColor() {
        int rgb = hsvToRgb(hue, saturation, value);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    private void syncColorToOption() {
        int col = getCurrentColor();
        this.option.setValue(col);
        if (onChange != null) {
            onChange.run();
        }
    }

    private void updateHexInputText() {
        this.updatingHexFromSliders = true;
        int col = getCurrentColor();
        String hex = String.format("#%08X", col);
        if (this.hexInput != null) {
            this.hexInput.setValue(hex);
        }
        this.updatingHexFromSliders = false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        if (mx >= svX && mx <= svX + svW && my >= svY && my <= svY + svH) {
            this.dragTarget = DragTarget.SV_AREA;
            updateSVFromMouse(mx, my);
            return true;
        } else if (mx >= hueX && mx <= hueX + hueW && my >= hueY && my <= hueY + hueH) {
            this.dragTarget = DragTarget.HUE_BAR;
            updateHueFromMouse(mx);
            return true;
        } else if (mx >= alphaX && mx <= alphaX + alphaW && my >= alphaY && my <= alphaY + alphaH) {
            this.dragTarget = DragTarget.ALPHA_BAR;
            updateAlphaFromMouse(mx);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.dragTarget = DragTarget.NONE;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mx = event.x();
        double my = event.y();

        if (this.dragTarget == DragTarget.SV_AREA) {
            updateSVFromMouse(mx, my);
            return true;
        } else if (this.dragTarget == DragTarget.HUE_BAR) {
            updateHueFromMouse(mx);
            return true;
        } else if (this.dragTarget == DragTarget.ALPHA_BAR) {
            updateAlphaFromMouse(mx);
            return true;
        }

        return super.mouseDragged(event, deltaX, deltaY);
    }

    private void updateSVFromMouse(double mx, double my) {
        float s = (float) ((mx - svX) / (double) svW);
        float v = (float) (1.0 - ((my - svY) / (double) svH));
        this.saturation = Math.max(0.0f, Math.min(1.0f, s));
        this.value = Math.max(0.0f, Math.min(1.0f, v));
        syncColorToOption();
        updateHexInputText();
    }

    private void updateHueFromMouse(double mx) {
        float h = (float) ((mx - hueX) / (double) hueW) * 360.0f;
        this.hue = Math.max(0.0f, Math.min(360.0f, h));
        syncColorToOption();
        updateHexInputText();
    }

    private void updateAlphaFromMouse(double mx) {
        float a = (float) ((mx - alphaX) / (double) alphaW) * 255.0f;
        this.alpha = Math.max(0, Math.min(255, Math.round(a)));
        syncColorToOption();
        updateHexInputText();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        syncColorToOption();
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        // Darkened background backdrop
        extractor.fill(0, 0, this.width, this.height, 0x88000000);

        // 1. Dialog background frame
        extractor.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xF018181B);
        OTheme.drawBorder(extractor, dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT, 0xFFFFFFFF);
        OTheme.drawBorder(extractor, dialogX + 1, dialogY + 1, DIALOG_WIDTH - 2, DIALOG_HEIGHT - 2, 0xFF3A3A3E);

        // 2. Large Color Preview Swatch (Top Left)
        drawCheckerboard(extractor, swatchX, swatchY, swatchW, swatchH, 6);
        extractor.fill(swatchX, swatchY, swatchX + swatchW, swatchY + swatchH, getCurrentColor());
        OTheme.drawBorder(extractor, swatchX, swatchY, swatchW, swatchH, 0xFFFFFFFF);

        // 3. 2D Saturation / Value Gradient Box (Top Right)
        for (int x = 0; x < svW; x++) {
            float s = (float) x / (float) svW;
            int topRgb = 0xFF000000 | hsvToRgb(hue, s, 1.0f);
            int bottomRgb = 0xFF000000; // Black
            extractor.fillGradient(svX + x, svY, svX + x + 1, svY + svH, topRgb, bottomRgb);
        }
        OTheme.drawBorder(extractor, svX, svY, svW, svH, 0xFFFFFFFF);

        // 2D Cursor Handle
        int cursorX = (int) (svX + saturation * svW);
        int cursorY = (int) (svY + (1.0f - value) * svH);
        extractor.fill(cursorX - 2, cursorY - 2, cursorX + 3, cursorY + 3, 0xFFFFFFFF);
        OTheme.drawBorder(extractor, cursorX - 3, cursorY - 3, 6, 6, 0xFF000000);

        // 4. Hue Rainbow Slider Bar (Middle)
        for (int x = 0; x < hueW; x++) {
            float h = (float) x / (float) hueW * 360.0f;
            int col = 0xFF000000 | hsvToRgb(h, 1.0f, 1.0f);
            extractor.fill(hueX + x, hueY, hueX + x + 1, hueY + hueH, col);
        }
        OTheme.drawBorder(extractor, hueX, hueY, hueW, hueH, 0xFFFFFFFF);

        // Hue Handle
        int hueHandleX = (int) (hueX + (hue / 360.0f) * hueW);
        extractor.fill(hueHandleX - 2, hueY - 2, hueHandleX + 2, hueY + hueH + 2, 0xFFFFFFFF);
        OTheme.drawBorder(extractor, hueHandleX - 2, hueY - 2, 4, hueH + 4, 0xFF000000);

        // 5. Alpha Transparency Slider Bar (Bottom)
        drawCheckerboard(extractor, alphaX, alphaY, alphaW, alphaH, 4);
        int pureRgb = hsvToRgb(hue, saturation, value) & 0x00FFFFFF;
        for (int x = 0; x < alphaW; x++) {
            int a = Math.round((float) x / (float) alphaW * 255.0f);
            int col = (a << 24) | pureRgb;
            extractor.fill(alphaX + x, alphaY, alphaX + x + 1, alphaY + alphaH, col);
        }
        OTheme.drawBorder(extractor, alphaX, alphaY, alphaW, alphaH, 0xFFFFFFFF);

        // Alpha Handle
        int alphaHandleX = (int) (alphaX + (alpha / 255.0f) * alphaW);
        extractor.fill(alphaHandleX - 2, alphaY - 2, alphaHandleX + 2, alphaY + alphaH + 2, 0xFFFFFFFF);
        OTheme.drawBorder(extractor, alphaHandleX - 2, alphaY - 2, 4, alphaH + 4, 0xFF000000);

        // 6. Bottom Mini Swatch next to Hex field
        int miniSwatchX = hexInput.getX() - 20;
        int miniSwatchY = hexInput.getY() + 2;
        int miniSwatchSize = 14;
        drawCheckerboard(extractor, miniSwatchX, miniSwatchY, miniSwatchSize, miniSwatchSize, 3);
        extractor.fill(miniSwatchX, miniSwatchY, miniSwatchX + miniSwatchSize, miniSwatchY + miniSwatchSize, getCurrentColor());
        OTheme.drawBorder(extractor, miniSwatchX, miniSwatchY, miniSwatchSize, miniSwatchSize, 0xFFFFFFFF);

        // Render widgets (Done button and Hex input)
        super.extractRenderState(extractor, mouseX, mouseY, partialTicks);
    }

    public static void drawCheckerboard(GuiGraphicsExtractor extractor, int x, int y, int w, int h, int tileSize) {
        int c1 = 0xFFFFFFFF;
        int c2 = 0xFFCCCCCC;
        for (int py = y; py < y + h; py += tileSize) {
            int th = Math.min(tileSize, y + h - py);
            for (int px = x; px < x + w; px += tileSize) {
                int tw = Math.min(tileSize, x + w - px);
                int color = (((px - x) / tileSize + (py - y) / tileSize) % 2 == 0) ? c1 : c2;
                extractor.fill(px, py, px + tw, py + th, color);
            }
        }
    }

    public static int hsvToRgb(float h, float s, float v) {
        if (s <= 0.0001f) {
            int val = Math.round(v * 255.0f);
            return (val << 16) | (val << 8) | val;
        }
        float hueNorm = (h % 360.0f) / 60.0f;
        int i = (int) Math.floor(hueNorm);
        float f = hueNorm - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));
        float r = 0, g = 0, b = 0;
        switch (i) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        int red = Math.max(0, Math.min(255, Math.round(r * 255.0f)));
        int green = Math.max(0, Math.min(255, Math.round(g * 255.0f)));
        int blue = Math.max(0, Math.min(255, Math.round(b * 255.0f)));
        return (red << 16) | (green << 8) | blue;
    }

    public static float[] rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float h = 0.0f;
        if (delta > 0.0001f) {
            if (max == r) {
                h = 60.0f * (((g - b) / delta) % 6.0f);
                if (h < 0.0f) h += 360.0f;
            } else if (max == g) {
                h = 60.0f * (((b - r) / delta) + 2.0f);
            } else {
                h = 60.0f * (((r - g) / delta) + 4.0f);
            }
        }
        float s = max > 0.0001f ? delta / max : 0.0f;
        float v = max;
        return new float[]{h, s, v};
    }
}
