package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import team.nextlevelmodding.ar2.MasterCallEvent;
import team.nextlevelmodding.ar2.ModBlocks;
import team.nextlevelmodding.ar2.data.TestData;

import java.util.HashSet;
import java.util.Set;

public class GuidanceComputerBlockEntity extends BlockEntity {

    private final Set<BlockPos> linkedChildren = new HashSet<>();

    public GuidanceComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GUIDANCE_COMPUTER_BE.get(), pos, state);
    }

    public void addChild(BlockPos pos) {
        linkedChildren.add(pos);
        setChanged(); // mark dirty
    }

    public void removeChild(BlockPos pos) {
        linkedChildren.remove(pos);
        setChanged();
    }

    public Set<BlockPos> getChildren() {
        return new HashSet<>(linkedChildren);
    }

    public void callChildren(Object data) {
        if (level == null) return;
        for (BlockPos child : linkedChildren) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        linkedChildren.clear();

        var list = tag.getLongArray("Children");
        for (long packed : list) {
            linkedChildren.add(BlockPos.of(packed));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        long[] arr = linkedChildren.stream().mapToLong(BlockPos::asLong).toArray();
        tag.putLongArray("Children", arr);
        super.saveAdditional(tag);
    }

    // Called when block is removed
    public void onRemoved() {
        linkedChildren.clear();
    }
    public void tick() {
        if (level == null || level.isClientSide) return;

        // Only call children if this block is powered by redstone
        if (level.hasNeighborSignal(worldPosition)) {
            for (BlockPos childPos : linkedChildren) {
                var state = level.getBlockState(childPos);

                // Only call if the child is a TestBlock
                if (state.getBlock() instanceof Test) {
                    // Create TestData with the thrust level expected
                    TestData data = new TestData(12000); // replace 12000 with desired thrust
                    MinecraftForge.EVENT_BUS.post(new MasterCallEvent(childPos, data));
                }
            }
        }

        // schedule next tick
        level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }

}
