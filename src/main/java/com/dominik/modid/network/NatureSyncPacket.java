package com.dominik.modid.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NatureSyncPacket(int entityId, String natureName) implements CustomPacketPayload {

    public static final Type<NatureSyncPacket> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath("pokeradar", "nature_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NatureSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    NatureSyncPacket::entityId,
                    ByteBufCodecs.STRING_UTF8,
                    NatureSyncPacket::natureName,
                    NatureSyncPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
