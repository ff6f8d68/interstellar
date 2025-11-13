package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.nextlevelmodding.ar2.MasterCallEvent;
import team.nextlevelmodding.ar2.data.ThrustData;
import team.nextlevelmodding.ar2.utils.Thrust;

public class Rocketmotor extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private double currentThrustLevel = 0.0;
    private static final double MAX_THRUST = 1000.0; // Define max thrust for this thruster

    public Rocketmotor() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, net.minecraft.core.Direction.NORTH));
        MinecraftForge.EVENT_BUS.register(this);
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

    @SubscribeEvent
    public void onMasterCall(MasterCallEvent event) {
        if (event.getData() instanceof ThrustData thrustData) {
            // Set thrust level, capped by max thrust
            currentThrustLevel = Math.min(thrustData.getThrustLevel(), MAX_THRUST);
            // Apply thrust if in world
            if (event.getTargetBlock() != null) {
                Level level = event.getTargetBlock().getClass() == BlockPos.class ? null : null; // Need to get level from somewhere
                // Thrust.applyThrust(level, event.getTargetBlock(), currentThrustLevel);
            }
        }
    }
}
