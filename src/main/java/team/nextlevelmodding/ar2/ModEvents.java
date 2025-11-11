package team.nextlevelmodding.ar2;

import team.nextlevelmodding.ar2.blocks.TankBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ModEvents {

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();

        if (level.isClientSide()) {
            return;
        }

        var blockEntity = level.getBlockEntity(event.getPos());
        if (!(blockEntity instanceof TankBlockEntity tankEntity)) {
            return;
        }

        ItemStack heldItem = player.getItemInHand(event.getHand());
        
        // Try to fill empty bucket from tank
        if (heldItem.is(Items.BUCKET)) {
            fillBucketFromTank(tankEntity, player, heldItem, event.getHand());
            event.setCanceled(true);
            return;
        }

        // Try to empty filled bucket into tank (supports ANY bucket via Fluid API)
        if (heldItem.getItem() instanceof BucketItem bucketItem) {
            emptyBucketIntoTank(tankEntity, player, heldItem, event.getHand());
            event.setCanceled(true);
            return;
        }
    }

    private static void fillBucketFromTank(TankBlockEntity tankEntity, Player player, ItemStack bucket, net.minecraft.world.InteractionHand hand) {
        FluidStack fluid = tankEntity.getFluid();
        
        if (!fluid.isEmpty() && fluid.getAmount() >= 1000) {
            // Use Fluid API to get the correct bucket for this fluid
            ItemStack filledBucket = FluidUtil.getFilledBucket(fluid);
            
            if (!filledBucket.isEmpty()) {
                tankEntity.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                
                if (!player.addItem(filledBucket)) {
                    player.drop(filledBucket, false);
                }
                
                if (!player.isCreative()) {
                    bucket.shrink(1);
                }
            }
        }
    }

    private static void emptyBucketIntoTank(TankBlockEntity tankEntity, Player player, ItemStack bucket, net.minecraft.world.InteractionHand hand) {
        FluidStack bucketFluid = getFluidFromBucket(bucket);
        
        if (!bucketFluid.isEmpty()) {
            int filled = tankEntity.fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);
            
            if (filled > 0) {
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                
                if (!player.addItem(emptyBucket)) {
                    player.drop(emptyBucket, false);
                }
                
                if (!player.isCreative()) {
                    bucket.shrink(1);
                }
            }
        }
    }

    private static FluidStack getFluidFromBucket(ItemStack stack) {
        // Use Fluid API to extract fluid from any bucket
        if (stack.getItem() instanceof BucketItem bucketItem) {
            var fluid = bucketItem.getFluid();
            if (fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                return new FluidStack(fluid, 1000);
            }
        }
        return FluidStack.EMPTY;
    }
}