package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.nextlevelmodding.ar2.MasterCallEvent;
import team.nextlevelmodding.ar2.ModBlocks;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraftforge.common.MinecraftForge;

public class GuidanceComputerBlockEntity extends BlockEntity {
    
    public static void tick(Level level, BlockPos pos, BlockState state, GuidanceComputerBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        // Call children every tick if powered (was every 10 ticks)
        if (level.hasNeighborSignal(pos)) {
            // Send 120000 to all linked Test blocks only
            blockEntity.callChildrenMatching(childPos -> level.getBlockState(childPos).getBlock() == ModBlocks.TEST.get(), 120000);
        }
    }

    private final Set<BlockPos> linkedChildren = new HashSet<>();

    public GuidanceComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GUIDANCE_COMPUTER_BE.get(), pos, state);
    }

    public void addChild(BlockPos pos) {
        System.out.println("[GuidanceComputer] Adding child at " + pos);
        linkedChildren.add(pos);
        setChanged(); // mark dirty
    }

    public void removeChild(BlockPos pos) {
        System.out.println("[GuidanceComputer] Removing child at " + pos);
        boolean removed = linkedChildren.remove(pos);
        if (removed) {
            System.out.println("[GuidanceComputer] Successfully removed child at " + pos);
        } else {
            System.out.println("[GuidanceComputer] Child not found at " + pos);
        }
        setChanged();
    }

    public Set<BlockPos> getChildren() {
        return new HashSet<>(linkedChildren);
    }

    public void callChildren(Object data) {
        if (level == null) {
            return;
        }
        for (BlockPos child : linkedChildren) {
            System.out.println("[GuidanceComputer] Dispatching to child at " + child);
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data, level));
        }
    }

    // ===== TARGETING SYSTEM =====
    // Get all linked children of a specific type
    public Set<BlockPos> getChildrenOfType(Class<?> blockType) {
        if (level == null) return new HashSet<>();
        return linkedChildren.stream()
                .filter(pos -> {
                    BlockEntity be = level.getBlockEntity(pos);
                    return blockType.isInstance(be);
                })
                .collect(Collectors.toSet());
    }

    // Get all Tank blocks
    public Set<BlockPos> getTankChildren() {
        return getChildrenOfType(TankBlockEntity.class);
    }

    // Get all Test blocks
    public Set<BlockPos> getTestChildren() {
        if (level == null) return new HashSet<>();
        return linkedChildren.stream()
                .filter(pos -> level.getBlockState(pos).getBlock() instanceof Test)
                .collect(Collectors.toSet());
    }

    // Get all Thruster blocks
    public Set<BlockPos> getThrusterChildren() {
        if (level == null) return new HashSet<>();
        return linkedChildren.stream()
                .filter(pos -> level.getBlockState(pos).getBlock().getClass().getSimpleName().toLowerCase().contains("rocketmotor"))
                .collect(Collectors.toSet());
    }

    // Call children of a specific type
    public void callChildrenOfType(Class<?> blockType, Object data) {
        if (level == null) return;
        for (BlockPos child : getChildrenOfType(blockType)) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data, level));
        }
    }

    // Call children matching a custom predicate (for advanced filtering)
    public void callChildrenMatching(Predicate<BlockPos> filter, Object data) {
        if (level == null) return;
        linkedChildren.stream()
                .filter(filter)
                .forEach(child -> MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data, level)));
    }

    // Call Test blocks only
    public void callTestChildren(Object data) {
        if (level == null) return;
        for (BlockPos child : getTestChildren()) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data, level));
        }
    }

    // Call Tank children only
    public void callTankChildren(Object data) {
        if (level == null) return;
        for (BlockPos child : getTankChildren()) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data, level));
        }
    }

    // Call Thruster children only
    public void callThrusterChildren(Object data) {
        if (level == null) return;
        for (BlockPos child : getThrusterChildren()) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data, level));
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
}
