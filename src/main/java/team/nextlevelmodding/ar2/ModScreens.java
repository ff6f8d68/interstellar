package team.nextlevelmodding.ar2;

import net.minecraft.client.gui.screens.MenuScreens;
import team.nextlevelmodding.ar2.client.gui.GuinuclearengineScreen;

public class ModScreens {
    public static void registerScreens() {
        MenuScreens.register(ModMenus.GUINUCLEARENGINE.get(), GuinuclearengineScreen::new);
    }
}
