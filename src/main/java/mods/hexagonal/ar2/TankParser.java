package mods.hexagonal.ar2;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

/**
 * TankParser handles tank block configuration and connectivity logic.
 * Fluid storage and capability handling is now managed by TankBlockEntity through Forge capabilities.
 */
public class TankParser {

    private static final Set<Block> CONNECTABLE_BLOCKS = new HashSet<>();

    /**
     * Register a block that can connect to tank networks
     */
    public static void registerConnectable(Block block) {
        CONNECTABLE_BLOCKS.add(block);
    }

    /**
     * Check if a block can connect to tank networks
     */
    public static boolean isConnectable(Block block) {
        return CONNECTABLE_BLOCKS.contains(block);
    }

    /**
     * Update tank block type (top/middle/bottom) based on neighboring tanks.
     * Tank blocks automatically update their appearance based on what's above/below them.
     */
    public static void updateTank(Level world, BlockPos pos, Block toptank, Block middletank, Block bottomtank) {
        if (world.isClientSide()) {
            return;
        }

        boolean hasAbove = isConnectable(world.getBlockState(pos.above()).getBlock())
                || isTankBlock(world.getBlockState(pos.above()).getBlock(), toptank, middletank, bottomtank);
        boolean hasBelow = isConnectable(world.getBlockState(pos.below()).getBlock())
                || isTankBlock(world.getBlockState(pos.below()).getBlock(), toptank, middletank, bottomtank);

        Block newBlock;
        if (hasAbove && hasBelow) {
            newBlock = middletank;
        } else if (hasAbove) {
            newBlock = bottomtank;
        } else if (hasBelow) {
            newBlock = toptank;
        } else {
            newBlock = toptank;
        }

        if (world.getBlockState(pos).getBlock() != newBlock) {
            world.setBlock(pos, newBlock.defaultBlockState(), 3);
        }
    }

    /**
     * Check if a block is one of the tank variants
     */
    private static boolean isTankBlock(Block block, Block toptank, Block middletank, Block bottomtank) {
        return block == toptank || block == middletank || block == bottomtank;
    }

    /**
     * Recursively find all connected tank blocks above and below a position.
     * Used for calculating total tank capacity and connectivity.
     */
    public static Set<BlockPos> getConnectedBlocks(Level world, BlockPos pos) {
        Set<BlockPos> connected = new HashSet<>();
        explore(world, pos, connected);
        return connected;
    }

    /**
     * Recursive helper to explore connected tanks
     */
    private static void explore(Level world, BlockPos pos, Set<BlockPos> visited) {
        if (visited.contains(pos)) {
            return;
        }

        Block block = world.getBlockState(pos).getBlock();
        if (!isConnectable(block) && !(block instanceof mods.hexagonal.ar2.blocks.Toptank) 
            && !(block instanceof mods.hexagonal.ar2.blocks.Middletank)
            && !(block instanceof mods.hexagonal.ar2.blocks.Bottomtank)
            && !(block instanceof mods.hexagonal.ar2.blocks.BipropellantTank)) {
            return;
        }

        visited.add(pos);

        // explore neighbors (up/down only for vertical tank stacks)
        explore(world, pos.above(), visited);
        explore(world, pos.below(), visited);
    }
}
