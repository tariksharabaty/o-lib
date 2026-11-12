package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.api.options.OOption;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StoneResetButtonWidget extends AbstractWidget {
    private final OOption<?> option;
    private final Runnable onReset;

    public StoneResetButtonWidget(int x, int y, OOption<?> option, Runnable onReset) {
        super(x, y, OTheme.RESET_WIDTH, OTheme.RESET_HEIGHT, Component.translatable("olib.config.reset"));
        this.option = option;
        this.onReset = onReset;
    }

    public boolean canReset() {
        return option != null && option.isChanged();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.visible && canReset() && this.isHovered()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (option != null) {
                option.reset();
            }
            if (onReset != null) {
                onReset.run();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        boolean enabled = canReset();
        boolean hovered = this.isHovered() && enabled;

        OTheme.drawDefaultButton(extractor, this.getX(), this.getY(), this.width, this.height, hovered, enabled);

        Font font = Minecraft.getInstance().font;
        int textColor = enabled ? (hovered ? OTheme.TEXT_WHITE : OTheme.TEXT_PRIMARY) : OTheme.TEXT_DISABLED;
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

        if (this.isHovered() && trimmed) {
            extractor.setComponentTooltipForNextFrame(font, java.util.List.of(this.getMessage()), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
