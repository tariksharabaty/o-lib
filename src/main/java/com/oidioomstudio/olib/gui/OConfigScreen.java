package com.oidioomstudio.olib.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.oidioomstudio.olib.api.OConfigCategory;
import com.oidioomstudio.olib.api.OConfigGroup;
import com.oidioomstudio.olib.api.OConfigRegistry;
import com.oidioomstudio.olib.api.OModConfig;
import com.oidioomstudio.olib.api.options.OOption;
import com.oidioomstudio.olib.client.OLibClientMod;
import com.oidioomstudio.olib.gui.theme.OTheme;
import com.oidioomstudio.olib.gui.widgets.StoneButtonWidget;
import com.oidioomstudio.olib.gui.widgets.StoneDropdownWidget;
import com.oidioomstudio.olib.gui.widgets.StoneTabButtonWidget;
import com.oidioomstudio.olib.gui.widgets.StoneLanguageDropdownWidget;
import com.oidioomstudio.olib.gui.widgets.StoneKeybindWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import com.oidioomstudio.olib.api.i18n.OLanguage;
import com.oidioomstudio.olib.api.i18n.OLanguageManager;

import java.util.ArrayList;
import java.util.List;

public class OConfigScreen extends Screen {
    private final Screen parent;
    private OModConfig currentMod;
    private int currentCategoryIndex = 0;
    private String searchQuery = "";
    private boolean searchExpanded = false;

    private OOptionList optionList;
    private StoneButtonWidget searchBtn;
    private EditBox searchInput;
    private StoneDropdownWidget dropdownWidget;
    private StoneLanguageDropdownWidget languageDropdown;
    private final List<StoneTabButtonWidget> tabButtons = new ArrayList<>();
    private final Runnable languageChangeListener = this::rebuildWidgets;

    // Cached layout positions
    private int listY;
    private int listHeight;
    private int popupX;
    private int popupY;
    private int popupWidth;
    private final int popupHeight = 22;

    public OConfigScreen(Screen parent) {
        this(parent, OConfigRegistry.getDefault());
    }

    public OConfigScreen(Screen parent, String modId) {
        this(parent, OConfigRegistry.get(modId) != null ? OConfigRegistry.get(modId) : OConfigRegistry.getDefault());
    }

    public OConfigScreen(Screen parent, OModConfig initialConfig) {
        super(Component.translatable("olib.config.title"));
        this.parent = parent;
        this.currentMod = initialConfig != null ? initialConfig : OConfigRegistry.getDefault();
    }

    public void setMod(OModConfig mod) {
        if (mod == null || mod == this.currentMod) return;
        if (this.currentMod != null) {
            this.currentMod.save();
        }
        this.currentMod = mod;
        this.currentCategoryIndex = 0;
        this.searchQuery = "";
        this.searchExpanded = false;
        this.rebuildWidgets();
    }

