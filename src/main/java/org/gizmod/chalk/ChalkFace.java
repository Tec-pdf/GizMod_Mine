package org.gizmod.chalk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import org.gizmod.ChalkConfig;

import java.util.*;

public class ChalkFace {
    public final byte[] pixels = new byte[ChalkPixel.COUNT];

    public long[] expiry;

    public int erosionStage = 0;

    public boolean set(int index, DyeColor color, ChalkVariant variant, long gameTime){

        byte value = ChalkPixel.encode(color, variant);
        boolean changed = pixels[index] != value;
        pixels[index] = value;
        if (variant == ChalkVariant.TEMPORARY){
            if (expiry == null) expiry = new long[ChalkPixel.COUNT];
            expiry[index] = gameTime + ChalkConfig.TEMPORARY_LIFETIME_TICKS;
        } else if (expiry != null) {
            expiry[index] = 0L;
        }
        return changed;
    }

    public boolean isEmpty(){
        for (byte b : pixels) {
            if (ChalkPixel.isPresent(b)) return false;
        }
        return true;
    }

    public void clear(){
        Arrays.fill(pixels, (byte) 0);
        expiry = null;
    }

    public boolean expire(long gameTime){
        if (expiry == null) return false;
        boolean changed = false;
        for (int i=0; i < pixels.length; i++){
            if (ChalkPixel.isPresent(pixels[i]) && expiry[i] > 0 && gameTime >= expiry[i]) {
                pixels[i] = 0;
                expiry[i] = 0;
                changed = true;
            }
        }
        return changed;
    }

    public boolean erode(RandomSource random){
        List<Integer> candidates = new ArrayList<>();
        for (int i=0; i < pixels.length; i++){
            if (ChalkPixel.isPresent(pixels[i]) && ChalkPixel.variant(pixels[i]) !=
                    ChalkVariant.WAXED) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) return false;

        erosionStage = Math.min(erosionStage + 1, ChalkConfig.WATER_STEPS);
        int divisor = ChalkConfig.WATER_STEPS + 1 - erosionStage;
        int toRemove = (int) Math.ceil(candidates.size() / (double) divisor);

        Collections.shuffle(candidates, new Random(random.nextLong()));
        for (int k=0; k < toRemove; k++){
            int index = candidates.get(k);
            pixels[index] = 0;
            if (expiry != null) expiry[index] = 0;
        }
        return true;
    }

    public CompoundTag save(){
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("px", pixels);
        if (expiry != null){
            tag.putLongArray("exp", expiry);
        }
        return tag;
    }

    public static ChalkFace load(CompoundTag tag){
        ChalkFace face = new ChalkFace();
        byte[] px = tag.getByteArray("px");
        System.arraycopy(px, 0, face.pixels, 0, Math.min(px.length, face.expiry.length));

        if (tag.contains("exp", tag.TAG_LONG_ARRAY)){
            long[] ex = tag.getLongArray("exp");
            face.expiry = new long[ChalkPixel.COUNT];
            System.arraycopy(ex, 0, face.expiry, 0, Math.min(ex.length, face.expiry.length));
        }
        return  face;
    }
}


