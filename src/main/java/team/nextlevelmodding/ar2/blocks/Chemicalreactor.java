package team.nextlevelmodding.ar2.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Chemicalreactor extends Block {

    public Chemicalreactor() {
        super(BlockBehaviour.Properties
                .of()
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }
}
