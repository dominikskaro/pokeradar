package com.dominik.modid.client;

import com.dominik.modid.network.HiddenAbilityFeaturePacket;
import com.dominik.modid.network.HiddenAbilityPackets;
import com.dominik.modid.network.NatureFeaturePacket;
import com.dominik.modid.network.NatureSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class PokeRadarClient implements ClientModInitializer {

	public static KeyMapping OPEN_RADAR;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		RadarClient.init();
		RadarRenderer.init();
		OPEN_RADAR = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"Open Analysis Menu",
						GLFW.GLFW_KEY_DELETE,
						"Pokemon Analysis"
				)
		);

		ClientPlayNetworking.registerGlobalReceiver(HiddenAbilityPackets.HIDDEN_ABILITY_SYNC, (payload, context) -> {
			context.client().execute(() -> {
				HiddenAbilityCache.ENTITY_HIDDEN_ABILITY.put(payload.entityId(), payload.hiddenAbility());
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(HiddenAbilityFeaturePacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				HiddenAbilityCache.FEATURE_AVAILABLE = payload.available();
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(NatureSyncPacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				NatureCache.ENTITY_NATURES.put(payload.entityId(), payload.natureName());
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(NatureFeaturePacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				NatureCache.FEATURE_AVAILABLE = payload.available();
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			HiddenAbilityCache.reset();
			NatureCache.reset();
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_RADAR.consumeClick()) {
				client.setScreen(new RadarScreen());
			}
		});
	}
}