package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class StoneTabButtonWidget extends AbstractWidget {
    private final int index;
    private final Consumer<Integer> onSelect;
    private boolean selected;

    public StoneTabButtonWidget(int x, int y, int width, int height, Component message,
                                int index, boolean selected, Consumer<Integer> onSelect) {
        super(x, y, width, height, message);
        this.index = index;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.active && this.visible && this.isHovered()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (this.onSelect != null) {
                this.onSelect.accept(this.index);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        boolean hovered = this.isHovered();

        OTheme.drawTabButton(extractor, this.getX(), this.getY(), this.width, this.height, this.selected, hovered);

        int textColor = this.selected ? OTheme.TEXT_WHITE : (hovered ? OTheme.TEXT_WHITE : OTheme.TEXT_PRIMARY);
        int textY = this.getY() + (this.height - 8) / 2;

        Component label = this.getMessage();
        int maxW = this.width - 8;
        boolean trimmed = false;
        if (font.width(label) > maxW && maxW > 16) {
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
