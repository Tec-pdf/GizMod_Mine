package org.gizmod.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.gizmod.chalk.ChalkClientCache;
import org.gizmod.chalk.ChalkPixel;
import org.gizmod.chalk.FaceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class FaceSyncPacket {
    public static final int BATCH_SIZE = 256; //faces enviadas por packet
    private static final int MAX_ENTRIES = 512; //máx aceito pelo client

    public record Entry(long pos, Direction dir, byte[] pixels){}

    private final boolean replaceChunk;
    private final long chunk;
    private final List<Entry> entries;

    private FaceSyncPacket(boolean replaceChunk, long chunk, List<Entry> entries){
        this.replaceChunk = replaceChunk;
        this.chunk = chunk;
        this.entries = entries;
    }

    public static FaceSyncPacket single(FaceKey key, byte[] pixelsOrNull){
        List<Entry> list = new ArrayList<>();
        list.add(new Entry(key.pos(), key.dir(), pixelsOrNull == null ? new byte[0]
                : pixelsOrNull));
        return new FaceSyncPacket(false, 0L, list);
    }

    public static FaceSyncPacket forChunk(long chunk, boolean replace, List<Entry> entries){
        return new FaceSyncPacket(replace, chunk, entries);
    }

    public static void encode(FaceSyncPacket msg, FriendlyByteBuf buf){
        buf.writeBoolean(msg.replaceChunk);
        buf.writeLong(msg.chunk);
        buf.writeVarInt(msg.entries.size());
        for(Entry e : msg.entries){
            buf.writeLong(e.pos());
            buf.writeEnum(e.dir());
            buf.writeByteArray(e.pixels());
        }
    }

    public static FaceSyncPacket decode(FriendlyByteBuf buf){
        boolean replace = buf.readBoolean();
        long chunk = buf.readLong();
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENTRIES){
            throw new DecoderException("Quantidade máxima de faces inválidas atingidas: " + count);
        }

        List<Entry> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++){
            long pos = buf.readLong();
            Direction dir = buf.readEnum(Direction.class);
            byte[] pixels = buf.readByteArray(ChalkPixel.COUNT);
            if (pixels.length != 0 && pixels.length != ChalkPixel.COUNT){
                throw new DecoderException("Desenho com tamanho inválido: " + pixels.length);
            }

            list.add(new Entry(pos, dir, pixels));
        }

        return new FaceSyncPacket(replace, chunk, list);
    }

    public static void handle(FaceSyncPacket msg, Supplier<NetworkEvent.Context> ctx){
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if(msg.replaceChunk){
                ChalkClientCache.clearChunk(msg.chunk);
            }

            for(Entry e : msg.entries){
                FaceKey key = new FaceKey(e.pos(), e.dir());
                if(e.pixels().length == 0){
                    ChalkClientCache.remove(key);
                } else {
                    ChalkClientCache.put(key, e.pixels());
                }
            }
        });

        context.setPacketHandled(true);
    }
}
