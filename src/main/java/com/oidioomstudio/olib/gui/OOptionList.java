package com.oidioomstudio.olib.gui;

import com.oidioomstudio.olib.api.OConfigGroup;
import com.oidioomstudio.olib.api.options.OOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.ArrayList;
import java.util.List;

public class OOptionList extends ContainerObjectSelectionList<OBaseListEntry> {
    private final List<OOptionEntry> optionEntries = new ArrayList<>();

    public OOptionList(Minecraft mc, int width, int height, int y, int defaultEntryHeight) {
        super(mc, width, height, y, defaultEntryHeight);
    }

    public void addOption(OOption<?> option, Runnable onModified) {
        OOptionEntry entry = new OOptionEntry(option, onModified);
        this.optionEntries.add(entry);
        this.addEntry(entry);
    }

    public void addGroupHeader(OConfigGroup group) {
        OGroupHeaderEntry entry = new OGroupHeaderEntry(group);
        this.addEntry(entry);
    }

    public void clearOptions() {
        com.oidioomstudio.olib.gui.widgets.StoneKeybindWidget.clearActiveListening();
        this.optionEntries.clear();
        this.clearEntries();
    }

    public void refreshWidgets() {
        for (OOptionEntry entry : this.optionEntries) {
            entry.updateWidgetState();
        }
    }

    @Override
    public int getRowWidth() {
        return Math.min(520, Math.max(300, this.width - 40));
    }

    @Override
    public int scrollbarWidth() {
        return 6;
    }

    @Override
    protected int scrollBarX() {
        return this.getRowRight() + 3;
    }

    @Override
    protected void extractScrollbar(net.minecraft.client.gui.GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        if (!this.scrollable()) {
            return;
        }

        int x = this.scrollBarX();
        int y = this.getY();
        int w = this.scrollbarWidth();
        int h = this.getHeight();
        int thumbY = this.scrollBarY();
        int thumbH = this.scrollerHeight();

        // Dark track background (Masa/Litematica style from Image 1)
        extractor.fill(x - 1, y, x + w + 1, y + h, 0xFF000000);
        com.oidioomstudio.olib.gui.theme.OTheme.drawBorder(extractor, x - 1, y, w + 2, h, 0xFF303034);

        // Clean light-grey thumb
        boolean hovered = this.isOverScrollbar(mouseX, mouseY);
        int thumbColor = hovered ? 0xFFD0D0D0 : 0xFFA4A4A4;
        int borderColor = hovered ? 0xFFFFFFFF : 0xFFDDDDDD;

        extractor.fill(x, thumbY, x + w, thumbY + thumbH, thumbColor);
        com.oidioomstudio.olib.gui.theme.OTheme.drawBorder(extractor, x, thumbY, w, thumbH, borderColor);
    }

    @Override
    protected void extractListBackground(net.minecraft.client.gui.GuiGraphicsExtractor extractor) {
        // Do not draw opaque textured dirt/stone tiles behind the list items
    }

    @Override
    protected void extractListSeparators(net.minecraft.client.gui.GuiGraphicsExtractor extractor) {
        // Do not draw opaque separators to preserve full background transparency
    }
}
