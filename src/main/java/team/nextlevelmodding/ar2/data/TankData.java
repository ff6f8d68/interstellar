package team.nextlevelmodding.ar2.data;

import net.minecraftforge.fluids.FluidStack;

public class TankData {
    private final FluidStack fluid;
    private final int capacity;

    public TankData(FluidStack fluid, int capacity) {
        this.fluid = fluid;
        this.capacity = capacity;
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public int getCapacity() {
        return capacity;
    }
}