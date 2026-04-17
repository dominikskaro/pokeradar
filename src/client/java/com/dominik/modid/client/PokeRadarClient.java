package com.dominik.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class PokeRadarClient implements ClientModInitializer {

	public static KeyMapping OPEN_RADAR;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		RadarClient.init();
		RadarRenderer.init();
		// keybind
		OPEN_RADAR = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"Open Analysis Menu",
						GLFW.GLFW_KEY_R,
						"Pokemon Analysis"
				)
		);

		// 🔥 OVO TI JE FALILO
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_RADAR.consumeClick()) {
				client.setScreen(new RadarScreen());
			}
		});
	}
}