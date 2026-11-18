package org.gizmod.chalk;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public record FaceKey(long pos, Direction dir) {

    public static FaceKey of(BlockPos pos, Direction dir){
        return new FaceKey(pos.asLong(), dir);
    }

    public BlockPos blockPos(){
        return BlockPos.of(pos);
    }

    public long chunk(){
        return ChunkPos.asLong(blockPos());
    }
}
