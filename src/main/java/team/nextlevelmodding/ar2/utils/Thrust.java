package team.nextlevelmodding.ar2.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.network.NetworkEvent;

import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import team.nextlevelmodding.ar2.ships.ForceInducedShips;
import team.nextlevelmodding.ar2.ships.ForceData;
import team.nextlevelmodding.ar2.ships.ForceMode;
import team.nextlevelmodding.ar2.ships.ForceDirectionMode;
import team.nextlevelmodding.ar2.Registry;

import java.util.function.Supplier;

public class Thrust {

    /**
     * Apply thrust toward a target position.
     * @param world     The world (server)
     * @param blockPos  Block position on/near the ship
     * @param targetPos Target position for thrust; if null, pushes downward
     * @param thrust    Magnitude of force
     */
    public static void applyThrust(Level world, BlockPos blockPos, BlockPos targetPos, double thrust) {
        if (!(world instanceof ServerLevel serverLevel)) return;
        if (blockPos == null) return;

        Ship s = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, blockPos);

        // Compute direction (down by default)
        Vec3 dir = targetPos != null
                ? Vec3.atCenterOf(targetPos).subtract(Vec3.atCenterOf(blockPos))
                : new Vec3(0, -1, 0);

        if (dir.length() < 1e-6) dir = new Vec3(0, -1, 0);
        dir = dir.normalize();

        // Apply force ONLY if ship exists
        if (s instanceof ServerShip serverShip) {
            // Convert world position to ship-local (important!)
            Vec3 worldCenter = Vec3.atCenterOf(blockPos);
            org.joml.Vector3dc shipLocal = serverShip.getTransform()
                    .getWorldToShip()
                    .transformPosition(VectorConversionsMCKt.toJOML(worldCenter));

            ForceInducedShips forceInducer = ForceInducedShips.getOrCreate(serverShip);

            // Invert direction so thrust pushes upward (opposite of computed direction)
            Vec3 invertedDir = dir.scale(-1);

            ForceData data = new ForceData(
                    VectorConversionsMCKt.toJOML(invertedDir),
                    thrust,
                    ForceMode.POSITION,
                    ForceDirectionMode.WORLD
            );

            forceInducer.addForce(blockPos, data);
        }

        // Send packet to clients to spawn particles
        sendParticlePacket(serverLevel, blockPos, s instanceof ServerShip ? (ServerShip) s : null);
    }

    /**
     * Overload: apply thrust downward if no target position is provided
     */
    public static void applyThrust(Level world, BlockPos blockPos, double thrust) {
        applyThrust(world, blockPos, null, 120000); // Default to 120000 thrust
    }

    /**
     * Send a packet to nearby clients to spawn thrust particles
     */
    private static void sendParticlePacket(ServerLevel level, BlockPos pos, ServerShip ship) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Get ship velocity (opposite direction for exhaust)
        double velX = 0, velY = 0, velZ = 0;
        if (ship != null) {
            org.joml.Vector3dc shipVel = ship.getVelocity();
            velX = -shipVel.x();
            velY = -shipVel.y();
            velZ = -shipVel.z();
        }

        // Send packet to nearby players
        ThrustParticlePacket packet = new ThrustParticlePacket(x, y, z, velX, velY, velZ);

        // Send to all players tracking this position (within 64 blocks)
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) < 64 * 64) {
                // TODO: Replace with your PacketHandler once created
                // PacketHandler.sendToPlayer(packet, player);

                // Temporary fallback: use vanilla particle spawning
                level.sendParticles(
                        Registry.ROCKET_FLAME.get(),
                        x, y, z,
                        50,
                        0.1, 0.1, 0.1,
                        0.1
                );
                break; // Only send once
            }
        }
    }

    /**
     * Inner class for particle packet
     */
    public static class ThrustParticlePacket {
        private final double x, y, z;
        private final double velX, velY, velZ;

        public ThrustParticlePacket(double x, double y, double z, double velX, double velY, double velZ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.velX = velX;
            this.velY = velY;
            this.velZ = velZ;
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
            buf.writeDouble(velX);
            buf.writeDouble(velY);
            buf.writeDouble(velZ);
        }

        public static ThrustParticlePacket decode(FriendlyByteBuf buf) {
            return new ThrustParticlePacket(
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
            );
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Level level = Minecraft.getInstance().level;
                if (level instanceof ClientLevel clientLevel) {
                    // Spawn 50 particles evenly distributed over the tick
                    int particleCount = 50;
                    for (int i = 0; i < particleCount; i++) {
                        final int index = i;
                        // Schedule each particle with even delay
                        scheduleParticle(clientLevel, index, particleCount);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

        private void scheduleParticle(ClientLevel level, int index, int total) {
            // Calculate delay in ticks (spread across 1 tick = 20 subticks)
            int delayTicks = (index * 20) / total;

            // Schedule the particle spawn
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    Minecraft.getInstance().execute(() -> {
                        level.addParticle(
                                Registry.ROCKET_FLAME.get(),
                                x, y, z,
                                velX, velY, velZ
                        );
                    });
                }
            }, delayTicks);
        }
    }
}