package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.api.i18n.OLanguage;
import com.oidioomstudio.olib.api.i18n.OLanguageManager;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StoneLanguageDropdownWidget extends AbstractWidget {
    private final Runnable onLanguageSelected;
    private boolean open = false;
    private final int itemHeight = 18;

    public StoneLanguageDropdownWidget(int x, int y, int width, int height, Runnable onLanguageSelected) {
        super(x, y, width, height, Component.empty());
        this.onLanguageSelected = onLanguageSelected;
        this.updateMessage();
    }

    public void updateMessage() {
        OLanguage current = OLanguageManager.getCurrentLanguage();
        if (current == OLanguage.AUTO) {
            this.setMessage(Component.translatable("olib.lang.auto_short"));
        } else {
            this.setMessage(Component.literal(current.getNativeName()));
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean handleClick(MouseButtonEvent event) {
        if (!this.visible) return false;

        OLanguage[] languages = OLanguage.values();
        int menuWidth = Math.max(this.width, 130);
        int menuX = this.getX() + this.width - menuWidth;
        if (menuX < 4) menuX = 4;
        int totalHeight = languages.length * itemHeight;
        int menuY = this.getY() + this.height + 2;

        if (open) {
            double mx = event.x();
            double my = event.y();

            if (mx >= menuX && mx <= menuX + menuWidth &&
                my >= menuY && my <= menuY + totalHeight) {

                int index = (int) ((my - menuY) / itemHeight);
                if (index >= 0 && index < languages.length) {
                    OLanguage selected = languages[index];
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    com.oidioomstudio.olib.client.OLibClientMod.setLanguage(selected);
                    this.updateMessage();
                    this.open = false;
                    if (onLanguageSelected != null) {
                        onLanguageSelected.run();
                    }
                    return true;
                }
            }

            this.open = false;
            return true;
        } else if (this.isHovered()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.open = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return handleClick(event);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered() || open;
        OTheme.drawDefaultButton(extractor, this.getX(), this.getY(), this.width, this.height, hovered, this.active);

        Font font = Minecraft.getInstance().font;
        int textColor = hovered ? OTheme.TEXT_WHITE : OTheme.TEXT_PRIMARY;
        int textY = this.getY() + (this.height - 8) / 2;

        Component label = this.getMessage();
        int maxW = this.width - 22;
        if (font.width(label) > maxW && maxW > 20) {
            String str = label.getString();
            while (!str.isEmpty() && font.width(str + "..") > maxW) {
                str = str.substring(0, str.length() - 1);
            }
            label = Component.literal(str + "..");
        }

        extractor.text(font, label, this.getX() + 6, textY, textColor);

        String arrow = open ? "▲" : "▼";
        extractor.text(font, Component.literal(arrow), this.getX() + this.width - 12, textY, hovered ? 0xFFFFFFFF : 0xFFA0A0A0);
    }

    public void renderDropdownOverlay(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        if (!open || !visible) return;

        OLanguage[] languages = OLanguage.values();
        int menuWidth = Math.max(this.width, 130);
        int menuX = this.getX() + this.width - menuWidth;
        if (menuX < 4) menuX = 4;
        int menuY = this.getY() + this.height + 2;
        int totalHeight = languages.length * itemHeight;

        extractor.fill(menuX, menuY, menuX + menuWidth, menuY + totalHeight, 0xF518181C);
        OTheme.drawBorder(extractor, menuX, menuY, menuWidth, totalHeight, 0xFF4A4A54);

        Font font = Minecraft.getInstance().font;
        OLanguage current = OLanguageManager.getCurrentLanguage();

        for (int i = 0; i < languages.length; i++) {
            OLanguage lang = languages[i];
            int itemY = menuY + (i * itemHeight);
            boolean itemHovered = mouseX >= menuX && mouseX <= menuX + menuWidth && mouseY >= itemY && mouseY < itemY + itemHeight;
            boolean isCurrent = (current == lang);

            if (itemHovered) {
                OTheme.drawDefaultButton(extractor, menuX + 1, itemY, menuWidth - 2, itemHeight, true, true);
            } else if (isCurrent) {
                extractor.fill(menuX + 1, itemY, menuX + menuWidth - 1, itemY + itemHeight, 0xFF353540);
            }

            int textColor = itemHovered ? OTheme.TEXT_WHITE : (isCurrent ? OTheme.TEXT_HIGHLIGHT : OTheme.TEXT_PRIMARY);
            int ty = itemY + (itemHeight - 8) / 2;
            Component label = (lang == OLanguage.AUTO)
                ? Component.translatable("olib.lang.auto")
                : Component.literal(lang.getNativeName());

            int maxItemW = menuWidth - 12;
            if (font.width(label) > maxItemW && maxItemW > 20) {
                String str = label.getString();
                while (!str.isEmpty() && font.width(str + "..") > maxItemW) {
                    str = str.substring(0, str.length() - 1);
                }
                label = Component.literal(str + "..");
            }

            extractor.text(font, label, menuX + 6, ty, textColor);

            if (i < languages.length - 1 && !itemHovered) {
                extractor.horizontalLine(menuX + 4, menuX + menuWidth - 4, itemY + itemHeight - 1, 0xFF2A2A30);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
