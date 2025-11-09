package mods.hexagonal.ar2.blocks;

import mods.hexagonal.ar2.ModBlocks;
import mods.hexagonal.ar2.fluids.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class NuclearGenerator extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public NuclearGenerator() {
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

    @Override
    public BlockState rotate(BlockState pState, net.minecraft.world.level.block.Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, net.minecraft.world.level.block.Mirror pMirror) {
        return pState.setValue(FACING, pMirror.mirror(pState.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public net.minecraft.world.InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        ItemStack itemStack = pPlayer.getItemInHand(pHand);

        // Handle nuclear fuel bucket
        if (itemStack.is(ModBlocks.NUCLEAR_ROCKET_FUEL_BUCKET.get())) {
            // For now, just consume the fuel
            // In a real implementation, you would store fuel and generate power
            ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
            itemStack.shrink(1);
            if (itemStack.isEmpty()) {
                pPlayer.setItemInHand(pHand, emptyBucket);
            } else {
                pPlayer.addItem(emptyBucket);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        return net.minecraft.world.InteractionResult.PASS;
    }
}