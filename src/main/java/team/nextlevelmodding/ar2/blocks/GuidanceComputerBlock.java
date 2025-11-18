package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GuidanceComputerBlock extends Block implements EntityBlock {

    public GuidanceComputerBlock() {
        super(Properties.of()
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GuidanceComputerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (lvl, pos, st, be) -> {
            if (be instanceof GuidanceComputerBlockEntity computer) {
                GuidanceComputerBlockEntity.tick(lvl, pos, st, computer);
            }
        };
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!oldState.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof GuidanceComputerBlockEntity gc) {
                gc.onRemoved();
            }
        }
        super.onRemove(oldState, level, pos, newState, moved);
    }
}
