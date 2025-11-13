package team.nextlevelmodding.ar2;

import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.Event;

public class MasterCallEvent extends Event {

    private final BlockPos targetBlock;
    private final Object data;

    public MasterCallEvent(BlockPos targetBlock, Object data) {
        this.targetBlock = targetBlock;
        this.data = data;
    }

    public BlockPos getTargetBlock() {
        return targetBlock;
    }

    public Object getData() {
        return data;
    }
}