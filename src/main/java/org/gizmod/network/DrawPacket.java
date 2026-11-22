package org.gizmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import org.gizmod.chalk.ChalkActions;
import org.gizmod.chalk.ChalkPixel;

import java.util.function.Supplier;

public record DrawPacket(BlockPos pos, Direction face, InteractionHand hand, int[] pixels,
                         int[] palette) {

    public static void encode(DrawPacket msg, FriendlyByteBuf buf){
        buf.writeBlockPos(msg.pos);
        buf.writeEnum(msg.face);
        buf.writeEnum(msg.hand);
        buf.writeVarIntArray(msg.pixels);
        buf.writeVarIntArray(msg.pixels);
    }

    public static DrawPacket decode(FriendlyByteBuf buf){
        BlockPos pos = buf.readBlockPos();
        Direction face = buf.readEnum(Direction.class);
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        int[] pixels = buf.readVarIntArray(ChalkPixel.COUNT);
        int[] palette = buf.readVarIntArray(ChalkPixel.COUNT);
        return new DrawPacket(pos, face, hand, pixels, palette);
    }

    public static void handle(DrawPacket msg, Supplier<NetworkEvent.Context> ctx){
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if(sender != null){
                ChalkActions.applyDrawing(sender, msg);
            }
        });

        context.setPacketHandled(true);
    }
}
