package mods.hexagonal.ar2;

import mods.hexagonal.ar2.commands.ThrustCommand;
import net.minecraftforge.event.RegisterCommandsEvent;

public class ModCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(ThrustCommand.register());
    }
}
