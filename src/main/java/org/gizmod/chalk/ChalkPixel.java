package org.gizmod.chalk;

import net.minecraft.world.item.DyeColor;

// Cada pixel cabe em 1 byte: [1 bit "tem tinta"][3 bits variante][4 bits cor]
public final class ChalkPixel {
    private ChalkPixel() {}

    public static final int SIZE = 16;              // 16 x 16 pixels por face
    public static final int COUNT = SIZE * SIZE;

    public static byte encode(DyeColor color, ChalkVariant variant) {
        return (byte) (0x80 | (variant.ordinal() << 4) | color.getId());
    }

    public static boolean isPresent(byte b) {
        return (b & 0x80) != 0;
    }

    public static DyeColor color(byte b) {
        return DyeColor.byId(b & 0x0F);
    }

    public static ChalkVariant variant(byte b) {
        return ChalkVariant.byIndex((b >> 4) & 0x07);
    }

    // Cor RGB usada para pintar (a mesma dos fogos de artifício de cada corante)
    public static int rgb(DyeColor color) {
        return color.getFireworkColor();
    }
}
