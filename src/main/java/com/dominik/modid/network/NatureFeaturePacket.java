package com.dominik.modid.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NatureFeaturePacket(boolean available) implements CustomPacketPayload {

    public static final Type<NatureFeaturePacket> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath("pokeradar", "nature_feature"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NatureFeaturePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    NatureFeaturePacket::available,
                    NatureFeaturePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
