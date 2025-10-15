package mods.hexagonal.interstellar.network;

import com.mojang.logging.LogUtils;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import mods.hexagonal.interstellar.registry.CelestialRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Handles network synchronization for celestial body data between server and clients.
 * Ensures all players have up-to-date celestial body information for proper rendering
 * and interaction in multiplayer environments.
 */
public class CelestialNetworkHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("interstellar", "celestial_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    // Server lifecycle management
    private static MinecraftServer serverInstance = null;

    /**
     * Initializes the network handler and registers all packet types.
     */
    public static void initialize() {
        LOGGER.info("Initializing celestial network handler...");

        // Register packet types
        registerPackets();

        LOGGER.info("Celestial network handler initialized");
    }

    /**
     * Sets the Minecraft server instance for server level access.
     *
     * @param server The Minecraft server instance
     */
    public static void setServerInstance(MinecraftServer server) {
        serverInstance = server;
        LOGGER.debug("Server instance set for celestial network handler");
    }

    /**
     * Gets the Minecraft server instance.
     *
     * @return The Minecraft server instance, or null if not set
     */
    public static MinecraftServer getServerInstance() {
        return serverInstance;
    }

    /**
     * Registers all packet types with the network channel.
     */
    private static void registerPackets() {
        // Server to Client: Celestial body synchronization
        INSTANCE.registerMessage(
            packetId++,
            CelestialSyncPacket.class,
            CelestialSyncPacket::encode,
            CelestialSyncPacket::decode,
            CelestialSyncPacket::handle
        );

        // Server to Client: Celestial body removal
        INSTANCE.registerMessage(
            packetId++,
            CelestialRemovePacket.class,
            CelestialRemovePacket::encode,
            CelestialRemovePacket::decode,
            CelestialRemovePacket::handle
        );

        // Server to Client: Full registry synchronization
        INSTANCE.registerMessage(
            packetId++,
            CelestialRegistrySyncPacket.class,
            CelestialRegistrySyncPacket::encode,
            CelestialRegistrySyncPacket::decode,
            CelestialRegistrySyncPacket::handle
        );

        LOGGER.debug("Registered {} celestial network packet types", packetId);
    }

    /**
     * Sends celestial body synchronization data to a specific player.
     *
     * @param player The player to send data to
     * @param celestialBody The celestial body to synchronize
     * @param dimension The dimension the celestial body is in
     */
    public static void sendCelestialBodySync(ServerPlayer player, CelestialBody celestialBody, ResourceKey<Level> dimension) {
        if (player == null || celestialBody == null || dimension == null) {
            return;
        }

        try {
            CelestialSyncPacket packet = new CelestialSyncPacket(celestialBody, dimension);
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);

            LOGGER.debug("Sent celestial body sync for '{}' to player '{}'",
                celestialBody.getDisplayName(), player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("Failed to send celestial body sync to player '{}'",
                player.getName().getString(), e);
        }
    }

    /**
     * Sends celestial body removal data to a specific player.
     *
     * @param player The player to send data to
     * @param celestialBody The celestial body that was removed
     * @param dimension The dimension the celestial body was in
     */
    public static void sendCelestialBodyRemoval(ServerPlayer player, CelestialBody celestialBody, ResourceKey<Level> dimension) {
        if (player == null || celestialBody == null || dimension == null) {
            return;
        }

        try {
            CelestialRemovePacket packet = new CelestialRemovePacket(celestialBody, dimension);
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);

            LOGGER.debug("Sent celestial body removal for '{}' to player '{}'",
                celestialBody.getDisplayName(), player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("Failed to send celestial body removal to player '{}'",
                player.getName().getString(), e);
        }
    }

    /**
     * Sends full registry synchronization to a player.
     * Used when a player joins or needs a complete update.
     *
     * @param player The player to synchronize
     */
    public static void sendFullRegistrySync(ServerPlayer player) {
        if (player == null) {
            return;
        }

        try {
            // Get all celestial bodies for all dimensions the player has access to
            // For now, we'll sync all dimensions - in a real implementation,
            // you might want to filter based on player's permissions or location

            for (ResourceKey<Level> dimension : CelestialRegistry.getDimensionCount() > 0 ?
                    getAllDimensionsWithCelestialBodies() : List.of(player.level().dimension())) {

                List<CelestialBody> celestialBodies = CelestialRegistry.getCelestialBodiesForDimension(dimension);

                if (!celestialBodies.isEmpty()) {
                    CelestialRegistrySyncPacket packet = new CelestialRegistrySyncPacket(celestialBodies, dimension);
                    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
                }
            }

            // Mark player as synchronized
            CelestialRegistry.markPlayerSynchronized(player);

            LOGGER.info("Sent full registry sync to player '{}' with {} celestial bodies across {} dimensions",
                player.getName().getString(),
                CelestialRegistry.getTotalCelestialBodyCount(),
                CelestialRegistry.getDimensionCount());

        } catch (Exception e) {
            LOGGER.error("Failed to send full registry sync to player '{}'",
                player.getName().getString(), e);
        }
    }

    /**
     * Broadcasts celestial body synchronization to all players in a dimension.
     *
     * @param celestialBody The celestial body to synchronize
     * @param dimension The dimension the celestial body is in
     */
    public static void broadcastCelestialBodySync(CelestialBody celestialBody, ResourceKey<Level> dimension) {
        if (celestialBody == null || dimension == null) {
            return;
        }

        try {
            CelestialSyncPacket packet = new CelestialSyncPacket(celestialBody, dimension);

            // Get the server level for this dimension
            ServerLevel level = getServerLevel(dimension);
            if (level != null) {
                INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), packet);
                LOGGER.debug("Broadcasted celestial body sync for '{}' in dimension '{}'",
                    celestialBody.getDisplayName(), dimension.location().getPath());
            } else {
                LOGGER.warn("Cannot broadcast sync for dimension '{}': level not found",
                    dimension.location().getPath());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to broadcast celestial body sync for '{}'",
                celestialBody.getDisplayName(), e);
        }
    }

    /**
     * Broadcasts celestial body removal to all players in a dimension.
     *
     * @param celestialBody The celestial body that was removed
     * @param dimension The dimension the celestial body was in
     */
    public static void broadcastCelestialBodyRemoval(CelestialBody celestialBody, ResourceKey<Level> dimension) {
        if (celestialBody == null || dimension == null) {
            return;
        }

        try {
            CelestialRemovePacket packet = new CelestialRemovePacket(celestialBody, dimension);

            // Get the server level for this dimension
            ServerLevel level = getServerLevel(dimension);
            if (level != null) {
                INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), packet);
                LOGGER.debug("Broadcasted celestial body removal for '{}' in dimension '{}'",
                    celestialBody.getDisplayName(), dimension.location().getPath());
            } else {
                LOGGER.warn("Cannot broadcast removal for dimension '{}': level not found",
                    dimension.location().getPath());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to broadcast celestial body removal for '{}'",
                celestialBody.getDisplayName(), e);
        }
    }

    /**
     * Gets all dimensions that have celestial bodies.
     *
     * @return List of dimension keys with celestial bodies
     */
    private static List<ResourceKey<Level>> getAllDimensionsWithCelestialBodies() {
        // This is a simplified implementation
        // In a real scenario, you'd need access to the server's dimension manager
        return List.of();
    }

    /**
     * Gets the server level for a dimension.
     *
     * @param dimension The dimension to get the level for
     * @return The server level, or null if not found
     */
    private static ServerLevel getServerLevel(ResourceKey<Level> dimension) {
        try {
            if (serverInstance == null) {
                LOGGER.warn("Cannot get server level for dimension '{}': server instance not available",
                    dimension.location().getPath());
                return null;
            }

            ServerLevel level = serverInstance.getLevel(dimension);
            if (level == null) {
                LOGGER.warn("Server level not found for dimension '{}'", dimension.location().getPath());
            }

            return level;
        } catch (Exception e) {
            LOGGER.error("Failed to get server level for dimension '{}'",
                dimension.location().getPath(), e);
            return null;
        }
    }

    /**
     * Packet for synchronizing a single celestial body from server to client.
     */
    public static class CelestialSyncPacket {
        private final CelestialBody celestialBody;
        private final ResourceKey<Level> dimension;

        public CelestialSyncPacket(CelestialBody celestialBody, ResourceKey<Level> dimension) {
            this.celestialBody = celestialBody;
            this.dimension = dimension;
        }

        public static void encode(CelestialSyncPacket packet, FriendlyByteBuf buffer) {
            // Encode celestial body data
            buffer.writeBoolean(packet.celestialBody.isSun());
            buffer.writeInt(packet.celestialBody.getSize());
            buffer.writeResourceLocation(packet.celestialBody.getTexture());
            buffer.writeInt(packet.celestialBody.getPerTextureSize());
            buffer.writeBoolean(packet.celestialBody.isInOrbit());
            buffer.writeInt(packet.celestialBody.getLightColor());
            buffer.writeDouble(packet.celestialBody.getLocation().x);
            buffer.writeDouble(packet.celestialBody.getLocation().y);
            buffer.writeDouble(packet.celestialBody.getLocation().z);
            buffer.writeResourceKey(packet.dimension);

            // Encode teleport destination
            ResourceKey<Level> teleportDest = packet.celestialBody.getTeleportDestination();
            buffer.writeBoolean(teleportDest != null);
            if (teleportDest != null) {
                buffer.writeResourceKey(teleportDest);
            }
        }

        public static CelestialSyncPacket decode(FriendlyByteBuf buffer) {
            // Decode celestial body data
            boolean isSun = buffer.readBoolean();
            int size = buffer.readInt();
            ResourceLocation texture = buffer.readResourceLocation();
            int perTextureSize = buffer.readInt();
            boolean inOrbit = buffer.readBoolean();
            int lightColor = buffer.readInt();
            double x = buffer.readDouble();
            double y = buffer.readDouble();
            double z = buffer.readDouble();
            Vec3 location = new Vec3(x, y, z);
            ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);

            // Decode teleport destination
            ResourceKey<Level> teleportDest = null;
            if (buffer.readBoolean()) {
                teleportDest = buffer.readResourceKey(Registries.DIMENSION);
            }

            // Create celestial body (simplified - in reality you'd need more data)
            CelestialBody celestialBody = new CelestialBody(
                isSun, size, texture, perTextureSize, inOrbit, null,
                lightColor, location, dimension, teleportDest != null ? teleportDest : dimension
            );

            return new CelestialSyncPacket(celestialBody, dimension);
        }

        public static void handle(CelestialSyncPacket packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                // Handle on client side - register celestial body with client renderer
                try {
                    mods.hexagonal.interstellar.client.celestial.SkyRenderHandler.registerCelestialBody(
                        packet.celestialBody, packet.dimension);
                    LOGGER.debug("Client received celestial body sync for '{}'",
                        packet.celestialBody.getDisplayName());
                } catch (Exception e) {
                    LOGGER.error("Failed to handle celestial body sync on client", e);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    /**
     * Packet for removing a celestial body from client.
     */
    public static class CelestialRemovePacket {
        private final String celestialBodyName;
        private final ResourceKey<Level> dimension;

        public CelestialRemovePacket(CelestialBody celestialBody, ResourceKey<Level> dimension) {
            this.celestialBodyName = celestialBody.getDisplayName();
            this.dimension = dimension;
        }

        public static void encode(CelestialRemovePacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.celestialBodyName);
            buffer.writeResourceKey(packet.dimension);
        }

        public static CelestialRemovePacket decode(FriendlyByteBuf buffer) {
            String name = buffer.readUtf();
            ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);

            // Create a dummy celestial body for removal (client will find the real one by name)
            CelestialBody dummyBody = new CelestialBody(
                false, 1, new ResourceLocation("interstellar", "dummy"),
                1, false, null, 0, Vec3.ZERO, dimension, dimension
            );

            return new CelestialRemovePacket(dummyBody, dimension);
        }

        public static void handle(CelestialRemovePacket packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                // Handle on client side - unregister celestial body
                try {
                    // Find the celestial body by name and unregister it
                    mods.hexagonal.interstellar.client.celestial.SkyRenderHandler.unregisterCelestialBodyByName(
                        packet.celestialBodyName, packet.dimension);
                    LOGGER.debug("Client received celestial body removal for '{}'",
                        packet.celestialBodyName);
                } catch (Exception e) {
                    LOGGER.error("Failed to handle celestial body removal on client", e);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    /**
     * Packet for full registry synchronization from server to client.
     */
    public static class CelestialRegistrySyncPacket {
        private final List<CelestialBody> celestialBodies;
        private final ResourceKey<Level> dimension;

        public CelestialRegistrySyncPacket(List<CelestialBody> celestialBodies, ResourceKey<Level> dimension) {
            this.celestialBodies = celestialBodies;
            this.dimension = dimension;
        }

        public static void encode(CelestialRegistrySyncPacket packet, FriendlyByteBuf buffer) {
            buffer.writeResourceKey(packet.dimension);
            buffer.writeInt(packet.celestialBodies.size());

            for (CelestialBody body : packet.celestialBodies) {
                // Encode each celestial body (simplified encoding)
                buffer.writeBoolean(body.isSun());
                buffer.writeInt(body.getSize());
                buffer.writeResourceLocation(body.getTexture());
                buffer.writeInt(body.getPerTextureSize());
                buffer.writeBoolean(body.isInOrbit());
                buffer.writeInt(body.getLightColor());
                buffer.writeDouble(body.getLocation().x);
                buffer.writeDouble(body.getLocation().y);
                buffer.writeDouble(body.getLocation().z);

                ResourceKey<Level> teleportDest = body.getTeleportDestination();
                buffer.writeBoolean(teleportDest != null);
                if (teleportDest != null) {
                    buffer.writeResourceKey(teleportDest);
                }
            }
        }

        public static CelestialRegistrySyncPacket decode(FriendlyByteBuf buffer) {
            ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);
            int count = buffer.readInt();
            List<CelestialBody> bodies = new java.util.ArrayList<>();

            for (int i = 0; i < count; i++) {
                // Decode each celestial body (simplified decoding)
                boolean isSun = buffer.readBoolean();
                int size = buffer.readInt();
                ResourceLocation texture = buffer.readResourceLocation();
                int perTextureSize = buffer.readInt();
                boolean inOrbit = buffer.readBoolean();
                int lightColor = buffer.readInt();
                double x = buffer.readDouble();
                double y = buffer.readDouble();
                double z = buffer.readDouble();
                Vec3 location = new Vec3(x, y, z);

                ResourceKey<Level> teleportDest = null;
                if (buffer.readBoolean()) {
                    teleportDest = buffer.readResourceKey(Registries.DIMENSION);
                }

                CelestialBody body = new CelestialBody(
                    isSun, size, texture, perTextureSize, inOrbit, null,
                    lightColor, location, dimension, teleportDest != null ? teleportDest : dimension
                );

                bodies.add(body);
            }

            return new CelestialRegistrySyncPacket(bodies, dimension);
        }

        public static void handle(CelestialRegistrySyncPacket packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                // Handle on client side - register all celestial bodies
                try {
                    for (CelestialBody body : packet.celestialBodies) {
                        mods.hexagonal.interstellar.client.celestial.SkyRenderHandler.registerCelestialBody(
                            body, packet.dimension);
                    }

                    LOGGER.debug("Client received registry sync for {} celestial bodies in dimension '{}'",
                        packet.celestialBodies.size(), packet.dimension.location().getPath());
                } catch (Exception e) {
                    LOGGER.error("Failed to handle registry sync on client", e);
                }
            });
            context.get().setPacketHandled(true);
        }
    }
}