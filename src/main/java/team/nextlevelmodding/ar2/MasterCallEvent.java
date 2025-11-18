package team.nextlevelmodding.ar2;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class MasterCallEvent extends Event {

    private final BlockPos targetBlock;
    private final Object data;
    private final Level level;

    public MasterCallEvent(BlockPos targetBlock, Object data, Level level) {
        this.targetBlock = targetBlock;
        this.data = data;
        this.level = level;
    }

    // Backwards compatibility constructor
    public MasterCallEvent(BlockPos targetBlock, Object data) {
        this(targetBlock, data, null);
    }

    public BlockPos getTargetBlock() {
        return targetBlock;
    }

    public Object getData() {
        return data;
    }

    public Level getLevel() {
        return level;
    }
}