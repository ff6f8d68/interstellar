package team.nextlevelmodding.lumen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;

public class bigbulbblock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public bigbulbblock() {
        super(Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> 150)
        );
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState pState, net.minecraft.world.level.block.Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, net.minecraft.world.level.block.Mirror pMirror) {
        return pState.setValue(FACING, pMirror.mirror(pState.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (facing == Direction.UP || facing == Direction.DOWN) {
            facing = context.getHorizontalDirection().getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, facing);
    }
}
