package io.delilaheve.simplegraves;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketEnums.DropRule;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class TrinketDropHandler implements TrinketDropCallback {
    @Override
    public DropRule drop(
        DropRule rule,
        ItemStack stack,
        SlotReference ref,
        LivingEntity entity
    ) {
        World world = entity.getEntityWorld();
        if (!world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            // If inventory is not kept on death, trinkets are placed into the grave
            // Since trinkets can't be removed in the player death event, we tell
            // Trinkets to destroy them, otherwise we duplicate the items
            return DropRule.DESTROY;
        } else return DropRule.DEFAULT;
    }
}
