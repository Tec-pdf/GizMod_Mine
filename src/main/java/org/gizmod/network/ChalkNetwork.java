package org.gizmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.gizmod.Gizmod;
import org.gizmod.chalk.ChalkFace;
import org.gizmod.chalk.FaceKey;

import java.util.Optional;

public final class ChalkNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Gizmod.MODID, "main"),
            () -> PROTOCOL, PROTOCOL :: equals, PROTOCOL :: equals);

    private ChalkNetwork(){}

    public static void register(){
        int id = 0;
        CHANNEL.registerMessage(id++, DrawPacket.class,
                DrawPacket :: encode, DrawPacket :: decode, DrawPacket :: handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, FaceSyncPacket.class,
                FaceSyncPacket :: encode, FaceSyncPacket :: decode, FaceSyncPacket :: handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void syncFace(ServerLevel level, FaceKey key, ChalkFace face){
        LevelChunk chunk = level.getChunkSource().getChunkNow(key.blockPos().getX() >> 4,
            key.blockPos().getZ() >> 4);
        if(chunk == null) return;
        byte[] pixels = face == null ? null : face.pixels.clone();
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), FaceSyncPacket.single(key,
            pixels));
    }
}
