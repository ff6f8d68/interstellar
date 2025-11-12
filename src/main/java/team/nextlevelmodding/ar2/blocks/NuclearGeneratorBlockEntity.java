package team.nextlevelmodding.ar2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.nextlevelmodding.ar2.ModBlocks;

public class NuclearGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public NuclearGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.NUCLEAR_GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Nuclear Generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new NuclearGeneratorMenu(containerId, inventory, this);
    }
}