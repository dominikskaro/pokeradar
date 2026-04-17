package com.dominik.modid;

import java.util.ArrayList;
import java.util.List;

public class RadarFilter {
    public static List<String> FILTERS = new ArrayList<>();
    public static boolean SHOW_LEGENDARY = true;
    public static boolean SHOW_SHINY = true;
    public static boolean SHOW_DITTO = true;
    public static boolean SHOW_SEARCH = true;
    public static boolean SHOW_HITBOX = false;
    public static boolean SHOW_UNCAUGHT = false;
    public static boolean SHOW_HIDDEN_ABILITY = true;


    public enum GenderFilter {
        ANY, MALE, FEMALE, GENDERLESS;

        public GenderFilter next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    public static GenderFilter SELECTED_GENDER = GenderFilter.ANY;
}
