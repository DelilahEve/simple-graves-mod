package io.delilaheve.simplegraves.registry;

import io.delilaheve.simplegraves.SimpleGravestones;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {

    public static final BlockItem GRAVE_BLOCK = new BlockItem(
        ModBlocks.GRAVE_BLOCK,
        new Item.Settings().group(ItemGroup.DECORATIONS)
    );

    public static void registerItems() {
        Registry.register(
            Registry.ITEM,
            new Identifier(SimpleGravestones.MOD_ID, "gravestone_block"),
            GRAVE_BLOCK
        );
    }
}
