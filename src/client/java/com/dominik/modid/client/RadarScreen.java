package com.dominik.modid.client;

import com.dominik.modid.RadarFilter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RadarScreen extends Screen {

    private EditBox textBox;
    private Checkbox checkLegendary;
    private Checkbox checkShiny;
    private Checkbox checkDitto;
    private Checkbox checkSearch;
    private Checkbox checkHitbox;
    private Button genderButton;
    private Checkbox checkUncaught;

    public RadarScreen() {
        super(Component.literal("Radar Filter"));
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int yOffset = -70;
        centerY += yOffset;

        // search box
        textBox = new EditBox(
                this.font,
                centerX - 100,
                centerY - 40,
                200,
                20,
                Component.literal("Pokemon name")
        );
        textBox.setValue(String.join(", ", RadarFilter.FILTERS));
        this.addRenderableWidget(textBox);
        this.setFocused(textBox);
        textBox.setFocused(true);

        // checkboxovi
        checkLegendary = Checkbox.builder(Component.literal("Show Lines for Legendary/Mythical/Ultra Beast"), this.font)
                .pos(centerX - 100, centerY - 10)
                .selected(RadarFilter.SHOW_LEGENDARY)
                .build();
        this.addRenderableWidget(checkLegendary);

        checkShiny = Checkbox.builder(Component.literal("Show Lines for Shiny"), this.font)
                .pos(centerX - 100, centerY + 15)
                .selected(RadarFilter.SHOW_SHINY)
                .build();
        this.addRenderableWidget(checkShiny);

        checkDitto = Checkbox.builder(Component.literal("Show Lines for Ditto"), this.font)
                .pos(centerX - 100, centerY + 40)
                .selected(RadarFilter.SHOW_DITTO)
                .build();
        this.addRenderableWidget(checkDitto);

        checkSearch = Checkbox.builder(Component.literal("Show Lines for Search"), this.font)
                .pos(centerX - 100, centerY + 65)
                .selected(RadarFilter.SHOW_SEARCH)
                .build();
        this.addRenderableWidget(checkSearch);

        checkUncaught = Checkbox.builder(Component.literal("Show Lines for Uncaught Pokemon"), this.font)
                .pos(centerX - 100, centerY + 90)
                .selected(RadarFilter.SHOW_UNCAUGHT)
                .build();
        this.addRenderableWidget(checkUncaught);

        checkHitbox = Checkbox.builder(Component.literal("Show Hitbox (Box around Pokemon)"), this.font)
                .pos(centerX - 100, centerY + 115)
                .selected(RadarFilter.SHOW_HITBOX)
                .build();
        this.addRenderableWidget(checkHitbox);

        genderButton = Button.builder(
                Component.literal("Gender: " + RadarFilter.SELECTED_GENDER.name()),
                btn -> {
                    RadarFilter.SELECTED_GENDER = RadarFilter.SELECTED_GENDER.next();
                    btn.setMessage(Component.literal("Gender: " + RadarFilter.SELECTED_GENDER.name()));
                }
        ).bounds(centerX - 100, centerY + 140, 200, 20).build();

        this.addRenderableWidget(genderButton);

        // apply button
        Button applyButton = Button.builder(
                Component.literal("Apply"),
                btn -> applyFilterAndClose()
        ).bounds(
                centerX - 100,
                centerY + 170,
                200,
                20
        ).build();
        this.addRenderableWidget(applyButton);
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

        RadarFilter.SHOW_LEGENDARY = checkLegendary.selected();
        RadarFilter.SHOW_SHINY = checkShiny.selected();
        RadarFilter.SHOW_DITTO = checkDitto.selected();
        RadarFilter.SHOW_SEARCH = checkSearch.selected();
        RadarFilter.SHOW_HITBOX = checkHitbox.selected();
        RadarFilter.SHOW_UNCAUGHT = checkUncaught.selected();

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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        textBox.render(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int yOffset = -70;
        centerY += yOffset;

        graphics.fill(centerX - 115, centerY - 10 + 3, centerX - 115 + 8, centerY - 10 + 11, 0xFFFF0000); // crvena
        graphics.fill(centerX - 115, centerY + 15 + 3, centerX - 115 + 8, centerY + 15 + 11, 0xFFFFC800); // žuta
        graphics.fill(centerX - 115, centerY + 40 + 3, centerX - 115 + 8, centerY + 40 + 11, 0xFFFF69B4); // roza
        graphics.fill(centerX - 115, centerY + 65 + 3, centerX - 115 + 8, centerY + 65 + 11, 0xFF0096FF); // plava
        graphics.fill(centerX - 115, centerY + 90 + 3, centerX - 115 + 8, centerY + 90 + 11, 0xFF00FF00); // zelena

        graphics.drawCenteredString(this.font, "Pokemon names (use comma to search for multiple)", centerX, centerY - 55, 0xFFFFFF);

    }
}