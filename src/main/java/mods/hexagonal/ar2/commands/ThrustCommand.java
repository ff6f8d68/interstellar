package mods.hexagonal.ar2.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import mods.hexagonal.ar2.VFX.VFXthrust;

public class ThrustCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("spawnthrust")
                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(ThrustCommand::execute))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        double x = DoubleArgumentType.getDouble(context, "x");
        double y = DoubleArgumentType.getDouble(context, "y");
        double z = DoubleArgumentType.getDouble(context, "z");

        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            // Call your VFXthrust.spawn method
            VFXthrust.spawn(mc.level, x, y, z);
        }

        return Command.SINGLE_SUCCESS;
    }
}
