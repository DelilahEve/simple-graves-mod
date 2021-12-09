package io.delilaheve.simplegraves.registry;

import io.delilaheve.simplegraves.SimpleGravestones;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

    public static final GravestoneBlock GRAVE_BLOCK = new GravestoneBlock(
        FabricBlockSettings.of(Material.METAL)
            .sounds(BlockSoundGroup.METAL)
            .luminance(3)
            .strength(0.2f,999f)
    );

    public static final BlockEntityType<GravestoneBlockEntity> GRAVE_BLOCK_ENTITY;

    static {
        GRAVE_BLOCK_ENTITY = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            SimpleGravestones.GRAVE,
            FabricBlockEntityTypeBuilder.create(GravestoneBlockEntity::new, GRAVE_BLOCK)
                .build(null)
        );
    }

    public static void registerBlocks(){
        Registry.register(
            Registry.BLOCK,
            new Identifier(SimpleGravestones.MOD_ID, "gravestone_block"),
            GRAVE_BLOCK
        );
    }

}
