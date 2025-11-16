package team.nextlevelmodding.ar2.blocks;

import team.nextlevelmodding.ar2.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NuclearGenerator extends Block implements EntityBlock {


    public NuclearGenerator() {
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
        return new NuclearGeneratorBlockEntity(pPos, pState);
    }






    @SuppressWarnings("deprecation")
    @Override
    public net.minecraft.world.InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        ItemStack itemStack = pPlayer.getItemInHand(pHand);

        // Handle nuclear fuel bucket
        if (itemStack.is(ModBlocks.ROCKET_FUEL_BUCKET.get())) {
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

        // Open GUI
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof MenuProvider menuProvider) {
            pPlayer.openMenu(menuProvider);
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        return net.minecraft.world.InteractionResult.PASS;
    }
}