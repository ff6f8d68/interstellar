package mods.hexagonal.interstellar.registry;

import com.mojang.logging.LogUtils;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative registry for celestial bodies.
 * Manages celestial body registration, synchronization, and player-specific generation.
 * Ensures consistency across multiplayer servers and clients.
 */
public class CelestialRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Server-authoritative celestial body storage
    private static final Map<ResourceKey<Level>, Set<CelestialBody>> dimensionCelestialBodies = new ConcurrentHashMap<>();

    // Player-specific generation tracking
    private static final Map<UUID, Map<ResourceKey<Level>, Long>> playerGenerationTimes = new ConcurrentHashMap<>();

    // Network synchronization tracking
    private static final Set<UUID> synchronizedPlayers = ConcurrentHashMap.newKeySet();

    // Registry state
    private static boolean initialized = false;

    // Server lifecycle management
    private static MinecraftServer serverInstance = null;

    /**
     * Initializes the celestial registry.
     */
    public static void initialize() {
        if (initialized) {
            LOGGER.warn("Celestial registry already initialized");
            return;
        }

        LOGGER.info("Initializing celestial registry...");
        initialized = true;
        LOGGER.info("Celestial registry initialized");
    }

    /**
     * Sets the Minecraft server instance for server level access.
     *
     * @param server The Minecraft server instance
     */
    public static void setServerInstance(MinecraftServer server) {
        serverInstance = server;
        LOGGER.debug("Server instance set for celestial registry");
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
     * Registers a celestial body in the specified dimension.
     *
     * @param celestialBody The celestial body to register
     * @param dimension The dimension to register it in
     * @return true if registration was successful
     */
    public static boolean registerCelestialBody(CelestialBody celestialBody, ResourceKey<Level> dimension) {
        LOGGER.info("=== Starting registerCelestialBody ===");
        LOGGER.info("Celestial body: {} (sun: {}, size: {}, texture: {})",
            celestialBody != null ? celestialBody.getDisplayName() : "null",
            celestialBody != null ? celestialBody.isSun() : "null",
            celestialBody != null ? celestialBody.getSize() : "null",
            celestialBody != null ? celestialBody.getTexture() : "null");
        LOGGER.info("Dimension: {}", dimension != null ? dimension.location().getPath() : "null");

        if (celestialBody == null) {
            LOGGER.error("Cannot register null celestial body");
            return false;
        }

        if (dimension == null) {
            LOGGER.error("Cannot register celestial body in null dimension");
            return false;
        }

        try {
            LOGGER.info("Registry initialized: {}", initialized);

            // Get or create the set for this dimension
            LOGGER.info("Getting or creating set for dimension...");
            Set<CelestialBody> bodies = dimensionCelestialBodies.computeIfAbsent(dimension, k -> {
                LOGGER.info("Creating new set for dimension: {}", k.location().getPath());
                return ConcurrentHashMap.newKeySet();
            });
            LOGGER.info("Dimension set size: {}", bodies.size());

            // Add the celestial body
            LOGGER.info("Adding celestial body to set...");
            boolean added = bodies.add(celestialBody);
            LOGGER.info("Add result: {}", added);

            if (added) {
                LOGGER.info("Successfully registered celestial body '{}' in dimension '{}'",
                    celestialBody.getDisplayName(), dimension.location().getPath());

                // Trigger synchronization for all players in the dimension
                LOGGER.info("Triggering dimension synchronization...");
                triggerDimensionSynchronization(dimension);
                LOGGER.info("Synchronization triggered");
            } else {
                LOGGER.warn("Celestial body '{}' already exists in dimension '{}'",
                    celestialBody.getDisplayName(), dimension.location().getPath());
            }

            LOGGER.info("=== registerCelestialBody completed successfully ===");
            return added;

        } catch (Exception e) {
            LOGGER.error("=== registerCelestialBody failed ===", e);
            return false;
        }
    }

    /**
     * Unregisters a celestial body from the specified dimension.
     *
     * @param celestialBody The celestial body to unregister
     * @param dimension The dimension to unregister it from
     * @return true if unregistration was successful
     */
    public static boolean unregisterCelestialBody(CelestialBody celestialBody, ResourceKey<Level> dimension) {
        if (celestialBody == null || dimension == null) {
            return false;
        }

        try {
            Set<CelestialBody> bodies = dimensionCelestialBodies.get(dimension);

            if (bodies == null) {
                LOGGER.warn("No celestial bodies found in dimension '{}'", dimension.location().getPath());
                return false;
            }

            boolean removed = bodies.remove(celestialBody);

            if (removed) {
                LOGGER.info("Unregistered celestial body '{}' from dimension '{}'",
                    celestialBody.getDisplayName(), dimension.location().getPath());

                // Trigger synchronization for all players in the dimension
                triggerDimensionSynchronization(dimension);
            } else {
                LOGGER.warn("Celestial body '{}' not found in dimension '{}'",
                    celestialBody.getDisplayName(), dimension.location().getPath());
            }

            // Clean up empty dimension sets
            if (bodies.isEmpty()) {
                dimensionCelestialBodies.remove(dimension);
            }

            return removed;

        } catch (Exception e) {
            LOGGER.error("Failed to unregister celestial body '{}' from dimension '{}'",
                celestialBody.getDisplayName(), dimension.location().getPath(), e);
            return false;
        }
    }

    /**
     * Gets all celestial bodies for a specific dimension.
     *
     * @param dimension The dimension to get celestial bodies for
     * @return List of celestial bodies in the dimension (never null)
     */
    public static List<CelestialBody> getCelestialBodiesForDimension(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return new ArrayList<>();
        }

        Set<CelestialBody> bodies = dimensionCelestialBodies.get(dimension);
        return bodies != null ? new ArrayList<>(bodies) : new ArrayList<>();
    }

    /**
     * Gets a specific celestial body by name in a dimension.
     *
     * @param name The name of the celestial body
     * @param dimension The dimension to search in
     * @return The celestial body, or null if not found
     */
    public static CelestialBody getCelestialBodyByName(String name, ResourceKey<Level> dimension) {
        if (name == null || dimension == null) {
            return null;
        }

        List<CelestialBody> bodies = getCelestialBodiesForDimension(dimension);

        // Case-insensitive search
        return bodies.stream()
            .filter(body -> body.getDisplayName().toLowerCase().contains(name.toLowerCase()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if a celestial body exists in a dimension.
     *
     * @param celestialBody The celestial body to check
     * @param dimension The dimension to check in
     * @return true if the celestial body exists in the dimension
     */
    public static boolean hasCelestialBody(CelestialBody celestialBody, ResourceKey<Level> dimension) {
        if (celestialBody == null || dimension == null) {
            return false;
        }

        Set<CelestialBody> bodies = dimensionCelestialBodies.get(dimension);
        return bodies != null && bodies.contains(celestialBody);
    }

    /**
     * Records that a player has generated celestial bodies in a dimension.
     * Used for player-specific generation tracking.
     *
     * @param player The player who generated the bodies
     * @param dimension The dimension where generation occurred
     */
    public static void recordPlayerGeneration(ServerPlayer player, ResourceKey<Level> dimension) {
        if (player == null || dimension == null) {
            return;
        }

        UUID playerUUID = player.getUUID();
        long currentTime = System.currentTimeMillis();

        playerGenerationTimes.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
            .put(dimension, currentTime);

        LOGGER.debug("Recorded generation for player {} in dimension {} at {}",
            player.getName().getString(), dimension.location().getPath(), currentTime);
    }

    /**
     * Gets the last generation time for a player in a dimension.
     *
     * @param player The player to check
     * @param dimension The dimension to check
     * @return The last generation time, or 0 if no generation recorded
     */
    public static long getPlayerGenerationTime(ServerPlayer player, ResourceKey<Level> dimension) {
        if (player == null || dimension == null) {
            return 0;
        }

        Map<ResourceKey<Level>, Long> playerTimes = playerGenerationTimes.get(player.getUUID());
        return playerTimes != null ? playerTimes.getOrDefault(dimension, 0L) : 0L;
    }

    /**
     * Checks if a player has generated celestial bodies in a dimension.
     *
     * @param player The player to check
     * @param dimension The dimension to check
     * @return true if the player has generated bodies in the dimension
     */
    public static boolean hasPlayerGeneratedInDimension(ServerPlayer player, ResourceKey<Level> dimension) {
        return getPlayerGenerationTime(player, dimension) > 0;
    }

    /**
     * Marks a player as synchronized with the celestial registry.
     * Used for network synchronization tracking.
     *
     * @param player The player to mark as synchronized
     */
    public static void markPlayerSynchronized(ServerPlayer player) {
        if (player != null) {
            synchronizedPlayers.add(player.getUUID());
            LOGGER.debug("Marked player {} as synchronized", player.getName().getString());
        }
    }

    /**
     * Checks if a player is synchronized with the celestial registry.
     *
     * @param player The player to check
     * @return true if the player is synchronized
     */
    public static boolean isPlayerSynchronized(ServerPlayer player) {
        return player != null && synchronizedPlayers.contains(player.getUUID());
    }

    /**
     * Triggers synchronization for all players in a dimension.
     * This should be called whenever celestial bodies are added or removed.
     *
     * @param dimension The dimension that changed
     */
    private static void triggerDimensionSynchronization(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return;
        }

        try {
            // Get the server level for this dimension
            ServerLevel level = getServerLevel(dimension);
            if (level == null) {
                LOGGER.warn("Cannot trigger synchronization for dimension '{}': level not found",
                    dimension.location().getPath());
                return;
            }

            // Mark all players in the dimension as needing synchronization
            for (ServerPlayer player : level.players()) {
                synchronizedPlayers.remove(player.getUUID());
                LOGGER.debug("Marked player {} for resynchronization in dimension {}",
                    player.getName().getString(), dimension.location().getPath());
            }

            LOGGER.debug("Triggered synchronization for {} players in dimension {}",
                level.players().size(), dimension.location().getPath());

        } catch (Exception e) {
            LOGGER.error("Failed to trigger synchronization for dimension '{}'",
                dimension.location().getPath(), e);
        }
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
     * Gets the total number of celestial bodies across all dimensions.
     *
     * @return Total celestial body count
     */
    public static int getTotalCelestialBodyCount() {
        return dimensionCelestialBodies.values().stream()
            .mapToInt(Set::size)
            .sum();
    }

    /**
     * Gets the number of dimensions with celestial bodies.
     *
     * @return Number of dimensions with celestial bodies
     */
    public static int getDimensionCount() {
        return dimensionCelestialBodies.size();
    }

    /**
     * Clears all celestial bodies from all dimensions.
     * Useful for cleanup or testing.
     */
    public static void clearAllCelestialBodies() {
        int totalBodies = getTotalCelestialBodyCount();
        dimensionCelestialBodies.clear();
        playerGenerationTimes.clear();
        synchronizedPlayers.clear();

        LOGGER.info("Cleared all celestial bodies ({} total) from registry", totalBodies);
    }

    /**
     * Gets registry statistics for debugging.
     *
     * @return Map of registry statistics
     */
    public static Map<String, Object> getRegistryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_bodies", getTotalCelestialBodyCount());
        stats.put("dimensions", getDimensionCount());
        stats.put("synchronized_players", synchronizedPlayers.size());
        stats.put("players_with_generation_data", playerGenerationTimes.size());
        stats.put("initialized", initialized);
        return stats;
    }

    /**
     * Checks if the registry is initialized.
     *
     * @return true if the registry is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}