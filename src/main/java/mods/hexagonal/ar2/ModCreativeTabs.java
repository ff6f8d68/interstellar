package mods.hexagonal.ar2;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class ModCreativeTabs {


    public static final CreativeModeTab ADVANCED_ROCKETRY_VS = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.advancedrocketryvs")) // from lang file
            .icon(() -> new ItemStack(ModBlocks.ROCKETMOTOR.get()))
            .displayItems((params, output) -> {
                // Add items in the BuildCreativeModeTabContentsEvent instead
            })
            .build();
}
