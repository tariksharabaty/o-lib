package com.oidioomstudio.olib.gui.widgets;

import com.oidioomstudio.olib.api.OConfigRegistry;
import com.oidioomstudio.olib.api.OModConfig;
import com.oidioomstudio.olib.gui.theme.OTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StoneDropdownWidget extends AbstractWidget {
    private final Consumer<OModConfig> onSelectMod;
    private OModConfig currentMod;
    private boolean open = false;
    private final int itemHeight = 20;

    public StoneDropdownWidget(int x, int y, int width, int height, OModConfig currentMod, Consumer<OModConfig> onSelectMod) {
        super(x, y, width, height, Component.empty());
        this.currentMod = currentMod;
        this.onSelectMod = onSelectMod;
        this.updateMessage();
    }

    public void setCurrentMod(OModConfig mod) {
        this.currentMod = mod;
        this.updateMessage();
    }

    private void updateMessage() {
        if (currentMod != null) {
            this.setMessage(currentMod.getDisplayName());
        } else {
            this.setMessage(Component.translatable("olib.config.dropdown.switch_mod"));
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    private List<OModConfig> getRegisteredMods() {
        return new ArrayList<>(OConfigRegistry.getAll());
    }

    private int getMenuWidth() {
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        return Math.min(Math.max(this.width, 140), Math.max(140, screenW - 16));
    }

    private int getMenuX() {
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int menuW = getMenuWidth();
        int x = this.getX() + this.width - menuW;
        if (x + menuW > screenW - 6) {
            x = screenW - menuW - 6;
        }
        if (x < 6) {
            x = 6;
        }
        return x;
    }

    public boolean handleClick(MouseButtonEvent event) {
        if (!this.visible) return false;

        List<OModConfig> mods = getRegisteredMods();
        int menuWidth = getMenuWidth();
        int menuX = getMenuX();
        int totalHeight = mods.size() * itemHeight;
        int menuY = this.getY() + this.height + 2;

        if (open) {
            double mx = event.x();
            double my = event.y();

            // Check if clicked inside dropdown menu
            if (mx >= menuX && mx <= menuX + menuWidth &&
                my >= menuY && my <= menuY + totalHeight) {

                int index = (int) ((my - menuY) / itemHeight);
                if (index >= 0 && index < mods.size()) {
                    OModConfig selected = mods.get(index);
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.currentMod = selected;
                    this.updateMessage();
                    this.open = false;
                    if (onSelectMod != null) {
                        onSelectMod.accept(selected);
                    }
                    return true;
                }
            }

            // Clicked outside while open -> close
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
        int maxW = this.width - 24;
        if (font.width(label) > maxW && maxW > 20) {
            String str = label.getString();
            while (!str.isEmpty() && font.width(str + "..") > maxW) {
                str = str.substring(0, str.length() - 1);
            }
            label = Component.literal(str + "..");
        }

        // Label on the left
        extractor.text(font, label, this.getX() + 8, textY, textColor);

        // Down arrow on the right
        String arrow = open ? "▲" : "▼";
        extractor.text(font, Component.literal(arrow), this.getX() + this.width - 14, textY, hovered ? 0xFFFFFFFF : 0xFFA0A0A0);
    }

    /**
     * Renders the floating popup menu on top of other screen components.
     */
    public void renderDropdownOverlay(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        if (!open || !visible) return;

        List<OModConfig> mods = getRegisteredMods();
        if (mods.isEmpty()) return;

        int menuWidth = getMenuWidth();
        int menuX = getMenuX();
        int menuY = this.getY() + this.height + 2;
        int totalHeight = mods.size() * itemHeight;

        // Menu background box
        extractor.fill(menuX, menuY, menuX + menuWidth, menuY + totalHeight, 0xF518181C);
        OTheme.drawBorder(extractor, menuX, menuY, menuWidth, totalHeight, 0xFF4A4A54);

        Font font = Minecraft.getInstance().font;

        for (int i = 0; i < mods.size(); i++) {
            OModConfig mod = mods.get(i);
            int itemY = menuY + (i * itemHeight);
            boolean itemHovered = mouseX >= menuX && mouseX <= menuX + menuWidth && mouseY >= itemY && mouseY < itemY + itemHeight;
            boolean isCurrent = currentMod != null && currentMod.getModId().equalsIgnoreCase(mod.getModId());

            if (itemHovered) {
                OTheme.drawDefaultButton(extractor, menuX + 1, itemY, menuWidth - 2, itemHeight, true, true);
            } else if (isCurrent) {
                extractor.fill(menuX + 1, itemY, menuX + menuWidth - 1, itemY + itemHeight, 0xFF353540);
            }

            int textColor = itemHovered ? OTheme.TEXT_WHITE : (isCurrent ? OTheme.TEXT_HIGHLIGHT : OTheme.TEXT_PRIMARY);
            String title = mod.getDisplayName().getString();
            int maxTitleW = menuWidth - 16;
            if (font.width(title) > maxTitleW && maxTitleW > 20) {
                while (!title.isEmpty() && font.width(title + "..") > maxTitleW) {
                    title = title.substring(0, title.length() - 1);
                }
                title = title + "..";
            }
            int ty = itemY + (itemHeight - 8) / 2;
            extractor.text(font, Component.literal(title), menuX + 8, ty, textColor);

            // Separator line between items
            if (i < mods.size() - 1 && !itemHovered) {
                extractor.horizontalLine(menuX + 4, menuX + menuWidth - 4, itemY + itemHeight - 1, 0xFF2A2A30);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
