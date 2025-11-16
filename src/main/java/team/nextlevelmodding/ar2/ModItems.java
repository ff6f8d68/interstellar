package team.nextlevelmodding.ar2;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.nextlevelmodding.ar2.items.LinkerItem;
import team.nextlevelmodding.ar2.items.UsbItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ar2.MOD_ID);
    public static final RegistryObject<Item> LINKER_ITEM =
            ITEMS.register("linker",
                    LinkerItem::new
            );
    public static final RegistryObject<Item>USB_ITEM =
            ITEMS.register("usb",
                    UsbItem::new
            );
}
