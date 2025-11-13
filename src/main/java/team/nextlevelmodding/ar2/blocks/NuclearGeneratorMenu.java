package team.nextlevelmodding.ar2.blocks;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.nextlevelmodding.ar2.ModBlocks;
import team.nextlevelmodding.ar2.ModMenus;

public class NuclearGeneratorMenu extends AbstractContainerMenu {

    public final NuclearGeneratorBlockEntity blockEntity;

    public NuclearGeneratorMenu(int containerId, Inventory inventory, NuclearGeneratorBlockEntity blockEntity) {
        super(ModMenus.GUINUCLEARENGINE.get(), containerId);
        this.blockEntity = blockEntity;

        // Add player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new net.minecraft.world.inventory.Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new net.minecraft.world.inventory.Slot(inventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) == blockEntity;
    }
}