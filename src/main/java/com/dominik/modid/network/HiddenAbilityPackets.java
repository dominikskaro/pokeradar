package com.dominik.modid.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HiddenAbilityPackets(int entityId, boolean hiddenAbility) implements CustomPacketPayload {

    public static final Type<HiddenAbilityPackets> HIDDEN_ABILITY_SYNC =
            new Type<>(ResourceLocation.fromNamespaceAndPath("pokeradar", "hidden_ability_sync"));


    public static final StreamCodec<RegistryFriendlyByteBuf, HiddenAbilityPackets> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    HiddenAbilityPackets::entityId,
                    ByteBufCodecs.BOOL,
                    HiddenAbilityPackets::hiddenAbility,
                    HiddenAbilityPackets::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return HIDDEN_ABILITY_SYNC;
    }
}
