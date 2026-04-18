package com.dominik.modid;

import com.dominik.modid.network.HiddenAbilityFeaturePacket;
import com.dominik.modid.network.HiddenAbilityPackets;
import com.dominik.modid.network.NatureFeaturePacket;
import com.dominik.modid.network.NatureSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PokeRadar implements ModInitializer {
	public static final String MOD_ID = "pokeradar";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		PayloadTypeRegistry.playS2C().register(HiddenAbilityPackets.HIDDEN_ABILITY_SYNC, HiddenAbilityPackets.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(HiddenAbilityFeaturePacket.ID, HiddenAbilityFeaturePacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(NatureSyncPacket.ID, NatureSyncPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(NatureFeaturePacket.ID, NatureFeaturePacket.STREAM_CODEC);
		HiddenAbilitySyncService.init();
		NatureSyncService.init();
		LOGGER.info("Hello Fabric world!");
	}
}