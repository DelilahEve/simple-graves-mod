package io.delilaheve.simplegraves.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import io.delilaheve.simplegraves.SimpleGravestones;
import io.delilaheve.simplegraves.registry.GravestoneBlock;
import io.delilaheve.simplegraves.registry.GravestoneBlockEntity;
import io.delilaheve.simplegraves.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin{

    /**
     * Handle player death event
     *
     * @param source the source of the lethal damage
     * @param info event info
     */
    @Inject(
        method = "drop",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    @SuppressWarnings("ConstantConditions")
    public void placeGrave(DamageSource source, CallbackInfo info) {
        if (((Object) this) instanceof ServerPlayerEntity player) {
            System.out.println("DEBUG - drop() of player is called");

            // Get position of player
            World thisWorld = player.getEntityWorld();
            BlockPos blockPos = player.getBlockPos();

            if (!thisWorld.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
                // Get BlockState of a grave block
                BlockState blockState = ModBlocks.GRAVE_BLOCK.getDefaultState()
                    .with(GravestoneBlock.FACING, player.getHorizontalFacing().getOpposite());

                // Figure out where to put the grave
                blockPos = findBestSpot(thisWorld, blockPos);
                blockPos = addDirtBlock(thisWorld, blockPos);

                // Spawn the grave
                thisWorld.setBlockState(blockPos, blockState, Block.NOTIFY_ALL);

                // Store all trinkets and inventory items
                List<ItemStack> extras = new ArrayList<>();
                // ToDo: This didn't work, needs reassessing
//                TrinketsApi.getTrinketComponent(player)
//                    .ifPresent(
//                        component -> component.getAllEquipped().forEach(
//                            pair -> extras.add(pair.getRight())
//                        )
//                    );
                swapInventory(player, extras, (Inventory) thisWorld.getBlockEntity(blockPos));

                // Create entity to store experience and player info
                GravestoneBlockEntity graveEntity = (GravestoneBlockEntity) player.getEntityWorld()
                    .getBlockEntity(blockPos);
                graveEntity.setExperience(player.totalExperience);
                graveEntity.setPlayerName(player.getName().asString());

                // Clear player info
                player.setExperienceLevel(0);
                player.setExperiencePoints(0);
                player.getInventory().clear();
            }

            if (!thisWorld.isClient) {
                player.sendMessage(
                    new LiteralText("Your grave is at: " + blockPos.toShortString()),
                    false
                );
            }

        }
    }

    /**
     * Place player's inventory into the grave
     *
     * @param player player to take inventory from
     * @param targetInv inventory to put items in
     */
    private void swapInventory(PlayerEntity player, List<ItemStack> extras, Inventory targetInv){
        int i = 0;
        int playerInvSize = player.getInventory().size();
        while (i < playerInvSize && i < targetInv.size()) {
            targetInv.setStack(i, player.getInventory().getStack(i));
            i++;
        }
        while (i < (playerInvSize + extras.size()) && i < targetInv.size()) {
            targetInv.setStack(i, extras.get(i));
            i++;
        }
    }

    /**
     * Locate the best possible position to place a gravestone within configured bounds
     *
     * @param world world to find
     * @param currentPos position to begin search from
     *
     * @return the best possible position to spawn a grave at
     */
    public BlockPos findBestSpot(World world, BlockPos currentPos){
        // Save initial position
        BlockPos initialPos = currentPos;
        // Store search radius
        int radius = SimpleGravestones.getSearchRadius();

        // If position is below the bottom of the world, put it just above
        if (currentPos.getY() <= world.getBottomY()){
            currentPos = currentPos.withY(world.getBottomY() + 1);
        }

        // Try to find a nearby position
        while (!isValid(world.getBlockState(currentPos)) && world.isInBuildLimit(currentPos)){
            // Try to find a valid nearby location
            currentPos = checkNearby(world, currentPos, radius);
            // If one was not found, move the position up 1 Y level
            if (!isValid(world.getBlockState(currentPos))) currentPos = currentPos.up();
        }
        // If we couldn't find empty space while checking all spaces above
        if (!world.isInBuildLimit(currentPos)){
            // If the initial position was good enough, just use that
            if (world.isInBuildLimit(initialPos)){
                return initialPos;
            }
            // Set to the top Y position
            // If that's not in build limit, move downward until a spot is found
            if (currentPos.getY() >= world.getTopY()){
                currentPos = currentPos.withY(world.getTopY() - 1);
            }
            while (!isValid(world.getBlockState(currentPos)) && world.isInBuildLimit(currentPos)){
                currentPos = currentPos.down();
            }
            // If it STILL couldn't find an empty space, just set the y value to something arbitrary
            if (!world.isInBuildLimit(currentPos)){
                return initialPos.withY(SimpleGravestones.getFallbackHeight());
            }
        }
        return currentPos;
    }

    /**
     * Check for a valid nearby block horizontally
     *
     * @param origin original location to search around
     * @param radius maximum number of blocks out to search
     *
     * @return valid block position or origin
     */
    private BlockPos checkNearby(World world, BlockPos origin, int radius) {
        BlockPos checkingPos = origin;
        List<BlockPos> potentialBlocks = new ArrayList<>();
        // Move position to southeast corner of radius
        checkingPos.south(radius);
        checkingPos.east(radius);
        int traverse = radius * 2;
        // Add all valid blocks in range to the list of potential locations
        int i = 0;
        int j = 0;
        while (i < traverse) {
            while (j < traverse) {
                if (isValid(world.getBlockState(checkingPos))) potentialBlocks.add(checkingPos);
                j++;
                checkingPos = checkingPos.north();
            }
            checkingPos = checkingPos.west();
            i++;
        }
        // Sort potential locations by distance from origin
        potentialBlocks.sort((a, b) -> (int) (
            a.getSquaredDistance(origin, true) - b.getSquaredDistance(origin, true)
        ));
        // Collections.reverse(potentialBlocks);
        if (potentialBlocks.isEmpty()) {
            // No valid location found on this Y level, return the origin so that we can try one block higher
            return origin;
        } else {
            // Valid location found, use it
            return potentialBlocks.get(0);
        }
    }

    /**
     * Replace block below the given position with dirt
     *
     * @param world world to replace the block in
     * @param currentPos position to replace the block below at
     *
     * @return block position given
     */
    private BlockPos addDirtBlock(World world, BlockPos currentPos){
        // For checking right below the player
        BlockPos blockPosBelow = currentPos.down();
        BlockState blockStateBelow = world.getBlockState(blockPosBelow);
        if (isValid(blockStateBelow)) {
            world.setBlockState(blockPosBelow, Blocks.DIRT.getDefaultState(), Block.NOTIFY_ALL);
        }
        return currentPos;
    }

    /**
     * Check if block state is valid target for replacement
     *
     * @param blockState Block state to check
     *
     * @return true if valid for replacement
     */
    private boolean isValid(BlockState blockState){
        // Check if grave can replace any block
        boolean replaceAny = SimpleGravestones.getReplaceAny();
        if (replaceAny) return true;
        // Can't replace any, build list of replace blocks
        List<Block> blockList = new ArrayList<>();
        for (String name: SimpleGravestones.getReplaceBlocks()) {
            Block block = Registry.BLOCK.get(new Identifier(name));
            if (!blockList.contains(block)) {
                blockList.add(block);
            }
        }
        // Return valid based on list mode
        SimpleGravestones.ListModes mode = SimpleGravestones.getListMode();
        return switch (mode) {
            case WHITELIST -> blockList.contains(blockState.getBlock());
            case BLACKLIST -> !blockList.contains(blockState.getBlock());
        };
    }

}
