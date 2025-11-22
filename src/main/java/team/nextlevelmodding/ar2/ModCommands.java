package team.nextlevelmodding.ar2;

import net.minecraftforge.event.RegisterCommandsEvent;

public class ModCommands {

    // Called via method reference in ar2: MinecraftForge.EVENT_BUS.addListener(ModCommands::register)
    public static void register(RegisterCommandsEvent event) {
        // Placeholder: actual command registration can be added here
        ar2.LOGGER.info("Registering mod commands");
    }

}
