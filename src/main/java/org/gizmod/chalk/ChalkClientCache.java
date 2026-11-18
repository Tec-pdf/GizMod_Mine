package org.gizmod.chalk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ChalkClientCache {
    private static final Map<FaceKey, byte[]> FACES = new HashMap<>();
    private static final Map<FaceKey, Set<FaceKey>> BY_CHUNK = new HashMap<>();

    private ChalkClientCache(){}

    private static void put(FaceKey key, byte[] pixels){
        if(pixels.length != ChalkPixel.COUNT) return;
        FACES.put(key, pixels);
        BY_CHUNK.computeIfAbsent(key.chunk(), c -> new HashSet<>()).add(key);
    }

    public static void remove(FaceKey key){
        if(FACES.remove(key) != null){
            Set<FaceKey> set = BY_CHUNK.get(key.chunk());
            if(set != null) set.remove(key);
        }
    }

    public static byte[] get(FaceKey key){
        return FACES.get(key);
    }

    public static Map<FaceKey, byte[]> all(){
        return FACES;
    }

    public static void clearChunk(long chunkLong){
        Set<FaceKey> set = BY_CHUNK.remove(chunkLong);
        if(set != null){
            for(FaceKey key : set) FACES.remove(key);
        }
    }

    public static void clear(){
        FACES.clear();
        BY_CHUNK.clear();
    }
}
