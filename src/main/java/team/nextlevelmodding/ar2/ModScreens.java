package team.nextlevelmodding.ar2;



import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.gui.screens.MenuScreens;

import team.nextlevelmodding.ar2.ModBlocks;
import team.nextlevelmodding.ar2.client.gui.NuclearGeneratorScreen;
import team.nextlevelmodding.ar2.client.gui.GuinuclearengineScreen;
import team.nextlevelmodding.ar2.ModMenus;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModScreens {
    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.GUINUCLEARENGINE.get(), GuinuclearengineScreen::new);
            MenuScreens.register(ModBlocks.NUCLEAR_GENERATOR_MENU.get(), NuclearGeneratorScreen::new);
        });
    }
}
