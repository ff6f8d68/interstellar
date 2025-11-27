package team.nextlevelmodding.ar2.blocks;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import team.nextlevelmodding.ar2.utils.Thrust;
import team.nextlevelmodding.ar2.MasterCallEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class Test extends Block implements EntityBlock {
    private static final Logger log = LoggerFactory.getLogger(Test.class);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public Test() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite());
    }





    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TestBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {

            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide) return;
        
        boolean isPowered = level.hasNeighborSignal(pos);
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TestBlockEntity testBlock) {


            if (isPowered) {
                Direction facing = state.getValue(FACING);

                Thrust.applyThrust(level, pos, facing, 1000.0);
            }
        }
        
        level.scheduleTick(pos, this, 1);
    }

    @SubscribeEvent
    public void onMasterCall(MasterCallEvent event) {
        Level level = event.getLevel();
        if (level == null || level.isClientSide) return;
        
        BlockPos targetPos = event.getTargetBlock();
        Object eventData = event.getData();



        double thrustAmount = 1000.0; // default
        boolean found = false;

        if (eventData instanceof Number n) {
            thrustAmount = ((Number) eventData).doubleValue();
            found = true;
        } else if (eventData instanceof String s) {
            try {
                thrustAmount = Double.parseDouble(s);
                found = true;
            } catch (NumberFormatException ignored) {
            }
        } else if (eventData != null) {
            String[] candidates = new String[]{"getThrustLevel", "getThrust", "getThrustValue", "getValue", "thrust", "getLevel"};
            for (String methodName : candidates) {
                try {
                    var m = eventData.getClass().getMethod(methodName);
                    Object result = m.invoke(eventData);
                    if (result instanceof Number rn) {
                        thrustAmount = rn.doubleValue();
                        found = true;
                        break;
                    } else if (result instanceof String rs) {
                        try {
                            thrustAmount = Double.parseDouble(rs);
                            found = true;
                            break;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } catch (NoSuchMethodException ignored) {
                    // try next
                } catch (Throwable t) {
                }
            }
        }

        if (!found && eventData != null) {

        }

        if (level.getBlockEntity(targetPos) instanceof TestBlockEntity) {
            BlockState state = level.getBlockState(targetPos);
            Direction facing = state.getValue(FACING);

            Thrust.applyThrust(level, targetPos, facing, thrustAmount);
        }
    }
}
