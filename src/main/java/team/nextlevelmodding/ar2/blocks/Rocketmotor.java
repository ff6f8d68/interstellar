package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.nextlevelmodding.ar2.MasterCallEvent;
import team.nextlevelmodding.ar2.utils.Thrust;

public class Rocketmotor extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final double MAX_THRUST = 1000.0; // Max thrust
    private static final Logger LOGGER = LogManager.getLogger();

    public Rocketmotor() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));

        // Register on the Forge event bus so this block can receive MasterCallEvent
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (facing == Direction.UP || facing == Direction.DOWN) {
            facing = context.getHorizontalDirection().getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @SubscribeEvent
    public void onMasterCall(MasterCallEvent event) {
        if (event == null || event.getLevel() == null || event.getLevel().isClientSide()) return;

        try {
            Object payload = event.getData();
            double thrustValue = 0.0;
            boolean hasThrust = false;

            // Direct numeric payload
            if (payload instanceof Number n) {
                thrustValue = n.doubleValue();
                hasThrust = true;
            }
            // String that can be parsed as number
            else if (payload instanceof String s) {
                try {
                    thrustValue = Double.parseDouble(s);
                    hasThrust = true;
                } catch (NumberFormatException ignored) {
                }
            }
            // Try common getter methods via reflection (e.g. getThrustLevel(), getThrust())
            else if (payload != null) {
                String[] candidates = new String[]{"getThrustLevel", "getThrust", "getThrustValue", "getValue", "thrust", "getLevel"};
                for (String methodName : candidates) {
                    try {
                        var m = payload.getClass().getMethod(methodName);
                        if (m != null) {
                            Object result = m.invoke(payload);
                            if (result instanceof Number rn) {
                                thrustValue = rn.doubleValue();
                                hasThrust = true;
                                break;
                            } else if (result instanceof String rs) {
                                try {
                                    thrustValue = Double.parseDouble(rs);
                                    hasThrust = true;
                                    break;
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    } catch (NoSuchMethodException ignored) {
                        // try next
                    }
                }
            }

            if (!hasThrust) return; // nothing actionable

            double newThrust = Math.min(thrustValue, MAX_THRUST);
            BlockPos pos = event.getTargetBlock();
            Level level = event.getLevel();

            if (level.getBlockEntity(pos) instanceof RocketMotorBlockEntity motor) {
                motor.setCurrentThrust(newThrust);

                if (newThrust > 0) {
                    Thrust.applyThrust(level, pos, newThrust);
                    LOGGER.info("Applied thrust of {} at {}", newThrust, pos);
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Error in Rocketmotor.onMasterCall", e);
        }
    }
}
