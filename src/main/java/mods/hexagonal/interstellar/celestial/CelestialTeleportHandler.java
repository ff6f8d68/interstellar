package mods.hexagonal.interstellar.celestial;

import com.mojang.logging.LogUtils;
import mods.hexagonal.interstellar.client.celestial.SkyRenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles player teleportation when entering celestial bodies.
 * Manages collision detection, teleportation logic, cooldowns, and safety checks.
 */
@Mod.EventBusSubscriber(modid = "interstellar")
public class CelestialTeleportHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Teleportation configuration
    private static final int TELEPORT_COOLDOWN_TICKS = 100; // 5 seconds at 20 TPS
    private static final float TELEPORT_HEIGHT_OFFSET = 10.0f; // Offset above celestial body center
    private static final double COLLISION_DETECTION_THRESHOLD = 0.9; // How close to center before teleport

    // Player teleport cooldown tracking
    private static final Map<UUID, Integer> playerCooldowns = new HashMap<>();

    // Return teleportation configuration
    private static final double RETURN_HEIGHT_TRIGGER = 1000.0; // Y position that triggers return teleportation
    private static final String SPACE_DIMENSION_KEY = "interstellar:space"; // Space dimension resource location

    // Player return tracking system
    private static final Map<UUID, CelestialBody> playerReturnBodies = new HashMap<>();
    private static final Map<UUID, Vec3> playerReturnLocations = new HashMap<>();

    // Dimension-to-celestial-body reverse lookup system
    private static final Map<ResourceKey<Level>, CelestialBody> dimensionToCelestialBody = new HashMap<>();

    // Default return location for unknown dimensions
    private static final Vec3 DEFAULT_SPACE_RETURN_LOCATION = new Vec3(0, 200, 0);

    // Sound and particle effects
    private static final float TELEPORT_VOLUME = 1.0f;
    private static final float TELEPORT_PITCH = 1.0f;

    /**
     * Handles player tick events to check for celestial body collisions.
     * This method runs on both client and server sides but only performs
     * teleportation logic on the server side.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        // Only process server-side teleportation logic
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Check if player is in a dimension with celestial bodies
        List<CelestialBody> celestialBodies = SkyRenderHandler.getCelestialBodiesForDimension(player.level().dimension());

        if (celestialBodies.isEmpty()) {
            return;
        }

        // Check teleport cooldown
        if (isPlayerOnCooldown(serverPlayer)) {
            updatePlayerCooldown(serverPlayer);
            return;
        }

        // Check for collision with celestial bodies
        for (CelestialBody celestialBody : celestialBodies) {
            if (shouldTeleportPlayer(serverPlayer, celestialBody)) {
                performTeleportation(serverPlayer, celestialBody);
                setPlayerCooldown(serverPlayer);
                break; // Only teleport to one celestial body per tick
            }
        }

        // Check for return teleportation conditions (height-based return to space)
        if (shouldReturnPlayerToSpace(serverPlayer)) {
            performReturnTeleportation(serverPlayer);
            setPlayerCooldown(serverPlayer); // Apply cooldown to prevent spam
        }
    }

    /**
     * Checks if a player should be teleported when colliding with a celestial body.
     *
     * @param player The player to check
     * @param celestialBody The celestial body to check collision with
     * @return true if the player should be teleported
     */
    private static boolean shouldTeleportPlayer(ServerPlayer player, CelestialBody celestialBody) {
        // Check if celestial body supports teleportation
        if (!celestialBody.canTeleport()) {
            return false;
        }

        // Check if player is in the same dimension as the celestial body
        if (!player.level().dimension().equals(celestialBody.getDimension())) {
            return false;
        }

        // Check collision with celestial body bounding box
        return isPlayerCollidingWithCelestialBody(player, celestialBody);
    }

    /**
     * Checks if a player is colliding with a celestial body's bounding box.
     *
     * @param player The player to check
     * @param celestialBody The celestial body to check collision with
     * @return true if the player is colliding with the celestial body
     */
    private static boolean isPlayerCollidingWithCelestialBody(ServerPlayer player, CelestialBody celestialBody) {
        Vec3 playerPos = player.position();
        Vec3 celestialPos = celestialBody.getCurrentPosition();

        // Calculate distance between player and celestial body center
        double distance = playerPos.distanceTo(celestialPos);

        // Get celestial body radius (half of size for collision detection)
        double celestialRadius = celestialBody.getSize() / 2.0;

        // Check if player is within the celestial body's bounding box
        // Use threshold to trigger teleportation slightly before reaching the center
        return distance <= (celestialRadius * COLLISION_DETECTION_THRESHOLD);
    }

    /**
     * Performs the actual teleportation of a player to a celestial body's destination dimension.
     *
     * @param player The player to teleport
     * @param celestialBody The celestial body being entered
     */
    private static void performTeleportation(ServerPlayer player, CelestialBody celestialBody) {
        try {
            LOGGER.info("Teleporting player {} to celestial body {} in dimension {}",
                player.getName().getString(),
                celestialBody.getDisplayName(),
                celestialBody.getTeleportDestination().location());

            // Get the destination dimension
            ServerLevel destinationLevel = player.server.getLevel(celestialBody.getTeleportDestination());

            if (destinationLevel == null) {
                LOGGER.error("Failed to get destination level for dimension: {}",
                    celestialBody.getTeleportDestination().location());
                return;
            }

            // Calculate safe teleport position in destination dimension
            Vec3 safePosition = calculateSafeTeleportPosition(celestialBody, destinationLevel);

            // Perform the teleportation
            player.teleportTo(destinationLevel, safePosition.x, safePosition.y, safePosition.z,
                player.getYRot(), player.getXRot());

            // Record return information for return teleportation
            recordReturnInformation(player, celestialBody);

            // Play teleportation effects
            playTeleportationEffects(player, celestialBody);

            LOGGER.info("Successfully teleported player {} to dimension {}",
                player.getName().getString(),
                celestialBody.getTeleportDestination().location());

        } catch (Exception e) {
            LOGGER.error("Failed to teleport player {} to celestial body {}",
                player.getName().getString(), celestialBody.getDisplayName(), e);
        }
    }

    /**
     * Calculates a safe teleport position in the destination dimension.
     * Places the player above the celestial body center with some offset.
     *
     * @param celestialBody The celestial body being entered
     * @param destinationLevel The destination dimension
     * @return Safe teleport position
     */
    private static Vec3 calculateSafeTeleportPosition(CelestialBody celestialBody, ServerLevel destinationLevel) {
        Vec3 celestialPos = celestialBody.getCurrentPosition();

        // Place player above the celestial body center
        double x = celestialPos.x;
        double y = celestialPos.y + TELEPORT_HEIGHT_OFFSET;
        double z = celestialPos.z;

        // Ensure the position is within world bounds
        y = Math.max(y, destinationLevel.getMinBuildHeight() + 10);
        y = Math.min(y, destinationLevel.getMaxBuildHeight() - 10);

        return new Vec3(x, y, z);
    }

    /**
     * Calculates an intelligent return location based on the player's current dimension.
     * Implements the enhanced return location detection with fallback logic.
     *
     * @param player The player being returned to space
     * @param spaceLevel The space dimension level
     * @return The calculated return location, or null if calculation failed
     */
    private static Vec3 calculateIntelligentReturnLocation(ServerPlayer player, ServerLevel spaceLevel) {
        // Validate inputs
        if (player == null || spaceLevel == null) {
            LOGGER.error("Cannot calculate return location: player or space level is null");
            return DEFAULT_SPACE_RETURN_LOCATION;
        }

        UUID playerUUID = player.getUUID();

        // Priority 1: Check if player has existing return tracking data (backward compatibility)
        if (playerReturnBodies.containsKey(playerUUID) && playerReturnLocations.containsKey(playerUUID)) {
            CelestialBody celestialBody = playerReturnBodies.get(playerUUID);
            Vec3 returnLocation = playerReturnLocations.get(playerUUID);

            // Validate return location
            if (celestialBody != null && returnLocation != null) {
                LOGGER.debug("Using existing return data for player {}: body={}, location={}",
                    player.getName().getString(), celestialBody.getDisplayName(), returnLocation);
                return returnLocation;
            } else {
                LOGGER.warn("Invalid return tracking data for player {}, falling back to intelligent detection",
                    player.getName().getString());
            }
        }

        // Priority 2: Check if current dimension corresponds to a celestial body
        CelestialBody celestialBody = getCelestialBodyForDimension(player.level().dimension());

        if (celestialBody != null) {
            // Player is in a celestial body dimension - return to celestial body's space location
            Vec3 celestialPos = celestialBody.getCurrentPosition();

            // Validate celestial position
            if (celestialPos != null) {
                Vec3 returnLocation = new Vec3(celestialPos.x, celestialPos.y + TELEPORT_HEIGHT_OFFSET, celestialPos.z);

                LOGGER.debug("Using celestial body location for player {}: body={}, location={}",
                    player.getName().getString(), celestialBody.getDisplayName(), returnLocation);

                return returnLocation;
            } else {
                LOGGER.warn("Invalid celestial body position for {}, using default location",
                    celestialBody.getDisplayName());
            }
        }

        // Priority 3: Fallback to default space location for unknown dimensions
        LOGGER.debug("Using default space location for player {} from unknown dimension {}",
            player.getName().getString(), player.level().dimension().location().getPath());

        return DEFAULT_SPACE_RETURN_LOCATION;
    }

    /**
     * Plays return teleportation visual and sound effects.
     * Uses a generic effect since we might not have a specific celestial body context.
     *
     * @param player The player being teleported
     */
    private static void playReturnTeleportationEffects(ServerPlayer player) {
        // Play teleportation sound
        player.playNotifySound(SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, TELEPORT_VOLUME, TELEPORT_PITCH);

        // Spawn teleportation particles at player location
        ServerLevel level = player.serverLevel();
        Vec3 playerPos = player.position();

        // Create particle effect around player
        for (int i = 0; i < 50; i++) {
            double offsetX = (Math.random() - 0.5) * 2.0;
            double offsetY = (Math.random() - 0.5) * 2.0;
            double offsetZ = (Math.random() - 0.5) * 2.0;

            level.sendParticles(ParticleTypes.PORTAL,
                playerPos.x + offsetX,
                playerPos.y + offsetY,
                playerPos.z + offsetZ,
                1, 0.0, 0.0, 0.0, 0.1);
        }

        // Send sound packet to all nearby players
        double radius = 32.0;
        List<ServerPlayer> nearbyPlayers = level.getPlayers(player1 ->
            player1.distanceToSqr(player) <= radius * radius);

        for (ServerPlayer nearbyPlayer : nearbyPlayers) {
            if (nearbyPlayer != player) {
                nearbyPlayer.playNotifySound(SoundEvents.PORTAL_TRAVEL,
                    SoundSource.PLAYERS, TELEPORT_VOLUME * 0.5f, TELEPORT_PITCH);
            }
        }
    }

    /**
     * Plays teleportation visual and sound effects.
     *
     * @param player The player being teleported
     * @param celestialBody The celestial body being entered
     */
    private static void playTeleportationEffects(ServerPlayer player, CelestialBody celestialBody) {
        // Play teleportation sound
        player.playNotifySound(SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, TELEPORT_VOLUME, TELEPORT_PITCH);

        // Spawn teleportation particles at player location
        ServerLevel level = player.serverLevel();
        Vec3 playerPos = player.position();

        // Create particle effect around player
        for (int i = 0; i < 50; i++) {
            double offsetX = (Math.random() - 0.5) * 2.0;
            double offsetY = (Math.random() - 0.5) * 2.0;
            double offsetZ = (Math.random() - 0.5) * 2.0;

            level.sendParticles(ParticleTypes.PORTAL,
                playerPos.x + offsetX,
                playerPos.y + offsetY,
                playerPos.z + offsetZ,
                1, 0.0, 0.0, 0.0, 0.1);
        }

        // Send sound packet to all nearby players
        double radius = 32.0;
        List<ServerPlayer> nearbyPlayers = level.getPlayers(player1 ->
            player1.distanceToSqr(player) <= radius * radius);

        for (ServerPlayer nearbyPlayer : nearbyPlayers) {
            if (nearbyPlayer != player) {
                nearbyPlayer.playNotifySound(SoundEvents.PORTAL_TRAVEL,
                    SoundSource.PLAYERS, TELEPORT_VOLUME * 0.5f, TELEPORT_PITCH);
            }
        }
    }

    /**
     * Checks if a player is currently on teleport cooldown.
     *
     * @param player The player to check
     * @return true if the player is on cooldown
     */
    private static boolean isPlayerOnCooldown(ServerPlayer player) {
        return playerCooldowns.getOrDefault(player.getUUID(), 0) > 0;
    }

    /**
     * Sets a teleport cooldown for a player.
     *
     * @param player The player to set cooldown for
     */
    private static void setPlayerCooldown(ServerPlayer player) {
        playerCooldowns.put(player.getUUID(), TELEPORT_COOLDOWN_TICKS);
        LOGGER.debug("Set teleport cooldown for player {} for {} ticks",
            player.getName().getString(), TELEPORT_COOLDOWN_TICKS);
    }

    /**
     * Updates and decrements a player's teleport cooldown.
     *
     * @param player The player whose cooldown to update
     */
    private static void updatePlayerCooldown(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        int currentCooldown = playerCooldowns.getOrDefault(playerUUID, 0);

        if (currentCooldown > 0) {
            playerCooldowns.put(playerUUID, currentCooldown - 1);
        } else {
            playerCooldowns.remove(playerUUID);
        }
    }

    /**
     * Gets the current teleport cooldown for a player.
     *
     * @param player The player to check
     * @return Remaining cooldown in ticks, or 0 if no cooldown
     */
    public static int getPlayerCooldown(ServerPlayer player) {
        return playerCooldowns.getOrDefault(player.getUUID(), 0);
    }

    /**
     * Manually clears a player's teleport cooldown.
     * Useful for administrative commands or special cases.
     *
     * @param player The player whose cooldown to clear
     */
    public static void clearPlayerCooldown(ServerPlayer player) {
        playerCooldowns.remove(player.getUUID());
        LOGGER.debug("Cleared teleport cooldown for player {}", player.getName().getString());
    }

    /**
     * Gets the total number of players currently on teleport cooldown.
     *
     * @return Number of players on cooldown
     */
    public static int getActiveCooldownCount() {
        return (int) playerCooldowns.values().stream().filter(cooldown -> cooldown > 0).count();
    }

    /**
     * Cleans up expired cooldowns to prevent memory leaks.
     * Should be called periodically.
     */
    public static void cleanupExpiredCooldowns() {
        playerCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    /**
     * Records return information for a player when they teleport to a celestial body.
     * This information is used for return teleportation when they reach the height trigger.
     * Also registers the dimension mapping for the enhanced return system.
     *
     * @param player The player who was teleported
     * @param celestialBody The celestial body they teleported to
     */
    private static void recordReturnInformation(ServerPlayer player, CelestialBody celestialBody) {
        UUID playerUUID = player.getUUID();

        // Store the celestial body for return tracking (backward compatibility)
        playerReturnBodies.put(playerUUID, celestialBody);

        // Calculate and store the return location in space dimension at celestial body's position
        Vec3 celestialPos = celestialBody.getCurrentPosition();
        Vec3 returnLocation = new Vec3(celestialPos.x, celestialPos.y + TELEPORT_HEIGHT_OFFSET, celestialPos.z);
        playerReturnLocations.put(playerUUID, returnLocation);

        // Register dimension mapping for enhanced return system
        registerDimensionMapping(celestialBody);

        LOGGER.debug("Recorded return information for player {}: body={}, returnPos={}, dimension={}",
            player.getName().getString(), celestialBody.getDisplayName(), returnLocation,
            celestialBody.getTeleportDestination().location().getPath());
    }

    /**
     * Checks if a player should be returned to space based on their Y position.
     * Enhanced system that works for any dimension, not just celestial ones.
     *
     * @param player The player to check
     * @return true if the player should be returned to space
     */
    private static boolean shouldReturnPlayerToSpace(ServerPlayer player) {
        // Check if player is at or above the return height trigger in any dimension
        if (player.getY() < RETURN_HEIGHT_TRIGGER) {
            return false;
        }

        // Check if player is in a celestial body dimension (has a corresponding celestial body)
        CelestialBody celestialBody = getCelestialBodyForDimension(player.level().dimension());

        if (celestialBody != null) {
            // Player is in a celestial body dimension - should return to space at celestial body's location
            return true;
        }

        // Check if player has return information (entered via celestial teleportation)
        // This maintains backward compatibility with the existing system
        UUID playerUUID = player.getUUID();
        if (playerReturnBodies.containsKey(playerUUID) && playerReturnLocations.containsKey(playerUUID)) {
            return true;
        }

        // For other dimensions (like overworld), also allow return teleportation
        // This enables manual teleportation to Y=1000 in any dimension
        return true;
    }

    /**
     * Performs return teleportation from any dimension back to space dimension.
     * Enhanced system with intelligent return location detection and fallback logic.
     *
     * @param player The player to return to space
     */
    private static void performReturnTeleportation(ServerPlayer player) {
        // Validate input
        if (player == null) {
            LOGGER.error("Cannot perform return teleportation: player is null");
            return;
        }

        try {
            LOGGER.info("Performing return teleportation for player {} from dimension {} at Y={}",
                player.getName().getString(), player.level().dimension().location().getPath(), player.getY());

            // Get the space dimension
            ServerLevel spaceLevel = player.server.getLevel(ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.tryParse(SPACE_DIMENSION_KEY)));

            if (spaceLevel == null) {
                LOGGER.error("Failed to get space dimension: {}", SPACE_DIMENSION_KEY);
                return;
            }

            // Determine return location using intelligent detection
            Vec3 returnLocation = calculateIntelligentReturnLocation(player, spaceLevel);

            if (returnLocation == null) {
                LOGGER.error("Failed to calculate return location for player {}, using default",
                    player.getName().getString());
                returnLocation = DEFAULT_SPACE_RETURN_LOCATION;
            }

            // Ensure return position is within world bounds and valid
            double y = returnLocation.y;
            y = Math.max(y, spaceLevel.getMinBuildHeight() + 10);
            y = Math.min(y, spaceLevel.getMaxBuildHeight() - 10);

            // Validate coordinates are finite numbers
            if (!Double.isFinite(returnLocation.x) || !Double.isFinite(returnLocation.z) || !Double.isFinite(y)) {
                LOGGER.error("Invalid return coordinates for player {}: x={}, z={}, y={}",
                    player.getName().getString(), returnLocation.x, returnLocation.z, y);
                returnLocation = DEFAULT_SPACE_RETURN_LOCATION;
                y = returnLocation.y;
            }

            Vec3 safeReturnLocation = new Vec3(returnLocation.x, y, returnLocation.z);

            // Perform the return teleportation
            player.teleportTo(spaceLevel, safeReturnLocation.x, safeReturnLocation.y, safeReturnLocation.z,
                player.getYRot(), player.getXRot());

            // Play return teleportation effects
            playReturnTeleportationEffects(player);

            // Clear return tracking data after successful return (for backward compatibility)
            clearPlayerReturnData(player);

            LOGGER.info("Successfully returned player {} to space dimension at {}",
                player.getName().getString(), safeReturnLocation);

        } catch (Exception e) {
            LOGGER.error("Failed to return player {} to space", player.getName().getString(), e);
        }
    }

    /**
     * Handles player dimension change events to reset return tracking.
     * This ensures players who manually change dimensions don't retain stale return data.
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Clear return data when player manually changes dimensions
            clearPlayerReturnData(serverPlayer);
            LOGGER.debug("Cleared return data for player {} due to dimension change",
                serverPlayer.getName().getString());
        }
    }

    /**
     * Clears return tracking data for a specific player.
     *
     * @param player The player whose return data to clear
     */
    private static void clearPlayerReturnData(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        playerReturnBodies.remove(playerUUID);
        playerReturnLocations.remove(playerUUID);
    }

    /**
     * Gets the celestial body a player is tracked to return from.
     *
     * @param player The player to check
     * @return The celestial body, or null if no return data exists
     */
    public static CelestialBody getPlayerReturnBody(ServerPlayer player) {
        return playerReturnBodies.get(player.getUUID());
    }

    /**
     * Gets the return location for a player.
     *
     * @param player The player to check
     * @return The return location, or null if no return data exists
     */
    public static Vec3 getPlayerReturnLocation(ServerPlayer player) {
        return playerReturnLocations.get(player.getUUID());
    }

    /**
     * Checks if a player has return tracking data.
     *
     * @param player The player to check
     * @return true if the player has return data
     */
    public static boolean hasPlayerReturnData(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        return playerReturnBodies.containsKey(playerUUID) && playerReturnLocations.containsKey(playerUUID);
    }

    /**
     * Manually clears return data for a player.
     * Useful for administrative commands or special cases.
     *
     * @param player The player whose return data to clear
     */
    public static void clearPlayerReturnData(UUID playerUUID) {
        playerReturnBodies.remove(playerUUID);
        playerReturnLocations.remove(playerUUID);
    }

    /**
     * Registers a celestial body in the dimension-to-celestial-body reverse lookup system.
     * This allows the system to find which celestial body corresponds to a given dimension.
     *
     * @param celestialBody The celestial body to register
     */
    public static void registerDimensionMapping(CelestialBody celestialBody) {
        if (celestialBody == null || celestialBody.getTeleportDestination() == null) {
            return;
        }

        ResourceKey<Level> destinationDimension = celestialBody.getTeleportDestination();
        dimensionToCelestialBody.put(destinationDimension, celestialBody);

        LOGGER.debug("Registered dimension mapping: {} -> {}",
            destinationDimension.location().getPath(), celestialBody.getDisplayName());
    }

    /**
     * Unregisters a celestial body from the dimension-to-celestial-body reverse lookup system.
     *
     * @param celestialBody The celestial body to unregister
     */
    public static void unregisterDimensionMapping(CelestialBody celestialBody) {
        if (celestialBody == null || celestialBody.getTeleportDestination() == null) {
            return;
        }

        ResourceKey<Level> destinationDimension = celestialBody.getTeleportDestination();
        dimensionToCelestialBody.remove(destinationDimension);

        LOGGER.debug("Unregistered dimension mapping: {} -> {}",
            destinationDimension.location().getPath(), celestialBody.getDisplayName());
    }

    /**
     * Gets the celestial body that corresponds to a given dimension.
     *
     * @param dimension The dimension to look up
     * @return The corresponding celestial body, or null if not found
     */
    public static CelestialBody getCelestialBodyForDimension(ResourceKey<Level> dimension) {
        return dimensionToCelestialBody.get(dimension);
    }

    /**
     * Clears all dimension mappings.
     * Useful for cleanup or reinitialization.
     */
    public static void clearAllDimensionMappings() {
        dimensionToCelestialBody.clear();
        LOGGER.debug("Cleared all dimension mappings");
    }

    /**
     * Builds the dimension-to-celestial-body reverse lookup system.
     * This method should be called after celestial bodies are registered with SkyRenderHandler.
     */
    public static void buildDimensionMappings() {
        clearAllDimensionMappings();

        // Note: This is a simplified approach. In a real implementation, you would need
        // access to all registered celestial bodies across all dimensions.
        // For now, we'll build mappings when celestial bodies are registered.

        LOGGER.info("Dimension mappings will be built dynamically as celestial bodies are registered");
    }
}