package team.nextlevelmodding.ar2;

import team.nextlevelmodding.ar2.commands.ThrustCommand;
import net.minecraftforge.event.RegisterCommandsEvent;

public class ModCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(ThrustCommand.register());
    }
}
