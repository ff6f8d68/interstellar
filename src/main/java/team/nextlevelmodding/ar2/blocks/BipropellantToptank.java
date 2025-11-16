package team.nextlevelmodding.ar2.blocks;

import team.nextlevelmodding.ar2.ModBlocks;
import team.nextlevelmodding.ar2.TankParser;
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

public class BipropellantToptank extends Block implements EntityBlock {


    public BipropellantToptank() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );

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

        // Handle empty buckets - fill with fluid from tank
        if (itemStack.is(net.minecraft.world.item.Items.BUCKET)) {
            FluidStack tankFluid = tankEntity.getFluid();
            if (!tankFluid.isEmpty() && tankFluid.getAmount() >= 1000) {
                // Get the bucket item for this fluid from the fluid's properties
                ItemStack resultBucket = net.minecraftforge.fluids.FluidUtil.getFilledBucket(tankFluid);
                
                if (!resultBucket.isEmpty()) {
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
        
        // Handle filled buckets - empty into tank using Fluid API
        if (itemStack.getItem() instanceof net.minecraft.world.item.BucketItem bucketItem) {
            net.minecraft.world.level.material.Fluid bucketFluid = bucketItem.getFluid();
            if (bucketFluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            
            FluidStack fluidToFill = new FluidStack(bucketFluid, 1000);
            int filled = tankEntity.fill(fluidToFill, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            
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