package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import team.nextlevelmodding.ar2.utils.Thrust;
import team.nextlevelmodding.ar2.ModBlocks;
import org.jetbrains.annotations.NotNull;

public class TestBlockEntity extends BlockEntity {
    public boolean wasPowered = false;
    private int tickCounter = 0;

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TEST_BLOCK_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level.isClientSide() || !(blockEntity instanceof TestBlockEntity testBE)) {
            return;
        }

        // Update powered state
        boolean isPowered = level.hasNeighborSignal(pos);
        boolean stateChanged = isPowered != testBE.wasPowered;
        
        if (stateChanged) {
            testBE.wasPowered = isPowered;
            // State changed - mark block for update to sync to client if needed
            level.sendBlockUpdated(pos, state, state, 3);
        }

        // Only process thrust on server side and if powered
        if (isPowered) {
            testBE.tickCounter++;
            // Apply thrust every tick (you might want to adjust this)
            Thrust.applyThrust(level, pos, 1000.0);
            
            // Example: Do something every second (20 ticks)
            if (testBE.tickCounter % 20 == 0) {
                // Your periodic logic here
            }
        }
    }
}
