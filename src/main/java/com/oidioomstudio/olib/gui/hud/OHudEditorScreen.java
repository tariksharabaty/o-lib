package com.oidioomstudio.olib.gui.hud;

import com.oidioomstudio.olib.api.hud.OHudElement;
import com.oidioomstudio.olib.api.hud.OHudRegistry;
import com.oidioomstudio.olib.gui.theme.OTheme;
import com.oidioomstudio.olib.gui.widgets.StoneButtonWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Collection;

/**
 * Interactive Visual HUD Position & Visibility Editor (Image 6).
 * Features:
 * - In-game screen overlay with dotted alignment grid and grid snapping
 * - Modular draggable HUD boxes with custom accent colors
 * - Top-right eye icon to toggle visibility (visible vs slashed eye)
 * - Dragging with live coordinates display
 * - Arrow key precision nudging
 * - Persistent configuration saving
 */
public class OHudEditorScreen extends Screen {
    private final Screen parent;
    private OHudElement selectedElement = null;
    private OHudElement draggingElement = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private boolean showGrid = true;
    private int snapGridSize = 16; // 0 = off, 8, 16, 32

    private StoneButtonWidget gridBtn;
    private StoneButtonWidget snapBtn;

    public OHudEditorScreen(Screen parent) {
        super(Component.translatable("olib.hud_editor.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int btnY = this.height - 26;
        int btnH = 20;
        int spacing = 6;

        Component doneText = Component.translatable("olib.config.done");
        Component resetText = Component.translatable("olib.hud_editor.reset");
        Component gridText = getGridBtnText();
        Component snapText = getSnapBtnText();

        Font font = Minecraft.getInstance().font;
        int doneW = Math.max(70, font.width(doneText) + 16);
        int resetW = Math.max(80, font.width(resetText) + 16);
        int gridW = Math.max(80, font.width(gridText) + 16);
        int snapW = Math.max(80, font.width(snapText) + 16);

        int curX = 8;
        // Bottom toolbar buttons with dynamic auto-adjusted width
        this.addRenderableWidget(new StoneButtonWidget(curX, btnY, doneW, btnH, doneText, this::onClose));
        curX += doneW + spacing;

        this.addRenderableWidget(new StoneButtonWidget(curX, btnY, resetW, btnH, resetText, () -> {
            OHudRegistry.resetAllToDefaults();
        }));
        curX += resetW + spacing;

        this.gridBtn = new StoneButtonWidget(curX, btnY, gridW, btnH, gridText, () -> {
            showGrid = !showGrid;
            this.gridBtn.setMessage(getGridBtnText());
        });
        this.addRenderableWidget(this.gridBtn);
        curX += gridW + spacing;

        this.snapBtn = new StoneButtonWidget(curX, btnY, snapW, btnH, snapText, () -> {
            if (snapGridSize == 16) snapGridSize = 8;
            else if (snapGridSize == 8) snapGridSize = 0;
            else snapGridSize = 16;
            this.snapBtn.setMessage(getSnapBtnText());
        });
        this.addRenderableWidget(this.snapBtn);
    }

    private Component getGridBtnText() {
        Component state = showGrid ?
            Component.translatable("olib.config.enabled").copy().withStyle(ChatFormatting.GREEN) :
            Component.translatable("olib.config.disabled").copy().withStyle(ChatFormatting.RED);
        return Component.translatable("olib.hud_editor.grid").append(": ").append(state);
    }

    private Component getSnapBtnText() {
        if (snapGridSize <= 0) {
            return Component.translatable("olib.hud_editor.snap").append(": ")
                .append(Component.translatable("olib.config.disabled").copy().withStyle(ChatFormatting.RED));
        }
        return Component.translatable("olib.hud_editor.snap").append(": ")
            .append(Component.literal(snapGridSize + "px").withStyle(ChatFormatting.GREEN));
    }

    private int getEyeX(OHudElement element) {
        return element.getX() + element.getWidth() - 15;
    }

    private int getEyeY(OHudElement element) {
        return element.getY() + 3;
    }

    private boolean isOverEye(OHudElement element, double mx, double my) {
        int ex = getEyeX(element);
        int ey = getEyeY(element);
        return mx >= ex - 2 && mx <= ex + 14 && my >= ey - 2 && my <= ey + 12;
    }

    private boolean isOverElement(OHudElement element, double mx, double my) {
        return mx >= element.getX() && mx <= element.getX() + element.getWidth() &&
               my >= element.getY() && my <= element.getY() + element.getHeight();
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        // Check HUD elements in reverse order (top to bottom)
        Collection<OHudElement> elements = OHudRegistry.getAll();
        for (OHudElement el : elements) {
            // Check Eye click
            if (isOverEye(el, mx, my)) {
                playClickSound();
                el.setVisible(!el.isVisible());
                OHudRegistry.saveLayout();
                return true;
            }

            // Check element box click
            if (isOverElement(el, mx, my)) {
                playClickSound();
                this.selectedElement = el;
                this.draggingElement = el;
                this.dragOffsetX = (int) (mx - el.getX());
                this.dragOffsetY = (int) (my - el.getY());
                return true;
            }
        }

        this.selectedElement = null;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.draggingElement != null) {
            OHudRegistry.saveLayout();
            this.draggingElement = null;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.draggingElement != null) {
            int newX = (int) (event.x() - dragOffsetX);
            int newY = (int) (event.y() - dragOffsetY);

            // Grid snapping
            if (snapGridSize > 0) {
                newX = Math.round((float) newX / snapGridSize) * snapGridSize;
                newY = Math.round((float) newY / snapGridSize) * snapGridSize;
            }

            // Clamp within screen bounds
            newX = Math.max(0, Math.min(this.width - draggingElement.getWidth(), newX));
            newY = Math.max(0, Math.min(this.height - draggingElement.getHeight(), newY));

            draggingElement.setX(newX);
            draggingElement.setY(newY);
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.selectedElement != null) {
            int step = event.hasShiftDown() ? 10 : 1;
            boolean moved = false;
            switch (event.key()) {
                case com.mojang.blaze3d.platform.InputConstants.KEY_LEFT -> {
                    selectedElement.setX(Math.max(0, selectedElement.getX() - step));
                    moved = true;
                }
                case com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT -> {
                    selectedElement.setX(Math.min(this.width - selectedElement.getWidth(), selectedElement.getX() + step));
                    moved = true;
                }
                case com.mojang.blaze3d.platform.InputConstants.KEY_UP -> {
                    selectedElement.setY(Math.max(0, selectedElement.getY() - step));
                    moved = true;
                }
                case com.mojang.blaze3d.platform.InputConstants.KEY_DOWN -> {
                    selectedElement.setY(Math.min(this.height - selectedElement.getHeight(), selectedElement.getY() + step));
                    moved = true;
                }
            }
            if (moved) {
                OHudRegistry.saveLayout();
                return true;
            }
        }

        if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        OHudRegistry.saveLayout();
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        // 1. In-game world backdrop with subtle dim
        extractor.fill(0, 0, this.width, this.height, 0x66000000);

        // 2. Alignment Grid Lines (High Performance - zero FPS drop)
        if (showGrid) {
            int gridStep = snapGridSize > 0 ? snapGridSize : 16;
            int gridColor = 0x14FFFFFF; // Subtle 1px guide line
            int centerColor = 0x4455FFFF; // Center crosshair lines

            // Center vertical & horizontal lines
            int midX = this.width / 2;
            int midY = this.height / 2;
            extractor.fill(midX, 0, midX + 1, this.height, centerColor);
            extractor.fill(0, midY, this.width, midY + 1, centerColor);

            // Fast continuous grid lines
            for (int x = gridStep; x < this.width; x += gridStep) {
                if (x == midX) continue;
                extractor.fill(x, 0, x + 1, this.height, gridColor);
            }
            for (int y = gridStep; y < this.height; y += gridStep) {
                if (y == midY) continue;
                extractor.fill(0, y, this.width, y + 1, gridColor);
            }
        }

        Font font = Minecraft.getInstance().font;

        // 3. Render Modular HUD Boxes
        Collection<OHudElement> elements = OHudRegistry.getAll();
        for (OHudElement el : elements) {
            int x = el.getX();
            int y = el.getY();
            int w = el.getWidth();
            int h = el.getHeight();
            boolean hovered = isOverElement(el, mouseX, mouseY);
            boolean selected = (el == this.selectedElement);
            int accent = el.getAccentColor();

            // Background fill (translucent with accent tint, matching Image 6)
            int bgAlpha = el.isVisible() ? 0x55000000 : 0x25000000;
            int bgColor = bgAlpha | (accent & 0x00FFFFFF);
            extractor.fill(x, y, x + w, y + h, bgColor);

            // Border (accent color, white if hovered or selected)
            int borderColor = (hovered || selected) ? 0xFFFFFFFF : accent;
            if (!el.isVisible()) {
                borderColor = 0x66888888;
            }
            OTheme.drawBorder(extractor, x, y, w, h, borderColor);

            // Centered Title Text
            int textColor = el.isVisible() ? 0xFFFFFFFF : 0xFFAAAAAA;
            int textY = y + (h - font.lineHeight) / 2;
            extractor.centeredText(font, el.getTitle(), x + w / 2, textY, textColor);

            // 4. Render Eye Icon in top-right corner (Image 6)
            int eyeX = getEyeX(el);
            int eyeY = getEyeY(el);
            boolean eyeHovered = isOverEye(el, mouseX, mouseY);
            renderEyeIcon(extractor, eyeX, eyeY, el.isVisible(), eyeHovered);

            // Dragging Coordinates Tooltip
            if (el == this.draggingElement) {
                String coords = "X: " + el.getX() + " Y: " + el.getY();
                extractor.fill(x, y - 14, x + font.width(coords) + 6, y - 2, 0xCC000000);
                extractor.text(font, coords, x + 3, y - 12, 0xFFFFAA00);
            }
        }

        // 5. Render toolbar buttons
        super.extractRenderState(extractor, mouseX, mouseY, partialTicks);
    }

    private void renderEyeIcon(GuiGraphicsExtractor extractor, int x, int y, boolean visible, boolean hovered) {
        // Eye outer shape: almond/oval (10px wide, 6px high)
        int eyeColor = hovered ? 0xFFFFFFFF : (visible ? 0xFF55FFFF : 0xFF888888);

        // Top and bottom borders
        extractor.fill(x + 2, y, x + 8, y + 1, eyeColor);
        extractor.fill(x + 2, y + 5, x + 8, y + 6, eyeColor);
        extractor.fill(x, y + 2, x + 1, y + 4, eyeColor);
        extractor.fill(x + 9, y + 2, x + 10, y + 4, eyeColor);
        extractor.fill(x + 1, y + 1, x + 2, y + 2, eyeColor);
        extractor.fill(x + 8, y + 1, x + 9, y + 2, eyeColor);
        extractor.fill(x + 1, y + 4, x + 2, y + 5, eyeColor);
        extractor.fill(x + 8, y + 4, x + 9, y + 5, eyeColor);

        // Pupil in the center
        int pupilColor = visible ? (hovered ? 0xFFFFFFFF : 0xFF00AAFF) : 0xFF555555;
        extractor.fill(x + 4, y + 2, x + 6, y + 4, pupilColor);

        // If not visible: diagonal red strike through line (Image 6)
        if (!visible) {
            int slashColor = hovered ? 0xFFFF7777 : 0xFFFF4444;
            for (int i = 0; i < 8; i++) {
                extractor.fill(x + i + 1, y + i - 1, x + i + 2, y + i, slashColor);
            }
        }
    }
}
