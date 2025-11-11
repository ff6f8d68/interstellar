package team.nextlevelmodding.ar2.blocks;

import team.nextlevelmodding.ar2.utils.Thrust;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test extends Block {

    private static final Logger log = LoggerFactory.getLogger(Test.class);

    public Test() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }
    @Override
    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
        world.scheduleTick(pos, this, 1);   // start ticking loop
    }


    // Create BlockEntity when this block is placed
    @Override
    public void tick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(blockstate, world, pos, random);

        // apply thrust every tick
        Thrust.applyThrust(world, pos, 1000.0);


        // schedule next tick (1 tick later)
        world.scheduleTick(pos, this, 1);
    }



}
