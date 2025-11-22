package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class RocketMotorBlockEntity extends BlockEntity implements INBTSerializable<CompoundTag> {
    private static final String THRUST_KEY = "CurrentThrust";
    private double currentThrust = 0.0;

    public RocketMotorBlockEntity(BlockPos pos, BlockState state) {
        // Block entity registration was removed to focus on test blocks; keep the class for tests.
        super(null, pos, state);
    }

    public double getCurrentThrust() {
        return currentThrust;
    }

    public void setCurrentThrust(double thrust) {
        this.currentThrust = Math.max(0, thrust);
        setChanged();
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.currentThrust = nbt.getDouble(THRUST_KEY);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble(THRUST_KEY, currentThrust);
    }

    @Override
    public @NotNull CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        load(nbt);
    }
}
