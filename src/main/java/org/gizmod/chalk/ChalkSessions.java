package org.gizmod.chalk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ChalkSessions {
    private record Session(ResourceKey<Level> dimension, BlockPos pos, Direction face,
                           InteractionHand hand, int expiresAt){}

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, Integer> LAST_DRAWING = new HashMap<>();

    private ChalkSessions(){}

    private static int now(ServerPlayer player){
        return player.serverLevel().getServer().getTickCount();
    }

    public static void open(ServerPlayer player, BlockPos pos, Direction face,
                            InteractionHand hand){
        SESSIONS.put(player.getUUID(), new Session(player.level().dimension(), pos.immutable(),
                face, hand,
                now(player) + org.gizmod.ChalkConfig.DRAW_SESSION_TICKS));
    }

    public static boolean consume(ServerPlayer player, BlockPos pos, Direction face,
                                  InteractionHand hand){
        Session session = SESSIONS.remove(player.getUUID());
        if(session == null) return false;

        int now = now(player);
        if(now > session.expiresAt()) return false;
        if(!session.dimension().equals(player.level().dimension())) return false;
        if(!session.pos().equals(pos) || session.face() != face || session.hand() !=
                hand) return false;

        Integer last = LAST_DRAWING.get(player.getUUID());
        if(last != null && now - last < org.gizmod.ChalkConfig.MIN_TICKS_BETWEEN_DRAWINGS) return false;
        LAST_DRAWING.put(player.getUUID(), now);
        return true;
    }

    public static void forget(UUID player) {
        SESSIONS.remove(player);
        LAST_DRAWING.remove(player);
    }

    public static void forgetAll() {
        SESSIONS.clear();
        LAST_DRAWING.clear();
    }
}
