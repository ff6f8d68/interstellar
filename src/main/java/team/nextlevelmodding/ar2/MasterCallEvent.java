package team.nextlevelmodding.ar2;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Forge event that can be posted on the MinecraftForge.EVENT_BUS to notify listeners
 * about a master call targeting a specific block position. This event is cancelable.
 */
@Cancelable
public class MasterCallEvent extends Event {

    private final BlockPos targetBlock;
    private final Object data;
    private final Level level;

    public MasterCallEvent(BlockPos targetBlock, Object data, Level level) {
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null");
        }
        this.targetBlock = targetBlock.immutable();
        this.data = data;
        this.level = level;
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