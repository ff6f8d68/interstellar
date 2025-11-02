package mods.hexagonal.ar2.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.Command;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import mods.hexagonal.ar2.VFX.VFXthrust;

public class ThrustCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("spawnthrust")
                .then(Commands.argument("pos", Vec3Argument.vec3())
                        .executes(ThrustCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        // Use getCoordinates() then getPosition(source) for 1.20.1
        Vec3 pos = Vec3Argument.getCoordinates(context, "pos").getPosition(context.getSource());

        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            // Call your VFXthrust.spawn method
            VFXthrust.spawn(mc.level, pos.x, pos.y, pos.z);
        }

        return Command.SINGLE_SUCCESS;
    }
}
