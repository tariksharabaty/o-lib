package com.oidioomstudio.olib.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.oidioomstudio.olib.api.options.KeybindOption;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StoneKeybindWidget extends AbstractWidget {
    private static StoneKeybindWidget activeListening = null;

    private final KeybindOption option;
    private final Runnable onChange;
    private boolean listening = false;

    public StoneKeybindWidget(int x, int y, int width, int height, KeybindOption option, Runnable onChange) {
        super(x, y, width, height, Component.empty());
        this.option = option;
        this.onChange = onChange;
        this.updateMessage();
    }

    public static StoneKeybindWidget getActiveListening() {
        return activeListening;
    }

    public static void clearActiveListening() {
        if (activeListening != null) {
            activeListening.setListening(false);
            activeListening = null;
        }
    }

    public void updateMessage() {
        if (listening) {
            this.setMessage(Component.literal("> ")
                .append(Component.translatable("olib.config.press_key"))
                .append(Component.literal(" <")));
        } else {
            this.setMessage(option.getKeyDisplayName());
        }
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        if (this.listening == listening) return;
        this.listening = listening;
        if (listening) {
            if (activeListening != null && activeListening != this) {
                activeListening.setListening(false);
            }
            activeListening = this;
        } else {
            if (activeListening == this) {
                activeListening = null;
            }
        }
        this.updateMessage();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || !this.visible) {
            return false;
        }

        if (this.isHovered()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (!listening) {
                this.setListening(true);
            } else {
                InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(event.button());
                applyKey(mouseKey);
            }
            return true;
        }

        return false;
    }

    public boolean handleInputMouse(MouseButtonEvent event) {
        if (!listening) return false;
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(event.button());
        applyKey(mouseKey);
        return true;
    }

    public boolean handleInputKey(KeyEvent event) {
        if (!listening) return false;

        int keyCode = event.key();
        if (keyCode == InputConstants.KEY_ESCAPE) {
            // Esc cancels keybind selection without modifying previous key
            this.setListening(false);
            return true;
        } else if (keyCode == InputConstants.KEY_BACKSPACE || keyCode == InputConstants.KEY_DELETE) {
            // Backspace/Delete unbinds key to None
            applyKey(InputConstants.UNKNOWN);
            return true;
        } else {
            // Set newly pressed key
            InputConstants.Key key = InputConstants.getKey(event);
            applyKey(key);
            return true;
        }
    }

    private void applyKey(InputConstants.Key key) {
        option.setValue(key);
        this.setListening(false);
        if (onChange != null) {
            onChange.run();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening) {
            return handleInputKey(event);
        }
        return super.keyPressed(event);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered() || listening;
        OTheme.drawDefaultButton(extractor, this.getX(), this.getY(), this.width, this.height, hovered, this.active);

        Font font = Minecraft.getInstance().font;
        int textColor;
        if (listening) {
            textColor = OTheme.TEXT_HIGHLIGHT;
        } else if (hovered) {
            textColor = OTheme.TEXT_WHITE;
        } else {
            textColor = OTheme.TEXT_PRIMARY;
        }

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