    @Override
    protected void init() {
        super.init();
        this.tabButtons.clear();

        if (this.currentMod == null) {
            this.currentMod = OConfigRegistry.getDefault();
        }

        int screenW = this.width;
        int screenH = this.height;

        // Top bar padding
        int topY = 8;
        int headerHeight = 22;

        // 1. Top-Right: Oidioom Studio Mod Switcher Dropdown & Language Switcher Dropdown
        // Dynamic sizing based on active text length
        int dropdownHeight = 20;

        Component modLabel = (this.currentMod != null)
            ? this.currentMod.getDisplayName()
            : Component.translatable("olib.config.dropdown.switch_mod");
        int defaultResetWidth = (screenW >= 400) ? 55 : 45;
        int dropdownWidth = defaultResetWidth * 2; // Sıfırla tuşunun iki katı (~110px)

        OLanguage curLang = OLanguageManager.getCurrentLanguage();
        Component langLabel = (curLang == OLanguage.AUTO)
            ? Component.translatable("olib.lang.auto_short")
            : Component.literal(curLang.getNativeName());
        int langWidth = Math.max(55, this.font.width(langLabel) + 24);

        // Keep dropdowns from consuming more than 60% of screen width on very small displays
        int maxAllowedDropdownsW = (screenW * 60) / 100;
        if (dropdownWidth + langWidth + 20 > maxAllowedDropdownsW) {
            dropdownWidth = (maxAllowedDropdownsW * 55) / 100;
            langWidth = (maxAllowedDropdownsW * 45) / 100;
        }

        int dropdownX = screenW - dropdownWidth - 14;
        int dropdownY = topY;

        this.dropdownWidget = new StoneDropdownWidget(dropdownX, dropdownY, dropdownWidth, dropdownHeight, this.currentMod, this::setMod);

        int langX = dropdownX - langWidth - 6;
        int langY = topY;

        this.languageDropdown = new StoneLanguageDropdownWidget(langX, langY, langWidth, dropdownHeight, this::rebuildWidgets);

        // 2. Horizontal Category Tabs (below title/header)
        // Dynamic sizing based on individual tab text length
        int tabsY = topY + headerHeight + 2;
        int tabHeight = 20;
        int tabSpacing = 4;

        List<OConfigCategory> categories = (this.currentMod != null) ? this.currentMod.getCategories() : List.of();
        int tabCount = categories.size();

        if (tabCount > 0) {
            int tabStartX = 14;
            int maxAllowedTabsWidth = screenW - 28;

            // Calculate natural text-based width for each tab
            int[] tabWidths = new int[tabCount];
            int totalTabsWidth = 0;
            for (int i = 0; i < tabCount; i++) {
                OConfigCategory cat = categories.get(i);
                int desiredW = Math.max(40, this.font.width(cat.getTitle()) + 18);
                tabWidths[i] = desiredW;
                totalTabsWidth += desiredW;
            }
            totalTabsWidth += (tabCount - 1) * tabSpacing;

            // If tabs exceed available screen width, scale them down proportionally
            if (totalTabsWidth > maxAllowedTabsWidth) {
                int availableForTabs = maxAllowedTabsWidth - ((tabCount - 1) * tabSpacing);
                int rawSum = totalTabsWidth - ((tabCount - 1) * tabSpacing);
                for (int i = 0; i < tabCount; i++) {
                    tabWidths[i] = Math.max(30, (int) ((long) tabWidths[i] * availableForTabs / rawSum));
                }
            }

            int currentTabX = tabStartX;
            for (int i = 0; i < tabCount; i++) {
                final int tabIdx = i;
                OConfigCategory cat = categories.get(i);
                int tw = tabWidths[i];
                boolean isSelected = (i == currentCategoryIndex && (searchQuery == null || searchQuery.isBlank()));

                StoneTabButtonWidget tabBtn = new StoneTabButtonWidget(
                    currentTabX, tabsY, tw, tabHeight,
                    cat.getTitle(),
                    i, isSelected,
                    idx -> {
                        currentCategoryIndex = idx;
                        searchQuery = "";
                        searchExpanded = false;
                        if (searchInput != null) {
                            searchInput.setValue("");
                            searchInput.visible = false;
                        }
                        if (searchBtn != null) {
                            searchBtn.setMessage(getSearchButtonLabel());
                        }
                        updateActiveTabs();
                        populateOptions();
                    }
                );
                this.tabButtons.add(tabBtn);
                this.addRenderableWidget(tabBtn);
                currentTabX += tw + tabSpacing;
            }
        }

        // 3. Scrollable Option List
        this.listY = tabsY + tabHeight + 6;
        int bottomBarHeight = 32;
        this.listHeight = Math.max(60, screenH - listY - bottomBarHeight - 6);

        this.optionList = new OOptionList(this.minecraft, screenW, listHeight, listY, 26);
        populateOptions();
        this.addRenderableWidget(this.optionList);

        // 4. Bottom Controls: [ Search ]  [ Reset All ]  [ Done ]
        int bottomY = screenH - 26;
        int spacing = 8;

        Component searchLabel = getSearchButtonLabel();
        Component resetLabel = Component.translatable("olib.config.reset_all");
        Component doneLabel = Component.translatable("olib.config.done");

        int searchBtnWidth = Math.max(65, this.font.width(searchLabel) + 16);
        int resetBtnWidth = Math.max(80, this.font.width(resetLabel) + 16);
        int doneBtnWidth = Math.max(65, this.font.width(doneLabel) + 20);

        int totalBottomWidth = searchBtnWidth + spacing + resetBtnWidth + spacing + doneBtnWidth;
        int maxBottomW = screenW - 20;

        if (totalBottomWidth > maxBottomW) {
            float scale = (float) (maxBottomW - (spacing * 2)) / (searchBtnWidth + resetBtnWidth + doneBtnWidth);
            searchBtnWidth = Math.max(50, (int) (searchBtnWidth * scale));
            resetBtnWidth = Math.max(60, (int) (resetBtnWidth * scale));
            doneBtnWidth = Math.max(50, (int) (doneBtnWidth * scale));
            totalBottomWidth = searchBtnWidth + spacing + resetBtnWidth + spacing + doneBtnWidth;
        }

        int startBottomX = (screenW - totalBottomWidth) / 2;

        int searchX = startBottomX;
        int resetX = searchX + searchBtnWidth + spacing;
        int doneX = resetX + resetBtnWidth + spacing;

        // Expanded Search Box bounds spanning across ALL THREE BUTTONS from searchX to (doneX + doneBtnWidth)
        this.popupX = searchX;
        this.popupWidth = (doneX + doneBtnWidth) - searchX;
        this.popupY = bottomY - popupHeight - 4;

        // Search Toggle Button
        this.searchBtn = new StoneButtonWidget(
            searchX, bottomY, searchBtnWidth, 20,
            searchLabel,
            this::toggleSearch
        );
        this.addRenderableWidget(this.searchBtn);

        // Reset All Button
        this.addRenderableWidget(new StoneButtonWidget(
            resetX, bottomY, resetBtnWidth, 20,
            resetLabel,
            () -> {
                if (currentMod != null) {
                    currentMod.resetAll();
                    if (optionList != null) {
                        optionList.refreshWidgets();
                    }
                }
            }
        ));

        // Done ("Bitti" / "Done") Button with auto-adjusted text width
        this.addRenderableWidget(new StoneButtonWidget(
            doneX, bottomY, doneBtnWidth, 20,
            doneLabel,
            this::onClose
        ));



        // 5. Active Search EditBox widget placed inside the extended popup area
        int inputX = popupX + 26;
        int inputY = popupY + (popupHeight - this.font.lineHeight) / 2;
        int inputW = popupWidth - 52;
        int inputH = this.font.lineHeight;

        this.searchInput = new EditBox(
            this.font,
            inputX,
            inputY,
            inputW,
            inputH,
            Component.empty()
        );
        this.searchInput.setBordered(false);
        this.searchInput.setTextColor(OTheme.TEXT_WHITE);
        this.searchInput.setTextColorUneditable(OTheme.TEXT_DISABLED);
        this.searchInput.setHint(Component.translatable("olib.config.search_placeholder"));
        this.searchInput.setValue(this.searchQuery);
        this.searchInput.visible = this.searchExpanded;
        this.searchInput.setResponder(query -> {
            this.searchQuery = query;
            if (this.searchBtn != null) {
                this.searchBtn.setMessage(getSearchButtonLabel());
            }
            updateActiveTabs();
            populateOptions();
        });
        // Register as widget child so events and narration are routed, but rendered custom in extractRenderState
        this.addWidget(this.searchInput);

        if (this.searchExpanded) {
            this.setFocused(this.searchInput);
            this.searchInput.setFocused(true);
        }
    }

