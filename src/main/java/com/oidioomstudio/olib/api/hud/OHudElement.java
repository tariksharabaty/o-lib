package com.oidioomstudio.olib.api.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Represents a modular in-game HUD element registered to O Lib.
 * Supports interactive dragging, positioning, grid snapping, and visibility toggling (Image 6).
 */
public class OHudElement {
    @FunctionalInterface
    public interface HudRenderer {
        void render(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker, int x, int y, int width, int height);
    }

    private final String id;
    private final Component title;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean visible;

    private final int defaultX;
    private final int defaultY;
    private final int defaultWidth;
    private final int defaultHeight;
    private final boolean defaultVisible;

    private int accentColor;
    private final HudRenderer renderer;

    public OHudElement(String id, Component title, int x, int y, int width, int height,
                        boolean visible, int accentColor, HudRenderer renderer) {
        this.id = id;
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = visible;

        this.defaultX = x;
        this.defaultY = y;
        this.defaultWidth = width;
        this.defaultHeight = height;
        this.defaultVisible = visible;

        this.accentColor = accentColor;
        this.renderer = renderer;
    }

    public String getId() {
        return id;
    }

    public Component getTitle() {
        return title;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    public void resetToDefault() {
        this.x = this.defaultX;
        this.y = this.defaultY;
        this.width = this.defaultWidth;
        this.height = this.defaultHeight;
        this.visible = this.defaultVisible;
    }

    public void render(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
        if (!visible || renderer == null) return;
        renderer.render(extractor, deltaTracker, x, y, width, height);
    }

    public static Builder builder(String id, Component title) {
        return new Builder(id, title);
    }

    public static class Builder {
        private final String id;
        private final Component title;
        private int x = 10;
        private int y = 10;
        private int width = 80;
        private int height = 30;
        private boolean visible = true;
        private int accentColor = 0xFF55FF55; // Default green
        private HudRenderer renderer;

        public Builder(String id, Component title) {
            this.id = id;
            this.title = title;
        }

        public Builder defaultPos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder defaultVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Builder accentColor(int color) {
            this.accentColor = color;
            return this;
        }

        public Builder renderer(HudRenderer renderer) {
            this.renderer = renderer;
            return this;
        }

        public OHudElement build() {
            return new OHudElement(id, title, x, y, width, height, visible, accentColor, renderer);
        }
    }
}
