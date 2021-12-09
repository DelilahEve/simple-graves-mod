package io.delilaheve.simplegraves.registry;

import io.delilaheve.simplegraves.SimpleGravestones;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class GravestoneBlockEntity extends BlockEntity implements ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(
        SimpleGravestones.getGraveSlots(),
        ItemStack.EMPTY
    );
    private int experience = 0;
    private String playerName = "null";

    public GravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GRAVE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        experience = nbt.getInt("Experience");
        playerName = nbt.getString("PlayerName");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("Experience", experience);
        nbt.putString("PlayerName", playerName);
        return nbt;
    }

    public void setExperience(int exp){
        experience = exp;
    }

    public int getExperience(){
        return experience;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

}
