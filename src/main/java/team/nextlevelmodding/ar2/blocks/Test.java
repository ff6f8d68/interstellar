package team.nextlevelmodding.ar2.blocks;

import team.nextlevelmodding.ar2.utils.Thrust;
import team.nextlevelmodding.ar2.MasterCallEvent;
import team.nextlevelmodding.ar2.data.TestData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class Test extends Block implements EntityBlock {
    private static final Logger log = LoggerFactory.getLogger(Test.class);

    public Test() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
        MinecraftForge.EVENT_BUS.register(this);
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
            log.info("[Test] Block placed at {} in world {}", pos, level.dimension().location());
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
            if (isPowered != testBlock.wasPowered) {
                log.info("[Test] Redstone state changed at {}: {} -> {}", 
                        pos, testBlock.wasPowered ? "POWERED" : "UNPOWERED", 
                        isPowered ? "POWERED" : "UNPOWERED");
                testBlock.wasPowered = isPowered;
            }

            if (isPowered) {
                log.debug("[Test] Applying thrust at {} with power 1000.0", pos);
                Thrust.applyThrust(level, pos, 1000.0);
            }
        }
        
        level.scheduleTick(pos, this, 1);
    }

    @SubscribeEvent
    public static void onMasterCall(MasterCallEvent event) {
        Level level = event.getLevel();
        if (level == null || level.isClientSide) return;
        
        BlockPos targetPos = event.getTargetBlock();
        Object eventData = event.getData();
        
        log.info("[Test] Received MasterCallEvent at {} with data: {}", targetPos, eventData);
        
        if (eventData instanceof TestData) {
            log.info("[Test] Processing TestData at {}", targetPos);
            if (level.getBlockEntity(targetPos) instanceof TestBlockEntity) {
                log.info("[Test] Applying thrust from MasterCallEvent at {}", targetPos);
                Thrust.applyThrust(level, targetPos, 1000.0);
            }
        }
    }
}
