package team.nextlevelmodding.ar2.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.nextlevelmodding.ar2.ar2;
import team.nextlevelmodding.ar2.blocks.FlightControlComputerBlockEntity;

import java.util.List;
import java.util.Objects;

public class FlightcontrolMenu extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ar2.MOD_ID);
    public static final RegistryObject<MenuType<FlightcontrolMenu>> FLIGHT_CONTROL_MENU =
        MENUS.register("flight_control",
            () -> IForgeMenuType.create((containerId, inventory, data) -> {
                BlockPos pos = data != null ? data.readBlockPos() : BlockPos.ZERO;
                return new FlightcontrolMenu(containerId, inventory, pos);
            }));

    private final BlockPos pos;
    private final Player player;
    private final Level level;
    private FlightControlComputerBlockEntity blockEntity;
    private List<String> programs = List.of();

    public FlightcontrolMenu(int windowId, Inventory inv, BlockPos pos) {
        this(windowId, inv, pos, pos != BlockPos.ZERO ? (FlightControlComputerBlockEntity) inv.player.level().getBlockEntity(pos) : null);
    }

    public FlightcontrolMenu(int windowId, Inventory inv, BlockPos pos, FlightControlComputerBlockEntity blockEntity) {
        super(FLIGHT_CONTROL_MENU.get(), windowId);
        this.pos = pos;
        this.player = inv.player;
        this.level = inv.player.level();
        this.blockEntity = blockEntity;

        // Load programs from block entity NBT
        if (blockEntity != null) {
            this.programs = blockEntity.getPrograms();
        } else {
            this.programs = List.of();
        }
    }

    public static FlightcontrolMenu client(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new FlightcontrolMenu(windowId, inv, data.readBlockPos());
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FlightControlComputerBlockEntity) {
                blockEntity = (FlightControlComputerBlockEntity) be;
            } else {
                return false;
            }
        }
        return stillValid(ContainerLevelAccess.create(level, pos), player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player playerIn, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    public BlockPos getPos() {
        return pos;
    }

    public List<String> getPrograms() {
        return programs;
    }

    public FlightControlComputerBlockEntity getBlockEntity() {
        if (blockEntity == null || blockEntity.isRemoved()) {
            blockEntity = (FlightControlComputerBlockEntity) level.getBlockEntity(pos);
        }
        return blockEntity;
    }
}
