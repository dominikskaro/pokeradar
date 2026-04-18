package com.dominik.modid;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.dominik.modid.network.NatureFeaturePacket;
import com.dominik.modid.network.NatureSyncPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;

public class NatureSyncService {

    private static int tickCounter = 0;

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayNetworking.send(handler.player, new NatureFeaturePacket(true));
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            if (tickCounter < 20) {
                return;
            }

            tickCounter = 0;

            for (var player : server.getPlayerList().getPlayers()) {
                if (player.level() == null) continue;

                for (Entity entity : player.level().getEntities(player, player.getBoundingBox().inflate(128))) {
                    if (!(entity instanceof PokemonEntity pokemonEntity)) continue;

                    Pokemon pokemon = pokemonEntity.getPokemon();

                    boolean hasOwner = pokemonEntity.getOwnerUUID() != null;
                    boolean isNotWild = !pokemon.isWild() || hasOwner;
                    if (isNotWild) continue;

                    String natureName = pokemon.getNature().getName().toString().toLowerCase();

                    ServerPlayNetworking.send(player, new NatureSyncPacket(entity.getId(), natureName));
                }
            }
        });
    }
}
