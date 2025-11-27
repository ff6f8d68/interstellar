package team.nextlevelmodding.ar2.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import team.nextlevelmodding.ar2.ar2;

import java.util.function.Supplier;

public class ServerboundRunProgramPacket {
    private final BlockPos pos;
    private final String programName;

    public ServerboundRunProgramPacket(BlockPos pos, String programName) {
        this.pos = pos;
        this.programName = programName;
    }

    public static void encode(ServerboundRunProgramPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.programName);
    }

    public static ServerboundRunProgramPacket decode(FriendlyByteBuf buf) {
        return new ServerboundRunProgramPacket(buf.readBlockPos(), buf.readUtf());
    }

    public static void handle(ServerboundRunProgramPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // TODO: Implement program execution when flight control computer block entity is ready
                // This will communicate with the guidance computer linked to the flight control computer
                ar2.LOGGER.info("Player {} selected program {} at position {}",
                    player.getName().getString(), msg.programName, msg.pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
