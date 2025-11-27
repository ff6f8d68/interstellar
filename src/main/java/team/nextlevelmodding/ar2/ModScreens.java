package team.nextlevelmodding.ar2;

import net.minecraft.client.gui.screens.MenuScreens;
import team.nextlevelmodding.ar2.client.gui.GuinuclearengineScreen;
import team.nextlevelmodding.ar2.client.gui.FlightcontrolScreen;
import team.nextlevelmodding.ar2.gui.FlightcontrolMenu;

public class ModScreens {
    public static void registerScreens() {
        MenuScreens.register(ModMenus.GUINUCLEARENGINE.get(), GuinuclearengineScreen::new);
        MenuScreens.register(FlightcontrolMenu.FLIGHT_CONTROL_MENU.get(), FlightcontrolScreen::new);
    }
}
