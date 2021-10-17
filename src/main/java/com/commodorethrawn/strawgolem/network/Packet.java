package com.commodorethrawn.strawgolem.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;

public abstract class Packet {

    NbtCompound tag;

    Packet(MinecraftClient client, PacketByteBuf byteBuf) {
        this.tag = byteBuf.readNbt();
        client.execute(this::execute);
    }

    Packet(MinecraftServer server, PacketByteBuf byteBuf) {
        this.tag = byteBuf.readNbt();
        server.execute(this::execute);
    }

    public Packet() {
        tag = new NbtCompound();
    }

    public void encode(PacketByteBuf byteBuf) {
        byteBuf.writeNbt(tag);
    }

    public abstract void execute();


}
