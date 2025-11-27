package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.nextlevelmodding.ar2.gui.FlightcontrolMenu;

import java.util.ArrayList;
import java.util.List;

public class FlightControlComputerBlockEntity extends BlockEntity implements MenuProvider {

    // TODO: Add fields for linked guidance computer position when ready
    // private BlockPos linkedGuidanceComputer = null;

    private List<String> programs = new ArrayList<>();

    public FlightControlComputerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public List<String> getPrograms() {
        return new ArrayList<>(programs);
    }

    public void setPrograms(List<String> programs) {
        this.programs = new ArrayList<>(programs);
        setChanged();
    }

    public void addProgram(String programName) {
        if (!programs.contains(programName)) {
            programs.add(programName);
            setChanged();
        }
    }

    public void removeProgram(String programName) {
        programs.remove(programName);
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Flight Control Computer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FlightcontrolMenu(containerId, inventory, worldPosition);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        // Save programs list
        ListTag programsTag = new ListTag();
        for (String program : programs) {
            programsTag.add(StringTag.valueOf(program));
        }
        tag.put("Programs", programsTag);

        // TODO: Save linked guidance computer position when implemented
        // if (linkedGuidanceComputer != null) {
        //     tag.putLong("LinkedGuidanceComputer", linkedGuidanceComputer.asLong());
        // }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        // Load programs list
        programs.clear();
        if (tag.contains("Programs", Tag.TAG_LIST)) {
            ListTag programsTag = tag.getList("Programs", Tag.TAG_STRING);
            for (int i = 0; i < programsTag.size(); i++) {
                programs.add(programsTag.getString(i));
            }
        }

        // TODO: Load linked guidance computer position when implemented
        // if (tag.contains("LinkedGuidanceComputer")) {
        //     linkedGuidanceComputer = BlockPos.of(tag.getLong("LinkedGuidanceComputer"));
        // }
    }
}
