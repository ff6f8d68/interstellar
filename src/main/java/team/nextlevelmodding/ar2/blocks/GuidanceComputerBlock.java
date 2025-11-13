package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import team.nextlevelmodding.ar2.MasterCallEvent;

import java.util.HashSet;
import java.util.Set;

public class GuidanceComputerBlock extends Block {

    private final Set<BlockPos> linkedChildren = new HashSet<>();

    public GuidanceComputerBlock() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }

    public void addChild(BlockPos childPos) {
        linkedChildren.add(childPos);
    }

    public void removeChild(BlockPos childPos) {
        linkedChildren.remove(childPos);
    }

    public void callChildren(Level level, Object data) {
        for (BlockPos childPos : linkedChildren) {
            MinecraftForge.EVENT_BUS.post(new MasterCallEvent(childPos, data));
        }
    }

    public Set<BlockPos> getLinkedChildren() {
        return new HashSet<>(linkedChildren);
    }
}
