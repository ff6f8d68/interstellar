package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import team.nextlevelmodding.ar2.utils.Thrust;
import team.nextlevelmodding.ar2.ModBlocks;

public class TestBlockEntity extends BlockEntity {

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TEST_BLOCK_ENTITY.get(), pos, state); // replace with your registered BlockEntityType
    }

    // Tick method called by the BlockEntityTicker
    public void tick() {
        Level world = getLevel();
        if (world != null) {
            // Apply downward thrust with a fixed value
            Thrust.applyThrust(world, getBlockPos(), 1000.0);
        }
    }
}
