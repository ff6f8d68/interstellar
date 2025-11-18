package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import team.nextlevelmodding.ar2.MasterCallEvent;
import team.nextlevelmodding.ar2.ModBlocks;
import team.nextlevelmodding.ar2.data.TestData;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuidanceComputerBlockEntity extends BlockEntity {
    
    public static void tick(Level level, BlockPos pos, BlockState state, GuidanceComputerBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        // Your tick logic here
        // For example, you might want to process queued operations or update state
        
        // Example: Call children every 10 ticks (0.5 seconds) if powered
        if (level.getGameTime() % 10 == 0 && level.hasNeighborSignal(pos)) {
            // Pass the game time as a double to TestData
            blockEntity.callChildren(new TestData(level.getGameTime()));
        }
    }

    private final Set<BlockPos> linkedChildren = new HashSet<>();
    private int tickCounter = 0;

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
            System.out.println("[GuidanceComputer] Cannot call children: Level is null");
            return;
        }
        System.out.println("[GuidanceComputer] Calling " + linkedChildren.size() + " children with data: " + data);
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
    public void tick() {
        if (level == null || level.isClientSide) return;

        // Example: Only call Test blocks if this block is powered by redstone
        if (level.hasNeighborSignal(worldPosition)) {
            TestData data = new TestData(12000); // replace 12000 with desired thrust
            callTestChildren(data);

            // Examples of other ways to use the targeting system:
            // callThrusterChildren(someData);       // Call only thrusters
            // callTankChildren(someData);           // Call only tanks
            // callChildrenOfType(TankBlockEntity.class, someData);  // Call specific type
            
            // Advanced: Custom predicate (e.g., only blocks below this computer)
            // callChildrenMatching(pos -> pos.getY() < worldPosition.getY(), someData);
        }

        // schedule next tick
        level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }

}
