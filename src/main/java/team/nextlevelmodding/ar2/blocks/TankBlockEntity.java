package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.nextlevelmodding.ar2.MasterCallEvent;
import team.nextlevelmodding.ar2.data.TankData;

import java.util.ArrayList;
import java.util.List;

public class TankBlockEntity extends BlockEntity implements IFluidTank {

    private final FluidTank tank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final LazyOptional<TankBlockEntity> fluidHandler = LazyOptional.of(() -> this);

    public TankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /* ================================
       FluidTank Methods
       ================================ */

    @Override
    public @NotNull FluidStack getFluid() {
        return tank.getFluid();
    }

    @Override
    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    @Override
    public int getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return tank.isFluidValid(stack);
    }

    /**
     * Fill the tank, distributing overflow to neighboring tanks (gravity aware)
     */
    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || level == null) return 0;

        int filled = tank.fill(resource, action);
        int remaining = resource.getAmount() - filled;

        if (remaining > 0) {
            // Fill downward first
            remaining -= distributeFluid(resource, remaining, Direction.DOWN, action);
            // Fill sides if still remaining
            if (remaining > 0) {
                Direction[] sides = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
                remaining -= distributeFluid(resource, remaining, sides, action);
            }
        }
        return resource.getAmount() - remaining;
    }

    /**
     * Drain the tank, pulling from tanks above first (gravity aware)
     */
    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (maxDrain <= 0 || level == null) return FluidStack.EMPTY;

        FluidStack drained = tank.drain(maxDrain, action);
        int remaining = maxDrain - drained.getAmount();

        if (remaining > 0) {
            // Drain from above first
            FluidStack fromAbove = pullFluid(remaining, Direction.UP, action);
            if (!fromAbove.isEmpty()) drained.grow(fromAbove.getAmount());

            // Drain from sides
            if (drained.getAmount() < maxDrain) {
                Direction[] sides = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
                for (Direction side : sides) {
                    FluidStack fromSide = pullFluid(maxDrain - drained.getAmount(), side, action);
                    if (!fromSide.isEmpty()) drained.grow(fromSide.getAmount());
                    if (drained.getAmount() >= maxDrain) break;
                }
            }
        }
        return drained;
    }

    @SubscribeEvent
    public void onMasterCall(MasterCallEvent event) {
        if (event.getTargetBlock().equals(worldPosition)) {
            // Handle tank data request
            if (event.getData() instanceof String && "REQUEST_TANK_DATA".equals(event.getData())) {
                TankData tankData = new TankData(tank.getFluid(), tank.getCapacity());
                // Send data back to master (implementation depends on how master handles responses)
            }
        }
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        return drain(resource.getAmount(), action);
    }

    /* ================================
       Fluid Distribution Helpers
       ================================ */

    private int distributeFluid(FluidStack resource, int remaining, Direction dir, IFluidHandler.FluidAction action) {
        BlockPos neighborPos = worldPosition.relative(dir);
        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be instanceof TankBlockEntity neighborTank) {
            FluidStack copy = new FluidStack(resource, remaining);
            return neighborTank.fill(copy, action);
        }
        return 0;
    }

    private int distributeFluid(FluidStack resource, int remaining, Direction[] dirs, IFluidHandler.FluidAction action) {
        int distributed = 0;
        for (Direction dir : dirs) {
            distributed += distributeFluid(resource, remaining - distributed, dir, action);
            if (distributed >= remaining) break;
        }
        return distributed;
    }

    private FluidStack pullFluid(int max, Direction dir, IFluidHandler.FluidAction action) {
        BlockPos neighborPos = worldPosition.relative(dir);
        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be instanceof TankBlockEntity neighborTank) {
            return neighborTank.drain(max, action);
        }
        return FluidStack.EMPTY;
    }

    /* ================================
       Capabilities
       ================================ */

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();
        return super.getCapability(cap, side);
    }

    /* ================================
       NBT Save & Load
       ================================ */

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag tankTag = new CompoundTag();
        tank.writeToNBT(tankTag);
        tag.put("Tank", tankTag);
    }

    /* ================================
       Optional: Balancing Fluid Across Network
       ================================ */

    public void balanceFluid() {
        if (level == null) return;

        List<TankBlockEntity> neighbors = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            if (be instanceof TankBlockEntity neighbor) neighbors.add(neighbor);
        }

        if (neighbors.isEmpty()) return;

        int totalFluid = tank.getFluidAmount();
        for (TankBlockEntity neighbor : neighbors) totalFluid += neighbor.getFluidAmount();
        int average = totalFluid / (neighbors.size() + 1);

        tank.drain(tank.getFluidAmount() - average, IFluidHandler.FluidAction.EXECUTE);
        for (TankBlockEntity neighbor : neighbors) {
            neighbor.tank.drain(neighbor.tank.getFluidAmount() - average, IFluidHandler.FluidAction.EXECUTE);
        }
    }
}