    private Component getSearchButtonLabel() {
        if (searchQuery == null || searchQuery.isBlank()) {
            return Component.translatable("olib.config.search_btn");
        } else {
            String shortQ = searchQuery.length() > 8 ? searchQuery.substring(0, 8) + "..." : searchQuery;
            return Component.literal("\"" + shortQ + "\"");
        }
    }

    private void toggleSearch() {
        this.searchExpanded = !this.searchExpanded;
        if (this.searchInput != null) {
            this.searchInput.visible = this.searchExpanded;
            if (this.searchExpanded) {
                this.setFocused(this.searchInput);
                this.searchInput.setFocused(true);
            } else {
                if (this.getFocused() == this.searchInput) {
                    this.setFocused(null);
                }
                this.searchInput.setFocused(false);
            }
        }
    }

    private void updateActiveTabs() {
        boolean isSearching = searchQuery != null && !searchQuery.isBlank();
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setSelected(!isSearching && i == currentCategoryIndex);
        }
    }

    private void populateOptions() {
        if (this.optionList == null || this.currentMod == null) return;
        this.optionList.clearOptions();
        this.optionList.setScrollAmount(0.0);

        Runnable onModified = () -> {
            if (this.currentMod != null) {
                this.currentMod.save();
            }
        };

        if (searchQuery != null && !searchQuery.isBlank()) {
            for (OOption<?> opt : this.currentMod.getAllOptions()) {
                if (opt.matchesSearch(searchQuery)) {
                    this.optionList.addOption(opt, onModified);
                }
            }
        } else {
            List<OConfigCategory> categories = this.currentMod.getCategories();
            if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
                OConfigCategory cat = categories.get(currentCategoryIndex);

                // 1. Add standalone options
                for (OOption<?> opt : cat.getStandaloneOptions()) {
                    this.optionList.addOption(opt, onModified);
                }

                // 2. Add sub-category groups with stylish headers
                for (OConfigGroup group : cat.getGroups()) {
                    if (!group.getOptions().isEmpty()) {
                        this.optionList.addGroupHeader(group);
                        for (OOption<?> opt : group.getOptions()) {
                            this.optionList.addOption(opt, onModified);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        StoneKeybindWidget activeKeybind = StoneKeybindWidget.getActiveListening();
        if (activeKeybind != null) {
            if (activeKeybind.handleInputMouse(event)) {
                return true;
            }
        }

        double mx = event.x();
        double my = event.y();

        // 1. If search popup is expanded
        if (searchExpanded && searchInput != null) {
            int closeX = popupX + popupWidth - 18;

            // Check if clicked close / clear button (X)
            if (mx >= closeX - 6 && mx <= popupX + popupWidth && my >= popupY && my <= popupY + popupHeight) {
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    this.searchInput.setValue("");
                    this.searchQuery = "";
                    this.setFocused(this.searchInput);
                    this.searchInput.setFocused(true);
                    populateOptions();
                } else {
                    toggleSearch();
                }
                return true;
            }

            // Check if clicked inside search box popup area
            if (mx >= popupX && mx <= popupX + popupWidth && my >= popupY && my <= popupY + popupHeight) {
                this.setFocused(this.searchInput);
                this.searchInput.setFocused(true);
                double innerX = Math.max(searchInput.getX(), Math.min(searchInput.getX() + searchInput.getWidth() - 2, mx));
                double innerY = searchInput.getY() + (searchInput.getHeight() / 2.0);
                MouseButtonEvent innerEvent = new MouseButtonEvent(innerX, innerY, event.buttonInfo());
                this.searchInput.mouseClicked(innerEvent, doubleClick);
                return true;
            }
        }

        // 2. Clicked search toggle button: directly toggle and maintain focus
        if (this.searchBtn != null && this.searchBtn.isMouseOver(mx, my)) {
            toggleSearch();
            return true;
        }

        // 3. Language switcher dropdown
        if (languageDropdown != null && languageDropdown.isOpen()) {
            if (languageDropdown.handleClick(event)) {
                return true;
            }
        }
        if (languageDropdown != null && languageDropdown.isHovered()) {
            if (dropdownWidget != null && dropdownWidget.isOpen()) {
                dropdownWidget.setOpen(false);
            }
            if (languageDropdown.handleClick(event)) {
                return true;
            }
        }

        // 4. Mod switcher dropdown
        if (dropdownWidget != null && dropdownWidget.isOpen()) {
            if (dropdownWidget.handleClick(event)) {
                return true;
            }
        }
        if (dropdownWidget != null && dropdownWidget.isHovered()) {
            if (languageDropdown != null && languageDropdown.isOpen()) {
                languageDropdown.setOpen(false);
            }
            if (dropdownWidget.handleClick(event)) {
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        StoneKeybindWidget activeKeybind = StoneKeybindWidget.getActiveListening();
        if (activeKeybind != null) {
            if (activeKeybind.handleInputKey(event)) {
                return true;
            }
        }

        if (searchExpanded && searchInput != null) {
            if (event.key() == InputConstants.KEY_ESCAPE) {
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    this.searchInput.setValue("");
                    this.searchQuery = "";
                    populateOptions();
                } else {
                    toggleSearch();
                }
                return true;
            }
            if (this.searchInput.isFocused() && this.searchInput.keyPressed(event)) {
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchExpanded && searchInput != null) {
            if (!this.searchInput.isFocused()) {
                this.setFocused(this.searchInput);
                this.searchInput.setFocused(true);
            }
            if (this.searchInput.charTyped(event)) {
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        boolean inWorld = this.minecraft != null && this.minecraft.level != null;
        if (!inWorld) {
            super.extractBackground(extractor, mouseX, mouseY, partialTicks);
        } else {
            this.extractTransparentBackground(extractor);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        // 1. Adjustable translucent dark overlay (0% = 100% completely transparent to the game world)
        int opacityPercent = Math.max(0, Math.min(100, OLibClientMod.backgroundOpacityPercent));
        if (opacityPercent > 0) {
            int alpha = (int) Math.round((opacityPercent / 100.0) * 255.0);
            int overlayColor = (alpha << 24) | 0x000000;
            extractor.fill(0, 0, this.width, this.height, overlayColor);
        }

        Font font = this.font;

        // 3. Top-Left: Clean Title - Dynamically fitted to avoid colliding with dropdowns
        int maxTitleWidth = (this.languageDropdown != null ? this.languageDropdown.getX() : (this.width - 200)) - 22;
        String fullTitle = (this.currentMod != null)
            ? this.currentMod.getDisplayName().getString()
            : "O Lib";

        String displayTitle = fullTitle;
        if (font.width(displayTitle) > maxTitleWidth && maxTitleWidth > 30) {
            String modName = fullTitle;
            while (!modName.isEmpty() && font.width(modName + "...") > maxTitleWidth) {
                modName = modName.substring(0, modName.length() - 1);
            }
            displayTitle = modName.isEmpty() ? "" : (modName + "...");
        }

        if (!displayTitle.isEmpty()) {
            extractor.text(font, Component.literal(displayTitle), 14, 14, OTheme.TEXT_PRIMARY);
        }

        if (displayTitle.length() < fullTitle.length() && mouseX >= 10 && mouseX <= 14 + font.width(displayTitle) && mouseY >= 8 && mouseY <= 26) {
            extractor.setComponentTooltipForNextFrame(font, List.of(Component.literal(fullTitle)), mouseX, mouseY);
        }

        // 4. Render base screen widgets (tabs, option list, bottom buttons)
        super.extractRenderState(extractor, mouseX, mouseY, partialTicks);

        // Friendly "No results found" label if searching yields no options
        if (searchQuery != null && !searchQuery.isBlank() && optionList != null && optionList.children().isEmpty()) {
            int emptyY = listY + listHeight / 2 - 4;
            extractor.centeredText(font, Component.translatable("olib.config.no_results"), this.width / 2, emptyY, OTheme.TEXT_MUTED);
        }

        // 5. If search is expanded: Render popup frame spanning across all 3 bottom buttons
        if (this.searchExpanded && this.searchInput != null) {
            extractor.fill(popupX, popupY, popupX + popupWidth, popupY + popupHeight, 0xF5141418);
            OTheme.drawBorder(extractor, popupX, popupY, popupWidth, popupHeight, this.searchInput.isFocused() ? 0xFFFFFFFF : 0xFF555562);

            // Draw crisp 7x7 pixel-art magnifying glass icon on the left
            int iconY = popupY + (popupHeight - 7) / 2;
            OTheme.drawSearchIcon(extractor, popupX + 9, iconY, OTheme.TEXT_WHITE);

            // Render search input on top of popup container
            this.searchInput.extractRenderState(extractor, mouseX, mouseY, partialTicks);

            // Close / Clear button (X) on right
            int closeX = popupX + popupWidth - 16;
            boolean closeHovered = mouseX >= closeX - 6 && mouseX <= popupX + popupWidth && mouseY >= popupY && mouseY <= popupY + popupHeight;
            int closeY = popupY + (popupHeight - font.lineHeight) / 2;
            extractor.text(font, Component.literal("X"), closeX, closeY, closeHovered ? 0xFFFF5555 : OTheme.TEXT_MUTED);
        }

        // 6. Language dropdown widget & overlay
        if (this.languageDropdown != null) {
            this.languageDropdown.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        }

        // 7. Floating Mod Switcher dropdown widget & overlay
        if (this.dropdownWidget != null) {
            this.dropdownWidget.extractRenderState(extractor, mouseX, mouseY, partialTicks);
        }

        // Render popup overlays on top of everything
        if (this.languageDropdown != null) {
            this.languageDropdown.renderDropdownOverlay(extractor, mouseX, mouseY);
        }
        if (this.dropdownWidget != null) {
            this.dropdownWidget.renderDropdownOverlay(extractor, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        StoneKeybindWidget.clearActiveListening();
        if (this.currentMod != null) {
            this.currentMod.save();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
    @Override
    public void rebuildWidgets() {
        super.rebuildWidgets();
    }
}
