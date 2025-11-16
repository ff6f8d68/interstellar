package team.nextlevelmodding.ar2.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.nextlevelmodding.ar2.blocks.GuidanceComputerBlock;
import team.nextlevelmodding.ar2.blocks.TankBlockEntity;

public class UsbItem extends Item {

    public UsbItem() {
        super(new Item.Properties().stacksTo(1));
    }
    @Override
    public String getDescriptionId() {
        return "thumb drive"; // <-- This makes the in-game name literally "linker"
    }
}