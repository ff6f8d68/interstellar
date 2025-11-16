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

public class LinkerItem extends Item {

    public LinkerItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        CompoundTag tag = stack.getOrCreateTag();

        if (be != null && be.getBlockState().getBlock() instanceof GuidanceComputerBlock) {
            // Set as master
            tag.putLong("MasterPos", pos.asLong());
            tag.remove("ChildPos");
            return InteractionResult.SUCCESS;
        } else if (be instanceof TankBlockEntity || isThruster(be) || isTestBlock(be)) {
            // Link as child
            if (tag.contains("MasterPos")) {
                BlockPos masterPos = BlockPos.of(tag.getLong("MasterPos"));
                BlockEntity masterBe = level.getBlockEntity(masterPos);
                if (masterBe != null && masterBe.getBlockState().getBlock() instanceof GuidanceComputerBlock masterBlock) {
                    tag.putLong("ChildPos", pos.asLong());
                    ((GuidanceComputerBlock) masterBlock).addChild(pos);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    private boolean isThruster(BlockEntity be) {
        // Check if block is a thruster (Rocketmotor, etc.)
        return be.getBlockState().getBlock().getClass().getSimpleName().contains("rocketmotor");
    }

    private boolean isTestBlock(BlockEntity be) {
        return be.getBlockState().getBlock() instanceof team.nextlevelmodding.ar2.blocks.Test;
    }
    @Override
    public String getDescriptionId() {
        return "linker"; // <-- This makes the in-game name literally "linker"
    }
}