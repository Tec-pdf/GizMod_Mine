package org.gizmod.chalk;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

//Atributos dos gizes
public enum ChalkVariant implements StringRepresentable {
    NORMAL("normal", null),
    WAXED("waxed", Items.HONEYCOMB),                // não some na água
    PHANTOM("phantom", Items.PHANTOM_MEMBRANE),     // só visível com visão noturna
    TEMPORARY("temporary", Items.BONE_MEAL),        // dura 3 minutos
    GLOWING("glowing", Items.GLOWSTONE_DUST);       // brilha no escuro

    private final String id;
    private final Item upgradeItem;

    ChalkVariant(String id, Item upgradeItem) {
        this.id = id;
        this.upgradeItem = upgradeItem;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    //Item usado no craft para transformar um giz normal nesta variante (null para NORMAL)
    public Item getUpgradeItem() {
        return upgradeItem;
    }

    //Descobre a variante a partir do item no craft, null se o item não serve
    public static ChalkVariant fromUpgradeItem(Item item) {
        for (ChalkVariant variant : values()) {
            if (variant.upgradeItem != null && variant.upgradeItem == item) return variant;
        }
        return null;
    }

    public static ChalkVariant byIndex(int index) {
        ChalkVariant[] all = values();
        return all[Math.floorMod(index, all.length)];
    }
}
