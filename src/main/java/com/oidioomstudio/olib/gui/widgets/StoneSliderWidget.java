package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.api.options.FloatSliderOption;
import com.oidioomstudio.olib.api.options.IntegerSliderOption;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StoneSliderWidget extends AbstractWidget {
    private final IntegerSliderOption intOption;
    private final FloatSliderOption floatOption;
    private final Runnable onChange;
    private boolean dragging = false;

    // Dual-Mode: Slider vs Textfield Mode (Image 4 / Image 3) - Default is Text Mode
    private boolean textMode = true;
    private final EditBox editBox;
    private static final int MODE_BTN_WIDTH = 20;

    public StoneSliderWidget(int x, int y, int width, int height, IntegerSliderOption option, Runnable onChange) {
        super(x, y, width, height, Component.empty());
        this.intOption = option;
        this.floatOption = null;
        this.onChange = onChange;

        Font font = Minecraft.getInstance().font;
        this.editBox = new EditBox(font, x, y, Math.max(10, width - MODE_BTN_WIDTH - 2), height, Component.empty());
        this.editBox.setValue(String.valueOf(option.getValue()));
        this.setupEditBoxResponder();
        this.updateMessage();
    }

    public StoneSliderWidget(int x, int y, int width, int height, FloatSliderOption option, Runnable onChange) {
        super(x, y, width, height, Component.empty());
        this.intOption = null;
        this.floatOption = option;
        this.onChange = onChange;

        Font font = Minecraft.getInstance().font;
        this.editBox = new EditBox(font, x, y, Math.max(10, width - MODE_BTN_WIDTH - 2), height, Component.empty());
        this.editBox.setValue(String.valueOf(option.getValue()));
        this.setupEditBoxResponder();
        this.updateMessage();
    }

    private void setupEditBoxResponder() {
        this.editBox.setResponder(val -> {
            if (!textMode) return;
            try {
                if (intOption != null) {
                    int parsed = Integer.parseInt(val.trim());
                    if (parsed >= intOption.getMin() && parsed <= intOption.getMax()) {
                        intOption.setValue(parsed);
                        this.setMessage(intOption.formatValue(parsed));
                        if (onChange != null) onChange.run();
                    }
                } else if (floatOption != null) {
                    float parsed = Float.parseFloat(val.trim());
                    if (parsed >= floatOption.getMin() && parsed <= floatOption.getMax()) {
                        floatOption.setValue(parsed);
                        this.setMessage(floatOption.formatValue(parsed));
                        if (onChange != null) onChange.run();
                    }
                }
            } catch (NumberFormatException ignored) {}
        });
    }

    public void updateMessage() {
        if (intOption != null) {
            this.setMessage(intOption.formatValue(intOption.getValue()));
            if (!this.editBox.isFocused()) {
                this.editBox.setValue(String.valueOf(intOption.getValue()));
            }
        } else if (floatOption != null) {
            this.setMessage(floatOption.formatValue(floatOption.getValue()));
            if (!this.editBox.isFocused()) {
                this.editBox.setValue(String.valueOf(floatOption.getValue()));
            }
        }
    }

    private double getRatio() {
        if (intOption != null) {
            return intOption.getRatio();
        } else if (floatOption != null) {
            return floatOption.getRatio();
        }
        return 0.0;
    }

    private void setRatio(double ratio) {
        if (intOption != null) {
            intOption.setFromRatio(ratio);
        } else if (floatOption != null) {
            floatOption.setFromRatio(ratio);
        }
        updateMessage();
        if (onChange != null) {
            onChange.run();
        }
    }

    private int getMainControlWidth() {
        return Math.max(10, this.width - MODE_BTN_WIDTH - 2);
    }

    private int getModeBtnX() {
        return this.getX() + this.width - MODE_BTN_WIDTH;
    }

    private boolean isOverModeButton(double mouseX, double mouseY) {
        int btnX = getModeBtnX();
        return mouseX >= btnX && mouseX <= btnX + MODE_BTN_WIDTH && mouseY >= this.getY() && mouseY <= this.getY() + this.height;
    }

    private void updateRatioFromMouse(double mouseX) {
        int mainW = getMainControlWidth();
        double ratio = (mouseX - (this.getX() + 4)) / (double) (mainW - 8);
        setRatio(ratio);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || !this.visible) {
            return false;
        }

        // 1. Click on Mode Switch Button ([T] or [||])
        if (isOverModeButton(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.textMode = !this.textMode;
            this.updateMessage();
            if (this.textMode) {
                this.editBox.setFocused(true);
            } else {
                this.editBox.setFocused(false);
            }
            return true;
        }

        // 2. Click in Text Mode -> Delegate to EditBox
        if (this.textMode) {
            boolean handled = this.editBox.mouseClicked(event, doubleClick);
            if (handled) {
                this.editBox.setFocused(true);
            }
            return handled;
        }

        // 3. Click in Slider Mode
        int mainW = getMainControlWidth();
        if (event.x() >= this.getX() && event.x() <= this.getX() + mainW &&
            event.y() >= this.getY() && event.y() <= this.getY() + this.height) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.dragging = true;
            updateRatioFromMouse(event.x());
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.dragging = false;
        if (this.textMode) {
            this.editBox.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.textMode) {
            return this.editBox.mouseDragged(event, deltaX, deltaY);
        }
        if (this.active && this.visible && this.dragging) {
            updateRatioFromMouse(event.x());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.textMode) {
            return this.editBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int mainW = getMainControlWidth();
        if (this.active && this.visible && mouseX >= this.getX() && mouseX <= this.getX() + mainW &&
            mouseY >= this.getY() && mouseY <= this.getY() + this.height) {
            double current = getRatio();
            double step = 0.05;
            if (intOption != null && intOption.getMax() > intOption.getMin()) {
                step = (double) intOption.getStep() / (intOption.getMax() - intOption.getMin());
            }
            setRatio(current + (scrollY > 0 ? step : -step));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.textMode) {
            return this.editBox.keyPressed(event);
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.textMode) {
            return this.editBox.charTyped(event);
        }
        return super.charTyped(event);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (this.textMode) {
            this.editBox.setFocused(focused);
        }
    }

    @Override
    public boolean isFocused() {
        if (this.textMode) {
            return this.editBox.isFocused() || super.isFocused();
        }
        return super.isFocused();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        int x = this.getX();
        int y = this.getY();
        int mainW = getMainControlWidth();
        int modeBtnX = getModeBtnX();
        boolean modeHovered = isOverModeButton(mouseX, mouseY);
        Font font = Minecraft.getInstance().font;

        // 1. Draw Main Control (Slider vs EditBox)
        if (this.textMode) {
            this.editBox.setX(x);
            this.editBox.setY(y);
            this.editBox.setWidth(mainW);
            this.editBox.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        } else {
            boolean sliderHovered = mouseX >= x && mouseX <= x + mainW && mouseY >= y && mouseY <= y + this.height;

            // Authentic dark recessed slider track (Image 4)
            extractor.fill(x, y, x + mainW, y + this.height, 0xFF141416);
            OTheme.drawBorder(extractor, x, y, mainW, this.height, sliderHovered ? 0xFF55555C : 0xFF36363B);

            // Draw sleek slider thumb / handle
            double ratio = Math.max(0.0, Math.min(1.0, getRatio()));
            int thumbWidth = 14;
            int thumbX = (int) Math.round(x + 2 + (mainW - 4 - thumbWidth) * ratio);
            int thumbY = y;
            int thumbH = this.height;

            int thumbColor = (sliderHovered || dragging) ? 0xFFC8C8C8 : 0xFFA0A0A0;
            int thumbBorder = (sliderHovered || dragging) ? 0xFFFFFFFF : 0xFFD8D8D8;
            extractor.fill(thumbX, thumbY, thumbX + thumbWidth, thumbY + thumbH, thumbColor);
            OTheme.drawBorder(extractor, thumbX, thumbY, thumbWidth, thumbH, thumbBorder);

            // Centered yellow text for slider value (Matching Image 4)
            int textColor = 0xFFFFFFAA;
            int textY = y + (this.height - font.lineHeight) / 2;

            Component label = this.getMessage();
            int maxW = mainW - 6;
            boolean trimmed = false;
            if (font.width(label) > maxW && maxW > 10) {
                String str = label.getString();
                while (!str.isEmpty() && font.width(str + "..") > maxW) {
                    str = str.substring(0, str.length() - 1);
                }
                label = Component.literal(str + "..");
                trimmed = true;
            }

            extractor.centeredText(font, label, x + mainW / 2, textY, textColor);

            if (sliderHovered && trimmed) {
                extractor.setComponentTooltipForNextFrame(font, java.util.List.of(this.getMessage()), mouseX, mouseY);
            }
        }

        // 2. Draw Mode Switch Button ([T] or [||])
        OTheme.drawDefaultButton(extractor, modeBtnX, y, MODE_BTN_WIDTH, this.height, modeHovered, this.active);
        OTheme.drawBorder(extractor, modeBtnX, y, MODE_BTN_WIDTH, this.height, modeHovered ? 0xFFFFFFFF : 0xFF555555);

        int iconY = y + (this.height - font.lineHeight) / 2;
        if (this.textMode) {
            // Slider icon: two small vertical bars "||"
            int iconColor = modeHovered ? 0xFFFFFFFF : 0xFFAAAAAA;
            extractor.centeredText(font, "||", modeBtnX + MODE_BTN_WIDTH / 2, iconY, iconColor);
        } else {
            // Text icon: "T"
            int iconColor = modeHovered ? 0xFFFFFFFF : 0xFFAAAAAA;
            extractor.centeredText(font, "T", modeBtnX + MODE_BTN_WIDTH / 2, iconY, iconColor);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        if (this.textMode) {
            this.editBox.updateWidgetNarration(narrationElementOutput);
        } else {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}
