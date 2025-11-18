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
import team.nextlevelmodding.ar2.blocks.GuidanceComputerBlockEntity;
import team.nextlevelmodding.ar2.blocks.TankBlockEntity;

import team.nextlevelmodding.ar2.blocks.Test;

import java.util.function.Consumer;

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

        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        CompoundTag tag = stack.getOrCreateTag();

        // --- CHAT LOGGING UTILITY ---
        Consumer<String> log = msg ->
                player.sendSystemMessage(Component.literal("§e[Linker] §f" + msg));

        // --- CORE BLOCK DETECTION ---
        if (be instanceof GuidanceComputerBlockEntity) {
            tag.putLong("CorePos", pos.asLong());
            tag.remove("PartPos");
            log.accept("Core set at: " + pos);
            return InteractionResult.SUCCESS;
        }

        // --- PART BLOCK DETECTION ---
        boolean isPart = be instanceof TankBlockEntity || isThruster(level, pos) || isTestBlock(level, pos)

;        if (isPart) {
            if (!tag.contains("CorePos")) {
                // User clicked part before setting Core
                if (isThruster(level, pos)) log.accept("Cannot link thruster: no Core selected.");
                else if (isTestBlock(level, pos)) log.accept("Cannot link test block: no Core selected.");
                else log.accept("Cannot link this part: no Core selected.");
                return InteractionResult.SUCCESS;
            }

            BlockPos corePos = BlockPos.of(tag.getLong("CorePos"));
            BlockEntity coreBe = level.getBlockEntity(corePos);

            if (!(coreBe instanceof GuidanceComputerBlockEntity coreEntity)) {
                log.accept("Stored Core is missing or invalid.");
                return InteractionResult.SUCCESS;
            }

            tag.putLong("PartPos", pos.asLong());
            coreEntity.addChild(pos);
            log.accept("Part linked to Core at: " + corePos);
            return InteractionResult.SUCCESS;
        }

        // If block is neither Core nor recognized Part, silently ignore
        return InteractionResult.PASS;
    }

    // Detects if block is a thruster
    // Detects if the clicked block is a thruster (no BE required)
    private boolean isThruster(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock().getClass().getSimpleName().toLowerCase().contains("rocketmotor");
    }

    // Detects if the clicked block is a Test block (no BE required)
    private boolean isTestBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof Test;
    }


    @Override
    public String getDescriptionId() {
        return "linker";
    }
}
