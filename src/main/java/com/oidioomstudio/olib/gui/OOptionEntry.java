package com.oidioomstudio.olib.gui;

import com.oidioomstudio.olib.api.options.*;
import com.oidioomstudio.olib.gui.theme.OTheme;
import com.oidioomstudio.olib.gui.widgets.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OOptionEntry extends OBaseListEntry {
    private final OOption<?> option;
    private final AbstractWidget controlWidget;
    private final StoneResetButtonWidget resetWidget;
    private final List<AbstractWidget> widgets = new ArrayList<>();

    public OOptionEntry(OOption<?> option, Runnable onConfigModified) {
        this.option = option;
        this.controlWidget = createControlWidget(option, onConfigModified);
        this.resetWidget = new StoneResetButtonWidget(0, 0, option, () -> {
            updateWidgetState();
            if (onConfigModified != null) {
                onConfigModified.run();
            }
        });

        this.widgets.add(this.controlWidget);
        this.widgets.add(this.resetWidget);
    }

    private AbstractWidget createControlWidget(OOption<?> opt, Runnable onModified) {
        int w = OTheme.WIDGET_WIDTH;
        int h = 20;

        if (opt instanceof BooleanOption boolOpt) {
            return new StoneToggleWidget(0, 0, w, h, boolOpt, onModified);
        } else if (opt instanceof KeybindOption keyOpt) {
            return new StoneKeybindWidget(0, 0, w, h, keyOpt, onModified);
        } else if (opt instanceof IntegerSliderOption intOpt) {
            return new StoneSliderWidget(0, 0, w, h, intOpt, onModified);
        } else if (opt instanceof FloatSliderOption floatOpt) {
            return new StoneSliderWidget(0, 0, w, h, floatOpt, onModified);
        } else if (opt instanceof EnumOption<?> enumOpt) {
            return new StoneEnumWidget(0, 0, w, h, enumOpt, onModified);
        } else if (opt instanceof ColorOption colorOpt) {
            return new StoneColorWidget(0, 0, w, h, colorOpt, onModified);
        } else {
            return new StoneToggleWidget(0, 0, w, h, new BooleanOption(opt.getId(), opt.getName(), opt.getDescription(), false, () -> false, v -> {}, null, null), onModified);
        }
    }

    public void updateWidgetState() {
        if (controlWidget instanceof StoneToggleWidget toggle) {
            toggle.updateMessage();
        } else if (controlWidget instanceof StoneSliderWidget slider) {
            slider.updateMessage();
        } else if (controlWidget instanceof StoneEnumWidget enumWidget) {
            enumWidget.updateMessage();
        } else if (controlWidget instanceof StoneColorWidget colorWidget) {
            colorWidget.updateMessage();
        } else if (controlWidget instanceof StoneKeybindWidget keybind) {
            keybind.setListening(false);
        }
    }

    public OOption<?> getOption() {
        return option;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return widgets;
    }

    @Override
    public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY, boolean hovering, float partialTicks) {
        int x = this.getContentX();
        int y = this.getContentY();
        int right = this.getContentRight();
        int height = this.getContentHeight();
        int totalWidth = right - x;

        Font font = Minecraft.getInstance().font;

        // 1. Reset Button width & position (Clean gap from scrollbar)
        boolean showReset = com.oidioomstudio.olib.client.OLibClientMod.showResetButtons;
        Component resetMsg = resetWidget.getMessage();
        int idealResetWidth = font.width(resetMsg) + 12;
        int baseMinResetWidth = (totalWidth >= 400) ? 56 : 48;
        int resetWidth = Math.min(80, Math.max(baseMinResetWidth, idealResetWidth));
        int gapFromScrollbar = 10; // Space between Reset button and the scrollbar
        int resetX = right - gapFromScrollbar - resetWidth;
        int resetY = y + (height - OTheme.RESET_HEIGHT) / 2;

        if (showReset) {
            resetWidget.visible = true;
            resetWidget.setX(resetX);
            resetWidget.setY(resetY);
            resetWidget.setWidth(resetWidth);
            resetWidget.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        } else {
            resetWidget.visible = false;
        }

        // 2. Control Widget width & position
        int controlRight = showReset ? resetX - 6 : right - gapFromScrollbar;

        // Kontrol bilesenlerinin genisligi (Acik/Kapali, Renk, Slider ve yanindaki mod degistirme tusu dahil)
        // Dillerdeki uzun kelimelerin tasmasini onlemek icin dinamik ve saglikli genislik (~115px) veriyoruz
        int widgetWidth = (totalWidth >= 400) ? 120 : 105;
        widgetWidth = Math.min(widgetWidth, totalWidth * 38 / 100);

        int widgetX = controlRight - widgetWidth;
        int widgetY = y + (height - 20) / 2;

        controlWidget.setX(widgetX);
        controlWidget.setY(widgetY);
        controlWidget.setWidth(widgetWidth);
        controlWidget.extractRenderState(extractor, mouseX, mouseY, partialTicks);

        // 3. Icon & Left Label
        boolean optionHasIcon = option.hasIcon();
        int textStartX = optionHasIcon ? x + 24 : x + 4;
        int maxLabelWidth = Math.max(30, widgetX - textStartX - 6);

        boolean isRowHovered = mouseX >= x && mouseX <= right && mouseY >= y && mouseY <= y + height;
        if (isRowHovered) {
            extractor.fill(x - 2, y, right + 2, y + height, 0x14FFFFFF);
        }

        if (optionHasIcon) {
            int slotX = x + 3;
            int slotY = y + (height - 18) / 2;
            int borderColor = isRowHovered ? 0xFFFFFFFF : 0xFF3A3A3E;
            extractor.fill(slotX, slotY, slotX + 18, slotY + 18, 0x8818181A);
            OTheme.drawBorder(extractor, slotX, slotY, 18, 18, borderColor);

            ItemStack icon = ItemStack.EMPTY;
            try {
                icon = option.getIcon();
            } catch (Throwable ignored) {}

            if (icon != null && !icon.isEmpty()) {
                try {
                    extractor.fakeItem(icon, slotX + 1, slotY + 1);
                } catch (Throwable ignored) {}
            }
        }

        Component originalName = option.getName();
        Component displayName = originalName;

        if (font.width(originalName) > maxLabelWidth && maxLabelWidth > 20) {
            String fullStr = originalName.getString();
            int dotsW = font.width("...");
            if (maxLabelWidth > dotsW + 10) {
                while (!fullStr.isEmpty() && font.width(fullStr) + dotsW > maxLabelWidth) {
                    fullStr = fullStr.substring(0, fullStr.length() - 1);
                }
                displayName = Component.literal(fullStr + "...");
            }
        }

        // Tooltip rendering when option is hovered (clean wrapped description)
        if (isRowHovered) {
            Component desc = option.getDescription();
            if (desc != null && !desc.getString().isBlank()) {
                int maxWrapWidth = 280;
                List<net.minecraft.network.chat.FormattedText> rawWrapped =
                    font.getSplitter().splitLines(desc, maxWrapWidth, net.minecraft.network.chat.Style.EMPTY);
                List<Component> tooltipLines = new ArrayList<>();

                boolean inNote = false;
                for (net.minecraft.network.chat.FormattedText line : rawWrapped) {
                    String str = line.getString();
                    if (str.startsWith("NOT:") || str.startsWith("UYARI:") || str.startsWith("NOTE:") || str.startsWith("WARNING:")) {
                        inNote = true;
                    }
                    if (inNote) {
                        tooltipLines.add(Component.literal(str).withStyle(ChatFormatting.GOLD));
                    } else {
                        tooltipLines.add(Component.literal(str).withStyle(ChatFormatting.WHITE));
                    }
                }

                if (!tooltipLines.isEmpty()) {
                    extractor.setComponentTooltipForNextFrame(font, tooltipLines, mouseX, mouseY);
                }
            }
        }

        int textY = y + (height - font.lineHeight) / 2;
        int labelColor = isRowHovered ? OTheme.TEXT_WHITE : OTheme.TEXT_PRIMARY;
        extractor.text(font, displayName, textStartX, textY, labelColor);
    }
}
