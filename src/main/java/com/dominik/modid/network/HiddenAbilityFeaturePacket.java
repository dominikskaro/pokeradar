package com.dominik.modid.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HiddenAbilityFeaturePacket(boolean available) implements CustomPacketPayload {

    public static final Type<HiddenAbilityFeaturePacket> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath("pokeradar", "hidden_ability_feature"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HiddenAbilityFeaturePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    HiddenAbilityFeaturePacket::available,
                    HiddenAbilityFeaturePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
