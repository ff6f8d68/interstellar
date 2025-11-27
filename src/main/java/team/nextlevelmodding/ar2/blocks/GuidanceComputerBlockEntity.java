package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.nextlevelmodding.ar2.MasterCallEvent;
import team.nextlevelmodding.ar2.ModBlocks;
import team.nextlevelmodding.ar2.ar2;
import team.nextlevelmodding.ar2.gui.FlightcontrolMenu;
import team.nextlevelmodding.ar2.network.AR2Packets;
import team.nextlevelmodding.ar2.network.ServerboundRunProgramPacket;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GuidanceComputerBlockEntity extends BlockEntity {
    private static final String PROGRAMS_TAG = "Programs";
    private final List<String> programs = new ArrayList<>();
    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    
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

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public List<String> getPrograms() {
        return new ArrayList<>(programs);
    }

    public boolean addProgram(String programName) {
        if (!programs.contains(programName)) {
            programs.add(programName);
            setChanged();
            return true;
        }
        return false;
    }

    public boolean removeProgram(String programName) {
        boolean removed = programs.remove(programName);
        if (removed) {
            setChanged();
        }
        return removed;
    }







    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }




    

    public void addChild(BlockPos pos) {
        ar2.LOGGER.info("Adding child at {}", pos);
        linkedChildren.add(pos);
        setChanged();
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

    // Get all linked children of a specific type and rotation
    public Set<BlockPos> getChildrenOfType(Class<?> blockType, Direction rotation) {
        if (level == null) return new HashSet<>();
        return linkedChildren.stream()
                .filter(pos -> {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (!blockType.isInstance(be)) return false;
                    BlockState state = level.getBlockState(pos);
                    return state.hasProperty(BlockStateProperties.FACING) && state.getValue(BlockStateProperties.FACING).equals(rotation);
                })
                .collect(Collectors.toSet());
    }

    // Get the child at a specific BlockPos if linked
    public Set<BlockPos> getChildrenOfType(BlockPos pos) {
        if (level == null) return new HashSet<>();
        if (linkedChildren.contains(pos)) {
            return Set.of(pos);
        } else {
            return new HashSet<>();
        }
    }

    // Get all linked children of a specific Block type
    public Set<BlockPos> getChildrenOfType(Block block) {
        if (level == null) return new HashSet<>();
        return linkedChildren.stream()
                .filter(pos -> level.getBlockState(pos).getBlock() == block)
                .collect(Collectors.toSet());
    }

    // Call children of a specific type and rotation
    public void callChildrenOfType(Class<?> blockType, Direction rotation, Object data) {
        if (level == null) return;
        for (BlockPos child : getChildrenOfType(blockType, rotation)) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data, level));
        }
    }

    // Call child at a specific BlockPos if linked
    public void callChildrenOfType(BlockPos pos, Object data) {
        if (level == null) return;
        if (linkedChildren.contains(pos)) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(pos, data, level));
        }
    }

    // Call children of a specific Block type
    public void callChildrenOfType(Block block, Object data) {
        if (level == null) return;
        for (BlockPos child : getChildrenOfType(block)) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(child, data, level));
        }
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
