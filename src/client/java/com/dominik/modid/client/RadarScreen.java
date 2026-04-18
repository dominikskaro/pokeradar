package com.dominik.modid.client;

import com.dominik.modid.RadarFilter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RadarScreen extends Screen {

    private static final int PANEL_WIDTH = 200;
    private static final int TOP_MARGIN = 16;
    private static final int BOTTOM_MARGIN = 16;
    private static final int SCROLL_STEP = 20;
    private static final int LEGEND_SIZE = 8;
    private static final int SEARCH_ROW_OFFSET = 12;
    private static final int SEARCH_BOX_OFFSET = 22;
    private static final int SEARCH_BOX_GAP = 6;

    private EditBox textBox;
    private Checkbox checkLegendary;
    private Checkbox checkShiny;
    private Checkbox checkDitto;
    private Checkbox checkSearch;
    private Checkbox checkHitbox;
    private Button genderButton;
    private Checkbox checkUncaught;
    private Checkbox checkHiddenAbility;
    private EditBox natureBox;
    private Checkbox checkNatureSearch;
    private Button applyButton;
    private int scrollOffset;
    private int contentHeight;


    public RadarScreen() {
        super(Component.literal("Radar Filter"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int left = centerX - PANEL_WIDTH / 2;

        textBox = new EditBox(
                this.font,
                getSearchInputX(centerX),
                0,
                getSearchInputWidth(centerX),
                20,
                Component.literal("Pokemon name")
        );
        textBox.setMaxLength(256);
        textBox.setValue(String.join(", ", RadarFilter.FILTERS));
        this.addRenderableWidget(textBox);

        checkSearch = Checkbox.builder(Component.empty(), this.font)
                .pos(getSearchCheckboxX(centerX), 0)
                .selected(RadarFilter.SHOW_SEARCH)
                .build();
        this.addRenderableWidget(checkSearch);

        this.setFocused(textBox);
        textBox.setFocused(true);

        if (showNatureOption()) {
            natureBox = new EditBox(
                    this.font,
                    getSearchInputX(centerX),
                    0,
                    getSearchInputWidth(centerX),
                    20,
                    Component.literal("Nature")
            );
            natureBox.setMaxLength(256);
            natureBox.setValue(String.join(", ", RadarFilter.NATURE_FILTERS));
            this.addRenderableWidget(natureBox);

            checkNatureSearch = Checkbox.builder(Component.empty(), this.font)
                    .pos(getSearchCheckboxX(centerX), 0)
                    .selected(RadarFilter.SHOW_NATURE_SEARCH)
                    .build();
            this.addRenderableWidget(checkNatureSearch);
        } else {
            natureBox = null;
            checkNatureSearch = null;
        }

        checkLegendary = Checkbox.builder(Component.literal("Show Legendary/Mythical/Ultra Beast"), this.font)
                .pos(left, 0)
                .selected(RadarFilter.SHOW_LEGENDARY)
                .build();
        this.addRenderableWidget(checkLegendary);

        checkShiny = Checkbox.builder(Component.literal("Show Shiny"), this.font)
                .pos(left, 0)
                .selected(RadarFilter.SHOW_SHINY)
                .build();
        this.addRenderableWidget(checkShiny);

        checkDitto = Checkbox.builder(Component.literal("Show Ditto"), this.font)
                .pos(left, 0)
                .selected(RadarFilter.SHOW_DITTO)
                .build();
        this.addRenderableWidget(checkDitto);


        if (showHiddenAbilityOption()) {
            checkHiddenAbility = Checkbox.builder(Component.literal("Show Hidden Ability"), this.font)
                    .pos(left, 0)
                    .selected(RadarFilter.SHOW_HIDDEN_ABILITY)
                    .build();
            this.addRenderableWidget(checkHiddenAbility);
        } else {
            checkHiddenAbility = null;
        }


        checkUncaught = Checkbox.builder(Component.literal("Show Uncaught"), this.font)
                .pos(left, 0)
                .selected(RadarFilter.SHOW_UNCAUGHT)
                .build();
        this.addRenderableWidget(checkUncaught);

        checkHitbox = Checkbox.builder(Component.literal("Show Hitbox (Box around Pokemon)"), this.font)
                .pos(left, 0)
                .selected(RadarFilter.SHOW_HITBOX)
                .build();
        this.addRenderableWidget(checkHitbox);

        genderButton = Button.builder(
                Component.literal("Gender: " + RadarFilter.SELECTED_GENDER.name()),
                btn -> {
                    RadarFilter.SELECTED_GENDER = RadarFilter.SELECTED_GENDER.next();
                    btn.setMessage(Component.literal("Gender: " + RadarFilter.SELECTED_GENDER.name()));
                }
        ).bounds(left, 0, PANEL_WIDTH, 20).build();
        this.addRenderableWidget(genderButton);

        applyButton = Button.builder(
                Component.literal("Apply"),
                btn -> applyFilterAndClose()
        ).bounds(left, 0, PANEL_WIDTH, 20).build();
        this.addRenderableWidget(applyButton);

        layoutWidgets();
    }


    private void applyFilterAndClose() {

        RadarFilter.FILTERS.clear();

        String input = textBox.getValue().toLowerCase();
        for (String part : input.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                RadarFilter.FILTERS.add(trimmed);
            }
        }

        RadarFilter.NATURE_FILTERS.clear();

        if (natureBox != null) {
            String natureInput = natureBox.getValue().toLowerCase();
            for (String part : natureInput.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    RadarFilter.NATURE_FILTERS.add(trimmed);
                }
            }
        }


        RadarFilter.SHOW_LEGENDARY = checkLegendary.selected();
        RadarFilter.SHOW_SHINY = checkShiny.selected();
        RadarFilter.SHOW_DITTO = checkDitto.selected();
        RadarFilter.SHOW_SEARCH = checkSearch.selected();
        RadarFilter.SHOW_HITBOX = checkHitbox.selected();
        RadarFilter.SHOW_UNCAUGHT = checkUncaught.selected();
        if (checkHiddenAbility != null) {
            RadarFilter.SHOW_HIDDEN_ABILITY = checkHiddenAbility.selected();
        }
        if (checkNatureSearch != null) {
            RadarFilter.SHOW_NATURE_SEARCH = checkNatureSearch.selected();
        }


        this.minecraft.setScreen(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257) { // ENTER
            applyFilterAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        int nextOffset = this.scrollOffset - (int) Math.round(scrollY * SCROLL_STEP);
        this.scrollOffset = clampScroll(nextOffset);
        layoutWidgets();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        super.render(graphics, mouseX, mouseY, delta);

        renderCheckboxBorder(graphics, checkSearch, 0xFF0096FF);
        graphics.drawCenteredString(this.font, "Pokemon (comma separated)", centerX, textBox.getY() - 12, 0xFFFFFF);

        if (natureBox != null) {
            renderCheckboxBorder(graphics, checkNatureSearch, 0xFFFF8C00);
            graphics.drawCenteredString(this.font, "Nature (comma separated)", centerX, natureBox.getY() - 12, 0xFFFFFF);
        }

        renderCheckboxBorder(graphics, checkLegendary, 0xFFFF0000);
        renderCheckboxBorder(graphics, checkShiny, 0xFFFFC800);
        renderCheckboxBorder(graphics, checkDitto, 0xFFFF69B4);
        renderCheckboxBorder(graphics, checkHiddenAbility, 0xFFFFFFFF);
        renderCheckboxBorder(graphics, checkUncaught, 0xFF00FF00);

        renderScrollbar(graphics, centerX);

    }

    private boolean showHiddenAbilityOption() {
        return HiddenAbilityCache.FEATURE_AVAILABLE || (this.minecraft != null && this.minecraft.hasSingleplayerServer());
    }

    private boolean showNatureOption() {
        return NatureCache.FEATURE_AVAILABLE || (this.minecraft != null && this.minecraft.hasSingleplayerServer());
    }

    private void layoutWidgets() {
        this.contentHeight = computeContentHeight();
        this.scrollOffset = clampScroll(this.scrollOffset);

        int centerX = this.width / 2;
        int left = centerX - PANEL_WIDTH / 2;
        int y = getContentTop() - this.scrollOffset;

        textBox.setPosition(getSearchInputX(centerX), y + SEARCH_ROW_OFFSET);
        checkSearch.setPosition(getSearchCheckboxX(centerX), y + SEARCH_ROW_OFFSET);
        y += 40;

        if (natureBox != null) {
            natureBox.setPosition(getSearchInputX(centerX), y + SEARCH_ROW_OFFSET);
            checkNatureSearch.setPosition(getSearchCheckboxX(centerX), y + SEARCH_ROW_OFFSET);
            y += 40;
        }

        checkLegendary.setPosition(left, y);
        y += 25;

        checkShiny.setPosition(left, y);
        y += 25;

        checkDitto.setPosition(left, y);
        y += 25;

        if (checkHiddenAbility != null) {
            checkHiddenAbility.setPosition(left, y);
            y += 25;
        }

        checkUncaught.setPosition(left, y);
        y += 25;

        checkHitbox.setPosition(left, y);
        y += 30;

        genderButton.setPosition(left, y);
        y += 30;

        applyButton.setPosition(left, y);
    }

    private int computeContentHeight() {
        int height = 40; // Pokemon label + search box
        if (natureBox != null) {
            height += 40; // Nature label + search box
        }

        int checkboxCount = 5; // Legendary, Shiny, Ditto, Uncaught, Hitbox
        if (checkHiddenAbility != null) {
            checkboxCount++;
        }

        height += checkboxCount * 25;
        height += 30; // gap before gender button
        height += 20; // gender button
        height += 30; // gap before apply button
        height += 20; // apply button

        return height;
    }

    private int getContentTop() {
        int availableHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;
        int centeredTop = TOP_MARGIN + Math.max(0, (availableHeight - this.contentHeight) / 2);
        return centeredTop;
    }

    private int getMaxScroll() {
        int availableHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;
        return Math.max(0, this.contentHeight - availableHeight);
    }

    private int clampScroll(int value) {
        return Math.max(0, Math.min(value, getMaxScroll()));
    }

    private void renderScrollbar(GuiGraphics graphics, int centerX) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) {
            return;
        }

        int trackX1 = centerX + PANEL_WIDTH / 2 + 18;
        int trackX2 = trackX1 + 6;
        int trackY1 = TOP_MARGIN;
        int trackY2 = this.height - BOTTOM_MARGIN;
        int trackHeight = trackY2 - trackY1;

        graphics.fill(trackX1, trackY1, trackX2, trackY2, 0x80404040);

        int thumbHeight = Math.max(20, (int) ((trackHeight * (double) trackHeight) / this.contentHeight));
        int travel = trackHeight - thumbHeight;
        int thumbY = trackY1 + (int) ((this.scrollOffset / (double) maxScroll) * travel);

        graphics.fill(trackX1, thumbY, trackX2, thumbY + thumbHeight, 0xFFC0C0C0);
    }

    private int getSearchCheckboxX(int centerX) {
        return centerX - 115 + LEGEND_SIZE + SEARCH_BOX_GAP;
    }

    private int getSearchInputX(int centerX) {
        return getSearchCheckboxX(centerX) + SEARCH_BOX_OFFSET;
    }

    private int getSearchInputWidth(int centerX) {
        int panelRight = centerX + PANEL_WIDTH / 2;
        return panelRight - getSearchInputX(centerX);
    }

    private void renderCheckboxBorder(GuiGraphics graphics, Checkbox checkbox, int color) {
        if (checkbox == null) {
            return;
        }

        int x1 = checkbox.getX() - 1;
        int y1 = checkbox.getY() - 1;
        int x2 = x1 + 19;
        int y2 = y1 + 19;

        graphics.fill(x1, y1, x2, y1 + 1, color);
        graphics.fill(x1, y2 - 1, x2, y2, color);
        graphics.fill(x1, y1, x1 + 1, y2, color);
        graphics.fill(x2 - 1, y1, x2, y2, color);
    }


}
