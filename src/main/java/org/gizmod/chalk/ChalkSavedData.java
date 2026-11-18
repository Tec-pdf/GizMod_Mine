package org.gizmod.chalk;

import org.gizmod.ChalkConfig;
import org.gizmod.network.ChalkNetwork;
import org.gizmod.network.FaceSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//todos os desenhos de uma dimensão, salvos junto com o mundo
public class ChalkSavedData extends SavedData {
    private static final String NAME = "gizmod_chalk";

    private final Map<FaceKey, ChalkFace> faces = new HashMap<>();
    private final Map<Long, Set<FaceKey>> byChunk = new HashMap<>();   // índice por chunk

    public static ChalkSavedData of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ChalkSavedData::load, ChalkSavedData::new,
                NAME);
    }

    public ChalkFace get(FaceKey key) {
        return faces.get(key);
    }

    public ChalkFace getOrCreate(FaceKey key) {
        ChalkFace face = faces.get(key);
        if (face == null) {
            face = new ChalkFace();
            faces.put(key, face);
            byChunk.computeIfAbsent(key.chunk(), c -> new HashSet<>()).add(key);
            setDirty();
        }
        return face;
    }

    public void remove(FaceKey key) {
        if (faces.remove(key) != null) {
            Set<FaceKey> set = byChunk.get(key.chunk());
            if (set != null) {
                set.remove(key);
                if (set.isEmpty()) byChunk.remove(key.chunk());
            }
            setDirty();
        }
    }

    //total de faces desenhadas nesta dimensão
    public int faceCount() {
        return faces.size();
    }

    //faces desenhadas dentro de um chunk (long)
    public int faceCountInChunk(long chunk) {
        Set<FaceKey> set = byChunk.get(chunk);
        return set == null ? 0 : set.size();
    }

    //todos os desenhos de um chunk, prontos para enviar a um jogador
    public List<FaceSyncPacket.Entry> entriesInChunk(ChunkPos chunkPos) {
        List<FaceSyncPacket.Entry> out = new ArrayList<>();
        Set<FaceKey> set = byChunk.get(chunkPos.toLong());
        if (set != null) {
            for (FaceKey key : set) {
                out.add(new FaceSyncPacket.Entry(key.pos(), key.dir(),
                        faces.get(key).pixels.clone()));
            }
        }
        return out;
    }

    //chama cada tick de servidor: água, validade dos temporários e existência do bloco
    public void tick(ServerLevel level) {
        if (faces.isEmpty()) return;

        long time = level.getGameTime();
        boolean erodeTick = time % ChalkConfig.WATER_STEP_INTERVAL_TICKS == 0;
        boolean slowTick = time % 20 == 0;                       // 1 vez por segundo

        List<FaceKey> changed = new ArrayList<>();
        List<FaceKey> removed = new ArrayList<>();

        for (Map.Entry<FaceKey, ChalkFace> entry : faces.entrySet()) {
            FaceKey key = entry.getKey();
            ChalkFace face = entry.getValue();
            BlockPos pos = key.blockPos();
            BlockPos front = pos.relative(key.dir());            // espaço onde o desenho "vive"
            // Nunca leia blocos de chunks descarregados: o servidor os carregaria à força (lag)
            if (!level.hasChunkAt(pos) || !level.hasChunkAt(front)) continue;

            boolean dirty = false;
            if (level.getFluidState(front).is(FluidTags.WATER)) {
                if (erodeTick) dirty |= face.erode(level.random);
            } else {
                face.erosionStage = 0;
            }

            if (slowTick) {
                dirty |= face.expire(time);
                if (!level.getBlockState(pos).isFaceSturdy(level, pos, key.dir())) {
                    face.clear();                                // bloco quebrado/trocado
                    dirty = true;
                }
            }

            if (face.isEmpty()) removed.add(key);
            else if (dirty) changed.add(key);
        }

        if (changed.isEmpty() && removed.isEmpty()) return;
        for (FaceKey key : removed) {
            remove(key);
            ChalkNetwork.syncFace(level, key, null);
        }
        for (FaceKey key : changed) {
            ChalkNetwork.syncFace(level, key, faces.get(key));
        }
        setDirty();
    }

    //Persistência

    public static ChalkSavedData load(CompoundTag tag) {
        ChalkSavedData data = new ChalkSavedData();
        ListTag list = tag.getList("faces", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            Direction dir = Direction.byName(entry.getString("dir"));
            if (dir == null) continue;
            FaceKey key = new FaceKey(entry.getLong("pos"), dir);
            data.faces.put(key, ChalkFace.load(entry));
            data.byChunk.computeIfAbsent(key.chunk(), c -> new HashSet<>()).add(key);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<FaceKey, ChalkFace> entry : faces.entrySet()) {
            CompoundTag faceTag = entry.getValue().save();
            faceTag.putLong("pos", entry.getKey().pos());
            faceTag.putString("dir", entry.getKey().dir().getName());
            list.add(faceTag);
        }
        tag.put("faces", list);
        return tag;
    }
}
