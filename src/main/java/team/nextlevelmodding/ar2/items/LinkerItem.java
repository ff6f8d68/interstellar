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
import team.nextlevelmodding.ar2.utils.BlockInfoFormatter;

import java.util.function.Consumer;
import net.minecraft.world.InteractionResultHolder;

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

        // --- SHIFT+RIGHT-CLICK ON GUIDANCE COMPUTER TO LIST PARTS ---
        if (player.isShiftKeyDown() && be instanceof GuidanceComputerBlockEntity coreEntity) {
            var children = coreEntity.getChildren();
            if (children.isEmpty()) {
                log.accept("Core at " + BlockInfoFormatter.formatBlockInfo(level, pos) + " has no linked parts.");
            } else {
                log.accept("§6Parts linked to Core at " + BlockInfoFormatter.formatBlockInfo(level, pos) + ":");
                for (BlockPos childPos : children) {
                    String childInfo = BlockInfoFormatter.formatBlockInfo(level, childPos);
                    String facingInfo = getFacingInfo(level, childPos);
                   log.accept("  §7- " + childInfo + facingInfo);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // --- CORE BLOCK DETECTION ---
        if (be instanceof GuidanceComputerBlockEntity) {
            tag.putLong("CorePos", pos.asLong());
            tag.remove("PartPos");
            log.accept("Core set at: " + BlockInfoFormatter.formatBlockInfo(level, pos));
            return InteractionResult.SUCCESS;
        }

        // --- PART BLOCK DETECTION ---
        boolean isPart = be instanceof TankBlockEntity || isThruster(level, pos) || isTestBlock(level, pos);
        if (isPart) {
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
                tag.remove("CorePos");
                return InteractionResult.SUCCESS;
            }

            tag.putLong("PartPos", pos.asLong());
            coreEntity.addChild(pos);
            level.blockEntityChanged(corePos); // Force update to sync to clients
            String partInfo = BlockInfoFormatter.formatBlockInfo(level, pos);
            log.accept("§aSuccessfully linked " + partInfo + " to Core!");
            return InteractionResult.SUCCESS;
        }

        // If block is neither Core nor recognized Part, silently ignore
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide() || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        // --- SHIFT+RIGHT-CLICK IN AIR TO RESET LINKER ---
        CompoundTag tag = stack.getOrCreateTag();
        boolean hadCore = tag.contains("CorePos");

        tag.remove("CorePos");
        tag.remove("PartPos");

        if (hadCore) {
            player.sendSystemMessage(Component.literal("§e[Linker] §fLinker reset. Ready to link a new Core."));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
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

    // Gets the facing direction info for a block if it has a FACING property
    private String getFacingInfo(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        try {
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
                var facing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
                return " §b[Facing: " + facing.getName().toUpperCase() + "]";
            } else if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                var facing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
                return " §b[Facing: " + facing.getName().toUpperCase() + "]";
            }
        } catch (Exception e) {
            // Block doesn't have a facing property
        }
        return "";
    }

    @Override
    public String getDescriptionId() {
        return "linker";
    }
}
