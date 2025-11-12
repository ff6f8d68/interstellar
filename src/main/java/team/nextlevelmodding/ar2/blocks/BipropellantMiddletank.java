package team.nextlevelmodding.ar2.blocks;

import team.nextlevelmodding.ar2.ModBlocks;
import team.nextlevelmodding.ar2.TankParser;
import team.nextlevelmodding.ar2.fluids.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BipropellantMiddletank extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BipropellantMiddletank() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new TankBlockEntity(ModBlocks.TANK.get(), pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return null;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide()) {
            TankParser.updateTank(world, pos, ModBlocks.BIPROPELLANT_TOPTANK.get(), ModBlocks.BIPROPELLANT_MIDDLETANK.get(), ModBlocks.BIPROPELLANT_BOTTOMTANK.get());
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!world.isClientSide()) {
            TankParser.updateTank(world, pos, ModBlocks.BIPROPELLANT_TOPTANK.get(), ModBlocks.BIPROPELLANT_MIDDLETANK.get(), ModBlocks.BIPROPELLANT_BOTTOMTANK.get());
        }
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
    public VoxelShape getCollisionShape(@NotNull BlockState pState, @NotNull net.minecraft.world.level.BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return net.minecraft.world.phys.shapes.Shapes.block();
    }

    @Override
    public VoxelShape getShape(@NotNull BlockState pState, @NotNull net.minecraft.world.level.BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return net.minecraft.world.phys.shapes.Shapes.block();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof TankBlockEntity tankEntity) {
                // Could add item dropping logic here if needed
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @SuppressWarnings("deprecation")
    @Override
    public net.minecraft.world.InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        BlockEntity be = pLevel.getBlockEntity(pPos);

        if (!(be instanceof TankBlockEntity tankEntity)) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        // Handle empty bucket - fill with fluid from tank
        if (itemStack.is(net.minecraft.world.item.Items.BUCKET)) {
            FluidStack fluid = tankEntity.getFluid();
            if (!fluid.isEmpty() && fluid.getAmount() >= 1000) {
                ItemStack resultBucket = null;

                if (fluid.getFluid() == ModFluids.SOURCE_ROCKET_FUEL.get()) {
                    resultBucket = new ItemStack(ModBlocks.ROCKET_FUEL_BUCKET.get());
                } else if (fluid.getFluid() == ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL.get()) {
                    resultBucket = new ItemStack(ModBlocks.BIPROPELLANT_ROCKET_FUEL_BUCKET.get());
                }

                if (resultBucket != null) {
                    tankEntity.drain(1000, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    itemStack.shrink(1);
                    if (itemStack.isEmpty()) {
                        pPlayer.setItemInHand(pHand, resultBucket);
                    } else {
                        pPlayer.addItem(resultBucket);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }

        // Handle fuel buckets - empty into tank
        if (itemStack.is(ModBlocks.ROCKET_FUEL_BUCKET.get())) {
            int filled = tankEntity.fill(new FluidStack(ModFluids.SOURCE_ROCKET_FUEL.get(), 1000), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
                itemStack.shrink(1);
                if (itemStack.isEmpty()) {
                    pPlayer.setItemInHand(pHand, emptyBucket);
                } else {
                    pPlayer.addItem(emptyBucket);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        } else if (itemStack.is(ModBlocks.BIPROPELLANT_ROCKET_FUEL_BUCKET.get())) {
            int filled = tankEntity.fill(new FluidStack(ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL.get(), 1000), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
                itemStack.shrink(1);
                if (itemStack.isEmpty()) {
                    pPlayer.setItemInHand(pHand, emptyBucket);
                } else {
                    pPlayer.addItem(emptyBucket);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }

        return net.minecraft.world.InteractionResult.PASS;
    }

}