package com.dominik.modid.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.dominik.modid.RadarData;
import com.dominik.modid.RadarFilter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class RadarClient {

    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.level == null) {
                HiddenAbilityCache.ENTITY_HIDDEN_ABILITY.clear();
                return;
            }
            RadarData.TARGETS.clear();

            for (Entity entity : client.level.getEntities(client.player, client.player.getBoundingBox().inflate(128))) {

                if (!(entity instanceof PokemonEntity pokemonEntity) || entity == client.player) continue;

                var key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                if (!key.getNamespace().equals("cobblemon")) continue;

                Pokemon pokemon = pokemonEntity.getPokemon();

                boolean hiddenAbilityFeatureAvailable = HiddenAbilityCache.FEATURE_AVAILABLE;
                boolean isHiddenAbility = hiddenAbilityFeatureAvailable
                        && HiddenAbilityCache.ENTITY_HIDDEN_ABILITY.getOrDefault(entity.getId(), false)
                        && RadarFilter.SHOW_HIDDEN_ABILITY;


                var pokedexManager = CobblemonClient.INSTANCE.getClientPokedexData();
                var speciesId = pokemon.getSpecies().getResourceIdentifier();
                var progress = pokedexManager.getKnowledgeForSpecies(speciesId);
                boolean isUncaught = progress != PokedexEntryProgress.CAUGHT && RadarFilter.SHOW_UNCAUGHT;

                var gender = pokemon.getGender();

                if (RadarFilter.SELECTED_GENDER != RadarFilter.GenderFilter.ANY) {
                    if (RadarFilter.SELECTED_GENDER == RadarFilter.GenderFilter.MALE && !gender.toString().equalsIgnoreCase("MALE")) continue;
                    if (RadarFilter.SELECTED_GENDER == RadarFilter.GenderFilter.FEMALE && !gender.toString().equalsIgnoreCase("FEMALE")) continue;
                    if (RadarFilter.SELECTED_GENDER == RadarFilter.GenderFilter.GENDERLESS && !gender.toString().equalsIgnoreCase("GENDERLESS")) continue;
                }

                boolean hasOwner = pokemonEntity.getOwnerUUID() != null;

// Provjera 2: Je li pokemon u bitci ili pripada nekom trenažeru?
// (isWild() nekad zakaže, pa kombiniramo)
                boolean isNotWild = !pokemon.isWild() || hasOwner;

                if (isNotWild) continue;

                String name = pokemon.getSpecies().getName().toLowerCase();

                boolean isSpecial = (pokemon.isLegendary() || pokemon.isMythical() || pokemon.isUltraBeast()) && RadarFilter.SHOW_LEGENDARY;
                boolean isShiny = pokemon.getShiny() && RadarFilter.SHOW_SHINY;
                boolean isDitto = name.equals("ditto") && RadarFilter.SHOW_DITTO;
                boolean isTarget = isSpecial || isShiny || isDitto || isHiddenAbility || isUncaught;

                if (!RadarFilter.FILTERS.isEmpty()) {
                    if (!isTarget) {
                        if (!RadarFilter.SHOW_SEARCH) continue;
                        boolean match = false;
                        for (String filter : RadarFilter.FILTERS) {
                            if (name.contains(filter)) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) continue;
                    }
                } else {
                    if (!isTarget) continue;
                }

                int[] color;
                if (name.equals("ditto")) {
                    color = new int[]{255, 105, 180, 220}; // roza
                } else if (isHiddenAbility) {
                    color = new int[]{255, 255, 255, 220}; // bijela
                } else if (isSpecial) {
                    color = new int[]{255, 0, 0, 255}; // crvena
                } else if (isShiny) {
                    color = new int[]{255, 200, 0, 220}; // zuta
                }else if (isUncaught) {
                        color = new int[]{0, 255, 0, 220}; // zelena
                } else {
                    color = new int[]{0, 150, 255, 220}; // plava
                }

                RadarData.TARGETS.put(entity, color);
            }
        });
    }
}