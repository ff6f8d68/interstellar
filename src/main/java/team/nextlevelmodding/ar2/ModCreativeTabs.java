package team.nextlevelmodding.ar2;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class ModCreativeTabs {


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ar2.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TUTORIAL_TAB = CREATIVE_MODE_TABS.register("tutorial_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.ROCKETMOTOR.get()))
                    .title(Component.literal("AR: VS blocks"))
                    .displayItems((pParameters, pOutput) -> {
                        ModBlocks.ITEMS.getEntries().forEach(itemObj -> pOutput.accept(itemObj.get()));



                    })
                    .build());
    public static final RegistryObject<CreativeModeTab> arvsitems = CREATIVE_MODE_TABS.register("arvsitems",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.USB_ITEM.get()))
                    .title(Component.literal("AR: VS items"))
                    .displayItems((pParameters, pOutput) -> {
                        ModItems.ITEMS.getEntries().forEach(itemObj -> pOutput.accept(itemObj.get()));



                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
