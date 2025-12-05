package team.nextlevelmodding.ar2.items;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.nextlevelmodding.ar2.blocks.FlightControlComputerBlockEntity;
import team.nextlevelmodding.ar2.blocks.GuidanceComputerBlock;
import team.nextlevelmodding.ar2.blocks.TankBlockEntity;
import team.nextlevelmodding.ar2.utils.StructureUsb;
import java.util.List;
import java.util.UUID;

public class UsbItem extends Item {
    private static final int MAX_STORAGE_BYTES = 2 * 1024 * 1024 * 1024; // 2GB in bytes
    public static final String STORAGE_TAG = "UsbData";
    private static final String USAGE_TAG = "UsedBytes";
    private static final String ID_TAG = "UsbId";
    private static final String PROGRAMS_FOLDER = "programs";

    public UsbItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public String getDescriptionId() {
        return "item.ar2.usb_stick";
    }
    
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack stack = context.getItemInHand();
        
        // Handle shift-right-click on Flight Control Computer
        if (player.isShiftKeyDown() && blockEntity instanceof FlightControlComputerBlockEntity flightControl) {
            return handleFlightControlInteraction(flightControl, stack, player);
        }
        
        return InteractionResult.PASS;
    }
    
    private InteractionResult handleFlightControlInteraction(FlightControlComputerBlockEntity flightControl, 
                                                           ItemStack usbStack, Player player) {
        // Get the flight control's programs
        List<String> flightControlPrograms = flightControl.getPrograms();
        
        // Get the root directory listing from the USB
        List<String> usbFiles = StructureUsb.list(usbStack, "");
        if (usbFiles == null || usbFiles.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cUSB drive is empty!"));
            return InteractionResult.FAIL;
        }
        
        // Find and import program files
        int importedCount = 0;
        for (String fileName : usbFiles) {
            if (fileName.endsWith("/")) continue; // Skip directories
            
            // Check if it's a program file (you might want to add more extensions)
            if (fileName.endsWith(".lua") || fileName.endsWith(".txt")) {
                String content = StructureUsb.load(usbStack, fileName);
                if (content != null && !content.trim().isEmpty()) {
                    // Add the program to the flight control
                    flightControl.addProgram(fileName);
                    importedCount++;
                }
            }
        }
        
        if (importedCount > 0) {
            player.sendSystemMessage(Component.literal("§aImported " + importedCount + " program(s) to the Flight Control Computer."));
            return InteractionResult.SUCCESS;
        } else {
            player.sendSystemMessage(Component.literal("§eNo valid program files found on the USB drive."));
            return InteractionResult.FAIL;
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        int usedBytes = getUsedBytes(stack);
        double usedMB = usedBytes / (1024.0 * 1024.0);
        double totalMB = MAX_STORAGE_BYTES / (1024.0 * 1024.0);
        
        tooltip.add(Component.literal(String.format("Storage: %.2f/%.0f MB", usedMB, totalMB)));
        tooltip.add(Component.literal("ID: " + getOrCreateId(stack)));
    }

    public static String getOrCreateId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(ID_TAG)) {
            tag.putUUID(ID_TAG, UUID.randomUUID());
        }
        return tag.getUUID(ID_TAG).toString();
    }

    public static int getUsedBytes(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null) {
            return stack.getTag().getInt(USAGE_TAG);
        }
        return 0;
    }

    public static int getRemainingBytes(ItemStack stack) {
        return MAX_STORAGE_BYTES - getUsedBytes(stack);
    }

    public static boolean hasEnoughSpace(ItemStack stack, int requiredBytes) {
        return getRemainingBytes(stack) >= requiredBytes;
    }

    public static boolean storeData(ItemStack stack, String key, Tag data) {
        if (!(stack.getItem() instanceof UsbItem)) return false;
        
        int dataSize = data.toString().getBytes().length;
        if (!hasEnoughSpace(stack, dataSize)) return false;
        
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(STORAGE_TAG, Tag.TAG_COMPOUND)) {
            tag.put(STORAGE_TAG, new CompoundTag());
        }
        
        CompoundTag storage = tag.getCompound(STORAGE_TAG);
        storage.put(key, data);
        
        // Update used bytes
        int currentUsed = tag.getInt(USAGE_TAG);
        tag.putInt(USAGE_TAG, currentUsed + dataSize);
        
        return true;
    }

    @Nullable
    public static Tag getData(ItemStack stack, String key) {
        if (!(stack.getItem() instanceof UsbItem) || !stack.hasTag()) return null;
        
        CompoundTag tag = stack.getTag();
        if (!tag.contains(STORAGE_TAG, Tag.TAG_COMPOUND)) return null;
        
        CompoundTag storage = tag.getCompound(STORAGE_TAG);
        return storage.contains(key) ? storage.get(key) : null;
    }

    public static boolean deleteData(ItemStack stack, String key) {
        if (!(stack.getItem() instanceof UsbItem) || !stack.hasTag()) return false;
        
        CompoundTag tag = stack.getTag();
        if (!tag.contains(STORAGE_TAG, Tag.TAG_COMPOUND)) return false;
        
        CompoundTag storage = tag.getCompound(STORAGE_TAG);
        if (!storage.contains(key)) return false;
        
        // Update used bytes
        int removedSize = storage.get(key).toString().getBytes().length;
        int currentUsed = tag.getInt(USAGE_TAG);
        tag.putInt(USAGE_TAG, Math.max(0, currentUsed - removedSize));
        
        storage.remove(key);
        return true;
    }
}