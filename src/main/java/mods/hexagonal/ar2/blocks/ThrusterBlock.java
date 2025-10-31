package mods.hexagonal.ar2.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ThrusterBlock extends Block {

    public ThrusterBlock() {
        super(BlockBehaviour.Properties
                .of()                // start the builder
                        .strength(3.0f, 6.0f)    // hardness and resistance
                        .requiresCorrectToolForDrops()  // optional: require correct tool
                        .noOcclusion()            // optional: for non-full-cube blocks
                // .sound(SoundType.METAL) etc, you can still set sound, light, etc
        );
    }
}
