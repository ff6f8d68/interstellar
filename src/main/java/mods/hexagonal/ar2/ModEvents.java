package mods.hexagonal.ar2;

import mods.hexagonal.ar2.blocks.TankBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
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
        if (heldItem.getItem() == Items.BUCKET) {
            fillBucketFromTank(tankEntity, player, heldItem, event.getHand());
            event.setCanceled(true);
            return;
        }

        // Try to empty filled bucket into tank
        if (isFueledBucket(heldItem)) {
            emptyBucketIntoTank(tankEntity, player, heldItem, event.getHand());
            event.setCanceled(true);
            return;
        }
    }

    private static void fillBucketFromTank(TankBlockEntity tankEntity, Player player, ItemStack bucket, net.minecraft.world.InteractionHand hand) {
        var fluidHandler = tankEntity.getFluidOptional();
        
        fluidHandler.ifPresent(handler -> {
            FluidStack fluid = handler.getFluidInTank(0);
            
            if (!fluid.isEmpty() && fluid.getAmount() >= 1000) {
                String fluidName = fluid.getFluid().getFluidType().getDescriptionId();
                ItemStack filledBucket = getFilledBucket(fluidName);
                
                if (!filledBucket.isEmpty()) {
                    handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    
                    if (!player.addItem(filledBucket)) {
                        player.drop(filledBucket, false);
                    }
                    
                    if (!player.isCreative()) {
                        bucket.shrink(1);
                    }
                }
            }
        });
    }

    private static void emptyBucketIntoTank(TankBlockEntity tankEntity, Player player, ItemStack bucket, net.minecraft.world.InteractionHand hand) {
        var fluidHandler = tankEntity.getFluidOptional();
        
        fluidHandler.ifPresent(handler -> {
            FluidStack bucketFluid = getFluidFromBucket(bucket);
            
            if (!bucketFluid.isEmpty()) {
                int filled = handler.fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);
                
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
        });
    }

    private static boolean isFueledBucket(ItemStack stack) {
        return stack.is(ModBlocks.ROCKET_FUEL_BUCKET.get()) ||
               stack.is(ModBlocks.ADVANCED_ROCKET_FUEL_BUCKET.get()) ||
               stack.is(ModBlocks.BIPROPELLANT_ROCKET_FUEL_BUCKET.get()) ||
               stack.is(ModBlocks.NUCLEAR_ROCKET_FUEL_BUCKET.get());
    }

    private static ItemStack getFilledBucket(String fluidName) {
        // Map fluid names to bucket items
        if (fluidName.contains("rocket_fuel")) {
            if (fluidName.contains("nuclear")) {
                return new ItemStack(ModBlocks.NUCLEAR_ROCKET_FUEL_BUCKET.get());
            } else if (fluidName.contains("bipropellant")) {
                return new ItemStack(ModBlocks.BIPROPELLANT_ROCKET_FUEL_BUCKET.get());
            } else if (fluidName.contains("advanced")) {
                return new ItemStack(ModBlocks.ADVANCED_ROCKET_FUEL_BUCKET.get());
            } else {
                return new ItemStack(ModBlocks.ROCKET_FUEL_BUCKET.get());
            }
        }
        return ItemStack.EMPTY;
    }

    private static FluidStack getFluidFromBucket(ItemStack stack) {
        if (stack.is(ModBlocks.ROCKET_FUEL_BUCKET.get())) {
            return new FluidStack(Fluids.ROCKET_FUEL.get(), 1000);
        } else if (stack.is(ModBlocks.ADVANCED_ROCKET_FUEL_BUCKET.get())) {
            return new FluidStack(Fluids.ADVANCED_ROCKET_FUEL.get(), 1000);
        } else if (stack.is(ModBlocks.BIPROPELLANT_ROCKET_FUEL_BUCKET.get())) {
            return new FluidStack(Fluids.BIPROPELLANT_ROCKET_FUEL.get(), 1000);
        } else if (stack.is(ModBlocks.NUCLEAR_ROCKET_FUEL_BUCKET.get())) {
            return new FluidStack(Fluids.NUCLEAR_ROCKET_FUEL.get(), 1000);
        }
        return FluidStack.EMPTY;
    }
}