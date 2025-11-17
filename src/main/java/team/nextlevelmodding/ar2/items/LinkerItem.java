package team.nextlevelmodding.ar2.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
        Player player = context.getPlayer();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        CompoundTag tag = stack.getOrCreateTag();

        if (player == null) return InteractionResult.PASS;

        // --- LOGGING UTILITY ---
        java.util.function.Consumer<String> log = (msg) ->
                player.displayClientMessage(Component.literal("§e[Linker] §f" + msg), true);

        if (be != null && be.getBlockState().getBlock() instanceof GuidanceComputerBlock) {
            // Set as master
            tag.putLong("MasterPos", pos.asLong());
            tag.remove("ChildPos");

            log.accept("Master set at: " + pos);

            return InteractionResult.SUCCESS;
        }
        else if (be instanceof TankBlockEntity || isThruster(be) || isTestBlock(be)) {

            if (!tag.contains("MasterPos")) {
                log.accept("No master selected! Right-click a Guidance Computer first.");
                return InteractionResult.SUCCESS;
            }

            BlockPos masterPos = BlockPos.of(tag.getLong("MasterPos"));
            BlockEntity masterBe = level.getBlockEntity(masterPos);

            if (masterBe == null || !(masterBe.getBlockState().getBlock() instanceof GuidanceComputerBlock masterBlock)) {
                log.accept("Stored master is missing or invalid.");
                return InteractionResult.SUCCESS;
            }

            log.accept("Linking child block at: " + pos);
            tag.putLong("ChildPos", pos.asLong());

            masterBlock.addChild(pos);

            log.accept("Child successfully linked to master at: " + masterPos);

            return InteractionResult.SUCCESS;
        }

        log.accept("This block cannot be linked.");
        return InteractionResult.PASS;
    }

    private boolean isThruster(BlockEntity be) {
        if (be == null) return false; // <-- Prevent crash
        return be.getBlockState().getBlock().getClass().getSimpleName().toLowerCase().contains("rocketmotor");
    }

    private boolean isTestBlock(BlockEntity be) {
        if (be == null) return false; // <-- Prevent crash
        return be.getBlockState().getBlock() instanceof team.nextlevelmodding.ar2.blocks.Test;
    }


    @Override
    public String getDescriptionId() {
        return "linker";
    }
}
