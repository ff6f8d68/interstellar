package mods.hexagonal.interstellar.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import mods.hexagonal.interstellar.celestial.SolarSystem;
import mods.hexagonal.interstellar.network.CelestialNetworkHandler;
import mods.hexagonal.interstellar.registry.CelestialRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;

/**
 * Command system for managing celestial bodies and solar systems.
 * Provides commands for creating, listing, and removing celestial bodies,
 * as well as spawning solar systems at player locations.
 */
public class CelestialCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Command constants
    private static final String MAKE_COMMAND = "make";
    private static final String SPAWN_SOLAR_COMMAND = "spawnsolar";
    private static final String SPAWN_TEST_OBJECT_COMMAND = "spawntestobject";
    private static final String LIST_COMMAND = "list";
    private static final String REMOVE_COMMAND = "remove";
    private static final String TEST_COMMAND = "test";

    // Parameter constants
    private static final String NAME_PARAM = "name";
    private static final String SIZE_PARAM = "size";
    private static final String SUN_PARAM = "isSun";
    private static final String ORBIT_PARAM = "inOrbit";
    private static final String ORBIT_RADIUS_PARAM = "orbitRadius";
    private static final String LIGHT_COLOR_PARAM = "lightColor";
    private static final String TEXTURE_PARAM = "texture";
    private static final String PER_TEXTURE_SIZE_PARAM = "perTextureSize";
    private static final String X_POS_PARAM = "x";
    private static final String Y_POS_PARAM = "y";
    private static final String Z_POS_PARAM = "z";
    private static final String DIMENSION_PARAM = "dimension";
    private static final String TELEPORT_TO_PARAM = "teleportTo";

    // Default values
    private static final int DEFAULT_SIZE = 25;
    private static final int DEFAULT_LIGHT_COLOR = 0xFFFFFFAA;
    private static final String DEFAULT_TEXTURE = "interstellar:textures/celestial/planet.png";
    private static final int DEFAULT_PER_TEXTURE_SIZE = 256;
    private static final String DEFAULT_DIMENSION = "minecraft:overworld";

    /**
     * Registers all celestial commands with the command dispatcher.
     *
     * @param dispatcher The command dispatcher to register with
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("celestial")
                .then(Commands.literal(MAKE_COMMAND)
                    .then(Commands.argument(NAME_PARAM, StringArgumentType.word())
                        // Core parameters (required)
                        .then(Commands.argument(SIZE_PARAM, IntegerArgumentType.integer(1, 1000))
                            .then(Commands.argument(TEXTURE_PARAM, StringArgumentType.string())
                                // Optional parameters that can be provided in any order
                                .then(Commands.argument(SUN_PARAM, BoolArgumentType.bool())
                                    .executes(CelestialCommand::executeMakeCommand)
                                    .then(Commands.argument(ORBIT_PARAM, BoolArgumentType.bool())
                                        .executes(CelestialCommand::executeMakeCommand)
                                        .then(Commands.argument(PER_TEXTURE_SIZE_PARAM, IntegerArgumentType.integer(1, 4096))
                                            .executes(CelestialCommand::executeMakeCommand)
                                            .then(Commands.argument(LIGHT_COLOR_PARAM, StringArgumentType.word())
                                                .executes(CelestialCommand::executeMakeCommand)
                                                .then(Commands.argument(X_POS_PARAM, DoubleArgumentType.doubleArg())
                                                    .executes(CelestialCommand::executeMakeCommand)
                                                    .then(Commands.argument(Y_POS_PARAM, DoubleArgumentType.doubleArg())
                                                        .executes(CelestialCommand::executeMakeCommand)
                                                        .then(Commands.argument(Z_POS_PARAM, DoubleArgumentType.doubleArg())
                                                            .executes(CelestialCommand::executeMakeCommand)
                                                            .then(Commands.argument(DIMENSION_PARAM, StringArgumentType.word())
                                                                .executes(CelestialCommand::executeMakeCommand)
                                                                .then(Commands.argument(TELEPORT_TO_PARAM, StringArgumentType.word())
                                                                    .executes(CelestialCommand::executeMakeCommand)
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                                // Alternative path for when sun parameter is not provided first
                                .then(Commands.argument(ORBIT_PARAM, BoolArgumentType.bool())
                                    .executes(CelestialCommand::executeMakeCommand)
                                    .then(Commands.argument(PER_TEXTURE_SIZE_PARAM, IntegerArgumentType.integer(1, 4096))
                                        .executes(CelestialCommand::executeMakeCommand)
                                        .then(Commands.argument(LIGHT_COLOR_PARAM, StringArgumentType.word())
                                            .executes(CelestialCommand::executeMakeCommand)
                                            .then(Commands.argument(X_POS_PARAM, DoubleArgumentType.doubleArg())
                                                .executes(CelestialCommand::executeMakeCommand)
                                                .then(Commands.argument(Y_POS_PARAM, DoubleArgumentType.doubleArg())
                                                    .executes(CelestialCommand::executeMakeCommand)
                                                    .then(Commands.argument(Z_POS_PARAM, DoubleArgumentType.doubleArg())
                                                        .executes(CelestialCommand::executeMakeCommand)
                                                        .then(Commands.argument(DIMENSION_PARAM, StringArgumentType.word())
                                                            .executes(CelestialCommand::executeMakeCommand)
                                                            .then(Commands.argument(TELEPORT_TO_PARAM, StringArgumentType.word())
                                                                .executes(CelestialCommand::executeMakeCommand)
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                                // Path for perTextureSize as first optional parameter
                                .then(Commands.argument(PER_TEXTURE_SIZE_PARAM, IntegerArgumentType.integer(1, 4096))
                                    .executes(CelestialCommand::executeMakeCommand)
                                    .then(Commands.argument(LIGHT_COLOR_PARAM, StringArgumentType.word())
                                        .executes(CelestialCommand::executeMakeCommand)
                                        .then(Commands.argument(X_POS_PARAM, DoubleArgumentType.doubleArg())
                                            .executes(CelestialCommand::executeMakeCommand)
                                            .then(Commands.argument(Y_POS_PARAM, DoubleArgumentType.doubleArg())
                                                .executes(CelestialCommand::executeMakeCommand)
                                                .then(Commands.argument(Z_POS_PARAM, DoubleArgumentType.doubleArg())
                                                    .executes(CelestialCommand::executeMakeCommand)
                                                    .then(Commands.argument(DIMENSION_PARAM, StringArgumentType.word())
                                                        .executes(CelestialCommand::executeMakeCommand)
                                                        .then(Commands.argument(TELEPORT_TO_PARAM, StringArgumentType.word())
                                                            .executes(CelestialCommand::executeMakeCommand)
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                                // Path for lightColor as first optional parameter
                                .then(Commands.argument(LIGHT_COLOR_PARAM, StringArgumentType.word())
                                    .executes(CelestialCommand::executeMakeCommand)
                                    .then(Commands.argument(X_POS_PARAM, DoubleArgumentType.doubleArg())
                                        .executes(CelestialCommand::executeMakeCommand)
                                        .then(Commands.argument(Y_POS_PARAM, DoubleArgumentType.doubleArg())
                                            .executes(CelestialCommand::executeMakeCommand)
                                            .then(Commands.argument(Z_POS_PARAM, DoubleArgumentType.doubleArg())
                                                .executes(CelestialCommand::executeMakeCommand)
                                                .then(Commands.argument(DIMENSION_PARAM, StringArgumentType.word())
                                                    .executes(CelestialCommand::executeMakeCommand)
                                                    .then(Commands.argument(TELEPORT_TO_PARAM, StringArgumentType.word())
                                                        .executes(CelestialCommand::executeMakeCommand)
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                                // Path for position coordinates as first optional parameters
                                .then(Commands.argument(X_POS_PARAM, DoubleArgumentType.doubleArg())
                                    .executes(CelestialCommand::executeMakeCommand)
                                    .then(Commands.argument(Y_POS_PARAM, DoubleArgumentType.doubleArg())
                                        .executes(CelestialCommand::executeMakeCommand)
                                        .then(Commands.argument(Z_POS_PARAM, DoubleArgumentType.doubleArg())
                                            .executes(CelestialCommand::executeMakeCommand)
                                            .then(Commands.argument(DIMENSION_PARAM, StringArgumentType.word())
                                                .executes(CelestialCommand::executeMakeCommand)
                                                .then(Commands.argument(TELEPORT_TO_PARAM, StringArgumentType.word())
                                                    .executes(CelestialCommand::executeMakeCommand)
                                                )
                                            )
                                        )
                                    )
                                )
                                // Path for dimension as first optional parameter
                                .then(Commands.argument(DIMENSION_PARAM, StringArgumentType.word())
                                    .executes(CelestialCommand::executeMakeCommand)
                                    .then(Commands.argument(TELEPORT_TO_PARAM, StringArgumentType.word())
                                        .executes(CelestialCommand::executeMakeCommand)
                                    )
                                )
                                // Path for teleport destination as first optional parameter
                                .then(Commands.argument(TELEPORT_TO_PARAM, StringArgumentType.word())
                                    .executes(CelestialCommand::executeMakeCommand)
                                )
                                // Execute with just core parameters
                                .executes(CelestialCommand::executeMakeCommand)
                            )
                        )
                    )
                    .executes(CelestialCommand::showMakeUsage)
                )
                .then(Commands.literal(SPAWN_SOLAR_COMMAND)
                    .executes(CelestialCommand::executeSpawnSolarCommand)
                )
                .then(Commands.literal(SPAWN_TEST_OBJECT_COMMAND)
                    .executes(CelestialCommand::executeSpawnTestObjectCommand)
                    .then(Commands.argument(SIZE_PARAM, IntegerArgumentType.integer(1, 1000))
                        .executes(CelestialCommand::executeSpawnTestObjectCommandWithSize)
                    )
                )
                .then(Commands.literal(LIST_COMMAND)
                    .executes(CelestialCommand::executeListCommand)
                )
                .then(Commands.literal(REMOVE_COMMAND)
                    .then(Commands.argument(NAME_PARAM, StringArgumentType.word())
                        .executes(CelestialCommand::executeRemoveCommand)
                    )
                    .executes(CelestialCommand::showRemoveUsage)
                )
                .then(Commands.literal("help")
                    .executes(CelestialCommand::showGeneralHelp)
                    .then(Commands.argument("subcommand", StringArgumentType.word())
                        .executes(CelestialCommand::showSubcommandHelp)
                    )
                )
        );

        LOGGER.info("Registered celestial commands");
    }

    /**
     * Executes the make command with all parameters.
     */
    private static int executeMakeCommand(CommandContext<CommandSourceStack> context) {
        try {
            String name = StringArgumentType.getString(context, NAME_PARAM);
            int size = IntegerArgumentType.getInteger(context, SIZE_PARAM);
            String texture = StringArgumentType.getString(context, TEXTURE_PARAM);

            // Get optional parameters with sensible defaults
            boolean isSun = getBooleanParameter(context, SUN_PARAM, false);
            boolean inOrbit = getBooleanParameter(context, ORBIT_PARAM, !isSun); // Suns don't orbit by default
            int perTextureSize = getIntegerParameter(context, PER_TEXTURE_SIZE_PARAM, DEFAULT_PER_TEXTURE_SIZE);
            int lightColor = getLightColorParameter(context, LIGHT_COLOR_PARAM, isSun ? DEFAULT_LIGHT_COLOR : 0xFF6666FF);

            // Get position parameters with defaults
            Vec3 location = getLocationParameter(context);

            // Get dimension parameters with defaults
            ResourceKey<Level> dimension = getDimensionParameter(context, DIMENSION_PARAM);
            ResourceKey<Level> teleportTo = getDimensionParameter(context, TELEPORT_TO_PARAM);

            return createCelestialBody(context, name, size, texture, isSun, inOrbit, perTextureSize,
                                     lightColor, location, dimension, teleportTo);
        } catch (Exception e) {
            LOGGER.error("Failed to execute make command", e);
            context.getSource().sendFailure(Component.literal("Error creating celestial body: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Executes the make command with simplified parameters.
     * This method is kept for backward compatibility but now delegates to the main method.
     */
    private static int executeMakeCommandSimple(CommandContext<CommandSourceStack> context) {
        // Delegate to the main execute method which handles all parameters properly
        return executeMakeCommand(context);
    }

    /**
     * Gets a boolean parameter with a default value if not provided.
     */
    private static boolean getBooleanParameter(CommandContext<CommandSourceStack> context, String paramName, boolean defaultValue) {
        try {
            return BoolArgumentType.getBool(context, paramName);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Gets an integer parameter with a default value if not provided.
     */
    private static int getIntegerParameter(CommandContext<CommandSourceStack> context, String paramName, int defaultValue) {
        try {
            return IntegerArgumentType.getInteger(context, paramName);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a light color parameter, parsing hex color strings or using defaults.
     */
    private static int getLightColorParameter(CommandContext<CommandSourceStack> context, String paramName, int defaultValue) {
        try {
            String colorStr = StringArgumentType.getString(context, paramName);
            // Handle hex color strings like "FFFFFF" or "0xFFFFFF"
            if (colorStr.startsWith("0x")) {
                return (int) Long.parseLong(colorStr.substring(2), 16) | 0xFF000000; // Add alpha channel
            } else {
                return (int) Long.parseLong(colorStr, 16) | 0xFF000000; // Add alpha channel
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid light color format, using default: {}", defaultValue);
            return defaultValue;
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Gets location coordinates with defaults based on player position.
     */
    private static Vec3 getLocationParameter(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                throw new IllegalArgumentException("This command can only be executed by a player");
            }

            // Try to get provided coordinates, otherwise use player position
            double x = getDoubleParameter(context, X_POS_PARAM, player.getX());
            double y = getDoubleParameter(context, Y_POS_PARAM, player.getY());
            double z = getDoubleParameter(context, Z_POS_PARAM, player.getZ());

            return new Vec3(x, y, z);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /**
     * Gets a double parameter with a default value if not provided.
     */
    private static double getDoubleParameter(CommandContext<CommandSourceStack> context, String paramName, double defaultValue) {
        try {
            return DoubleArgumentType.getDouble(context, paramName);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a dimension parameter, parsing dimension keys or using defaults.
     */
    private static ResourceKey<Level> getDimensionParameter(CommandContext<CommandSourceStack> context, String paramName) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                throw new IllegalArgumentException("This command can only be executed by a player");
            }

            String dimensionStr = StringArgumentType.getString(context, paramName);
            ResourceKey<Level> dimension = parseDimensionKey(dimensionStr);

            if (dimension == null) {
                LOGGER.warn("Invalid dimension format '{}', using player dimension", dimensionStr);
                return player.level().dimension();
            }

            return dimension;
        } catch (IllegalArgumentException e) {
            // Use player dimension as default
            return context.getSource().getPlayer().level().dimension();
        }
    }

    /**
     * Parses a dimension key string into a ResourceKey<Level>.
     */
    private static ResourceKey<Level> parseDimensionKey(String dimensionStr) {
        try {
            if (dimensionStr.contains(":")) {
                ResourceLocation location = ResourceLocation.tryParse(dimensionStr);
                if (location != null) {
                    return ResourceKey.create(Registries.DIMENSION, location);
                }
            } else {
                // Default to minecraft namespace if not provided
                ResourceLocation location = ResourceLocation.tryParse("minecraft:" + dimensionStr);
                if (location != null) {
                    return ResourceKey.create(Registries.DIMENSION, location);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse dimension key: {}", dimensionStr, e);
        }
        return null;
    }

    /**
     * Creates a celestial body with the specified parameters.
     */
    private static int createCelestialBody(CommandContext<CommandSourceStack> context,
                                          String name, int size, String texture, boolean isSun, boolean inOrbit,
                                          int perTextureSize, int lightColor, Vec3 location,
                                          ResourceKey<Level> dimension, ResourceKey<Level> teleportTo) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                source.sendFailure(Component.literal("This command can only be executed by a player"));
                return 0;
            }

            // Validate texture format
            ResourceLocation textureLocation = validateTexture(texture);
            if (textureLocation == null) {
                source.sendFailure(Component.literal("Invalid texture format. Use 'modid:path/to/texture.png'"));
                return 0;
            }

            // Validate parameters
            validateCreateParameters(size, perTextureSize, location, dimension, teleportTo);

            // Create celestial body with all specified parameters
            CelestialBody celestialBody = new CelestialBody(
                isSun,
                size,
                textureLocation,
                perTextureSize,
                inOrbit,
                null, // orbitsAround (will be set later if needed)
                lightColor,
                location,
                dimension,
                teleportTo != null ? teleportTo : dimension // Use same dimension for teleport if not specified
            );

            // Register the celestial body
            LOGGER.info("Registering celestial body '{}' in dimension '{}'", name, dimension.location().getPath());
            boolean registered = CelestialRegistry.registerCelestialBody(celestialBody, dimension);

            if (!registered) {
                LOGGER.error("Failed to register celestial body: {}", name);
                source.sendFailure(Component.literal("Failed to register celestial body: " + name));
                return 0;
            }

            LOGGER.info("Successfully registered celestial body: {}", name);

            // Send network synchronization to all players in the dimension
            LOGGER.info("Sending network sync for celestial body: {}", name);
            CelestialNetworkHandler.broadcastCelestialBodySync(celestialBody, dimension);

            // Create detailed success message
            String dimensionName = dimension.location().getPath();
            String teleportDimensionName = (teleportTo != null && !teleportTo.equals(dimension))
                ? teleportTo.location().getPath() : dimensionName;

            source.sendSuccess(() ->
                Component.literal(String.format(
                    "Created celestial body '%s' at (%.1f, %.1f, %.1f) in %s (size: %d, sun: %s, texture size: %d, teleport to: %s)",
                    name, location.x, location.y, location.z, dimensionName, size, isSun, perTextureSize, teleportDimensionName)), true);

            LOGGER.info("Player {} created celestial body '{}' at {} in dimension {} (teleport to: {})",
                player.getName().getString(), name, location, dimensionName,
                teleportDimensionName);
            return 1;

        } catch (Exception e) {
            LOGGER.error("Failed to create celestial body", e);
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
      * Executes the spawn solar command.
      */
     private static int executeSpawnSolarCommand(CommandContext<CommandSourceStack> context) {
         try {
             LOGGER.info("=== Starting executeSpawnSolarCommand ===");
             CommandSourceStack source = context.getSource();
             ServerPlayer player = source.getPlayer();

             if (player == null) {
                 LOGGER.error("Player is null - command can only be executed by a player");
                 source.sendFailure(Component.literal("This command can only be executed by a player"));
                 return 0;
             }

             LOGGER.info("Player: {}", player.getName().getString());
             Vec3 playerPos = player.position();
             ResourceKey<Level> currentDimension = player.level().dimension();
             LOGGER.info("Player position: {}, Dimension: {}", playerPos, currentDimension.location().getPath());

             // Check if registry is initialized
             if (!CelestialRegistry.isInitialized()) {
                 LOGGER.error("CelestialRegistry is not initialized!");
                 source.sendFailure(Component.literal("Celestial registry is not initialized"));
                 return 0;
             }
             LOGGER.info("CelestialRegistry is initialized");

             // Generate solar system at player's location
             LOGGER.info("Creating SolarSystem instance...");
             SolarSystem solarSystem = new SolarSystem(currentDimension);
             LOGGER.info("SolarSystem instance created successfully");

             LOGGER.info("Calling generateSolarSystemAtLocation...");
             List<CelestialBody> celestialBodies = solarSystem.generateSolarSystemAtLocation(playerPos);
             LOGGER.info("generateSolarSystemAtLocation completed, generated {} celestial bodies", celestialBodies.size());

             // Register all celestial bodies
             LOGGER.info("Registering {} celestial bodies...", celestialBodies.size());
             for (int i = 0; i < celestialBodies.size(); i++) {
                 CelestialBody celestialBody = celestialBodies.get(i);
                 LOGGER.info("Registering body {}: {} (sun: {}, size: {}, texture: {})",
                     i + 1, celestialBody.getDisplayName(), celestialBody.isSun(),
                     celestialBody.getSize(), celestialBody.getTexture());
                 boolean registered = CelestialRegistry.registerCelestialBody(celestialBody, currentDimension);
                 if (!registered) {
                     LOGGER.error("Failed to register celestial body: {}", celestialBody.getDisplayName());
                     source.sendFailure(Component.literal("Failed to register celestial body: " + celestialBody.getDisplayName()));
                     return 0;
                 }
                 LOGGER.info("Successfully registered celestial body: {}", celestialBody.getDisplayName());
             }

             LOGGER.info("All celestial bodies registered successfully");

             // Send network synchronization to all players in the dimension
             LOGGER.info("=== Starting network synchronization ===");
             for (int i = 0; i < celestialBodies.size(); i++) {
                 CelestialBody celestialBody = celestialBodies.get(i);
                 LOGGER.info("Sending network sync for celestial body {}: {}", i + 1, celestialBody.getDisplayName());

                 // Broadcast synchronization to all players in the dimension
                 CelestialNetworkHandler.broadcastCelestialBodySync(celestialBody, currentDimension);
                 LOGGER.info("Network sync sent for celestial body: {}", celestialBody.getDisplayName());
             }

             // Also send full registry sync to the executing player for immediate feedback
             LOGGER.info("Sending full registry sync to player: {}", player.getName().getString());
             CelestialNetworkHandler.sendFullRegistrySync(player);
             LOGGER.info("Full registry sync sent to player");

             LOGGER.info("=== Network synchronization completed ===");

             source.sendSuccess(() ->
                 Component.literal(String.format("Spawned solar system with %d celestial bodies at your location",
                     celestialBodies.size())), true);

             LOGGER.info("Player {} spawned solar system at {} with {} bodies",
                 player.getName().getString(), playerPos, celestialBodies.size());
             LOGGER.info("=== executeSpawnSolarCommand completed successfully ===");
             return 1;

         } catch (Exception e) {
             LOGGER.error("=== executeSpawnSolarCommand failed with exception ===", e);
             context.getSource().sendFailure(Component.literal("Error spawning solar system: " + e.getMessage()));
             return 0;
         }
     }

    /**
     * Executes the spawn test object command.
     */
    private static int executeSpawnTestObjectCommand(CommandContext<CommandSourceStack> context) {
        try {
            LOGGER.info("=== Starting executeSpawnTestObjectCommand ===");
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                LOGGER.error("Player is null - command can only be executed by a player");
                source.sendFailure(Component.literal("This command can only be executed by a player"));
                return 0;
            }

            LOGGER.info("Player: {}", player.getName().getString());
            Vec3 playerPos = player.position();
            ResourceKey<Level> currentDimension = player.level().dimension();
            LOGGER.info("Player position: {}, Dimension: {}", playerPos, currentDimension.location().getPath());

            // Check if registry is initialized
            if (!CelestialRegistry.isInitialized()) {
                LOGGER.error("CelestialRegistry is not initialized!");
                source.sendFailure(Component.literal("Celestial registry is not initialized"));
                return 0;
            }
            LOGGER.info("CelestialRegistry is initialized");

            // Create test celestial body at player's location with default size
            LOGGER.info("Creating test celestial body...");
            CelestialBody testBody = createTestCelestialBody(playerPos, currentDimension, DEFAULT_SIZE);
            LOGGER.info("Test celestial body created successfully");

            // Register the celestial body
            LOGGER.info("Registering test celestial body...");
            boolean registered = CelestialRegistry.registerCelestialBody(testBody, currentDimension);
            if (!registered) {
                LOGGER.error("Failed to register test celestial body");
                source.sendFailure(Component.literal("Failed to register test celestial body"));
                return 0;
            }
            LOGGER.info("Test celestial body registered successfully");

            // Send network synchronization to all players in the dimension
            LOGGER.info("Sending network sync for test celestial body");
            CelestialNetworkHandler.broadcastCelestialBodySync(testBody, currentDimension);

            // Also send full registry sync to the executing player for immediate feedback
            LOGGER.info("Sending full registry sync to player: {}", player.getName().getString());
            CelestialNetworkHandler.sendFullRegistrySync(player);

            source.sendSuccess(() ->
                Component.literal(String.format("Spawned test celestial body '%s' at your location (size: %d, sun: %s)",
                    testBody.getDisplayName(), testBody.getSize(), testBody.isSun())), true);

            LOGGER.info("Player {} spawned test celestial body at {} in dimension {}",
                player.getName().getString(), playerPos, currentDimension.location().getPath());
            LOGGER.info("=== executeSpawnTestObjectCommand completed successfully ===");
            return 1;

        } catch (Exception e) {
            LOGGER.error("=== executeSpawnTestObjectCommand failed with exception ===", e);
            context.getSource().sendFailure(Component.literal("Error spawning test object: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Executes the spawn test object command with size parameter.
     */
    private static int executeSpawnTestObjectCommandWithSize(CommandContext<CommandSourceStack> context) {
        try {
            LOGGER.info("=== Starting executeSpawnTestObjectCommandWithSize ===");
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                LOGGER.error("Player is null - command can only be executed by a player");
                source.sendFailure(Component.literal("This command can only be executed by a player"));
                return 0;
            }

            // Get the size parameter
            int size = IntegerArgumentType.getInteger(context, SIZE_PARAM);

            LOGGER.info("Player: {}, Size: {}", player.getName().getString(), size);
            Vec3 playerPos = player.position();
            ResourceKey<Level> currentDimension = player.level().dimension();
            LOGGER.info("Player position: {}, Dimension: {}", playerPos, currentDimension.location().getPath());

            // Check if registry is initialized
            if (!CelestialRegistry.isInitialized()) {
                LOGGER.error("CelestialRegistry is not initialized!");
                source.sendFailure(Component.literal("Celestial registry is not initialized"));
                return 0;
            }
            LOGGER.info("CelestialRegistry is initialized");

            // Create test celestial body at player's location with specified size
            LOGGER.info("Creating test celestial body with size {}...", size);
            CelestialBody testBody = createTestCelestialBody(playerPos, currentDimension, size);
            LOGGER.info("Test celestial body created successfully");

            // Register the celestial body
            LOGGER.info("Registering test celestial body...");
            boolean registered = CelestialRegistry.registerCelestialBody(testBody, currentDimension);
            if (!registered) {
                LOGGER.error("Failed to register test celestial body");
                source.sendFailure(Component.literal("Failed to register test celestial body"));
                return 0;
            }
            LOGGER.info("Test celestial body registered successfully");

            // Send network synchronization to all players in the dimension
            LOGGER.info("Sending network sync for test celestial body");
            CelestialNetworkHandler.broadcastCelestialBodySync(testBody, currentDimension);

            // Also send full registry sync to the executing player for immediate feedback
            LOGGER.info("Sending full registry sync to player: {}", player.getName().getString());
            CelestialNetworkHandler.sendFullRegistrySync(player);

            source.sendSuccess(() ->
                Component.literal(String.format("Spawned test celestial body '%s' at your location (size: %d, sun: %s)",
                    testBody.getDisplayName(), testBody.getSize(), testBody.isSun())), true);

            LOGGER.info("Player {} spawned test celestial body at {} in dimension {} with size {}",
                player.getName().getString(), playerPos, currentDimension.location().getPath(), size);
            LOGGER.info("=== executeSpawnTestObjectCommandWithSize completed successfully ===");
            return 1;

        } catch (Exception e) {
            LOGGER.error("=== executeSpawnTestObjectCommandWithSize failed with exception ===", e);
            context.getSource().sendFailure(Component.literal("Error spawning test object: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Executes the list command.
     */
    private static int executeListCommand(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                source.sendFailure(Component.literal("This command can only be executed by a player"));
                return 0;
            }

            ResourceKey<Level> currentDimension = player.level().dimension();
            List<CelestialBody> celestialBodies = CelestialRegistry.getCelestialBodiesForDimension(currentDimension);

            if (celestialBodies.isEmpty()) {
                source.sendSuccess(() ->
                    Component.literal("No celestial bodies found in current dimension"), false);
                return 1;
            }

            source.sendSuccess(() ->
                Component.literal(String.format("Celestial bodies in %s:", currentDimension.location().getPath())), false);

            for (int i = 0; i < celestialBodies.size(); i++) {
                CelestialBody body = celestialBodies.get(i);
                String info = String.format("  %d. %s (size: %d, sun: %s, pos: %.1f, %.1f, %.1f)",
                    i + 1, body.getDisplayName(), body.getSize(), body.isSun(),
                    body.getLocation().x, body.getLocation().y, body.getLocation().z);

                source.sendSuccess(() -> Component.literal(info), false);
            }

            return 1;

        } catch (Exception e) {
            LOGGER.error("Failed to list celestial bodies", e);
            context.getSource().sendFailure(Component.literal("Error listing celestial bodies: " + e.getMessage()));
            return 0;
        }
    }

    /**
      * Executes the remove command.
      */
     private static int executeRemoveCommand(CommandContext<CommandSourceStack> context) {
         try {
             String name = StringArgumentType.getString(context, NAME_PARAM);
             CommandSourceStack source = context.getSource();
             ServerPlayer player = source.getPlayer();

             if (player == null) {
                 source.sendFailure(Component.literal("This command can only be executed by a player"));
                 return 0;
             }

             ResourceKey<Level> currentDimension = player.level().dimension();
             List<CelestialBody> celestialBodies = CelestialRegistry.getCelestialBodiesForDimension(currentDimension);

             // Find celestial body by name (case-insensitive partial match)
             final CelestialBody toRemove = findCelestialBodyByName(celestialBodies, name);

             if (toRemove == null) {
                 source.sendFailure(Component.literal("Celestial body '" + name + "' not found in current dimension"));
                 return 0;
             }

             // Remove the celestial body
             LOGGER.info("Removing celestial body '{}' from dimension '{}'", toRemove.getDisplayName(), currentDimension.location().getPath());
             boolean removed = CelestialRegistry.unregisterCelestialBody(toRemove, currentDimension);

             if (!removed) {
                 LOGGER.error("Failed to remove celestial body: {}", toRemove.getDisplayName());
                 source.sendFailure(Component.literal("Failed to remove celestial body: " + toRemove.getDisplayName()));
                 return 0;
             }

             LOGGER.info("Successfully removed celestial body: {}", toRemove.getDisplayName());

             // Send network removal notification to all players in the dimension
             LOGGER.info("Sending network removal notification for celestial body: {}", toRemove.getDisplayName());
             CelestialNetworkHandler.broadcastCelestialBodyRemoval(toRemove, currentDimension);

             source.sendSuccess(() ->
                 Component.literal("Removed celestial body '" + toRemove.getDisplayName() + "'"), true);

             LOGGER.info("Player {} removed celestial body '{}' from {}",
                 player.getName().getString(), toRemove.getDisplayName(), currentDimension.location().getPath());
             return 1;

         } catch (Exception e) {
             LOGGER.error("Failed to remove celestial body", e);
             context.getSource().sendFailure(Component.literal("Error removing celestial body: " + e.getMessage()));
             return 0;
         }
     }

    /**
     * Shows usage information for the make command.
     */
    private static int showMakeUsage(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("Usage: /celestial make <name> <size> <texture> [optional parameters...]"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("Required parameters:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  <name> - Name of the celestial body"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  <size> - Size (1-1000)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  <texture> - Texture resource location (e.g., interstellar:textures/celestial/planet.png)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("Optional parameters (can be in any order):"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  isSun <true/false> - Whether this is a sun (default: false)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  inOrbit <true/false> - Whether this body orbits (default: true for non-suns)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  perTextureSize <size> - Cubic texture size in pixels (default: 256)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  lightColor <hex> - Light color as hex (e.g., FFFFFF or 0xFFFFFF)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  x <x> y <y> z <z> - Position coordinates (default: player position)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  dimension <dim> - Dimension key (e.g., minecraft:overworld)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  teleportTo <dim> - Teleport destination dimension"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("Examples:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  /celestial make Earth 25 interstellar:textures/celestial/earth.png"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  /celestial make Sun 100 interstellar:textures/celestial/sun.png isSun true inOrbit false"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  /celestial make Mars 15 interstellar:textures/celestial/mars.png x 100 y 80 z 200 dimension minecraft:overworld"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  /celestial make Moon 8 interstellar:textures/celestial/moon.png inOrbit true perTextureSize 128 lightColor CCCCCC"), false);
        return 1;
    }

    /**
      * Shows usage information for the remove command.
      */
     private static int showRemoveUsage(CommandContext<CommandSourceStack> context) {
         context.getSource().sendSuccess(() ->
             Component.literal("Usage: /celestial remove <name>"), false);
         context.getSource().sendSuccess(() ->
             Component.literal("Example: /celestial remove Earth"), false);
         return 1;
     }

     /**
      * Finds a celestial body by name (case-insensitive partial match).
      *
      * @param celestialBodies The list of celestial bodies to search in
      * @param name The name to search for
      * @return The found celestial body, or null if not found
      */
     private static CelestialBody findCelestialBodyByName(List<CelestialBody> celestialBodies, String name) {
         for (CelestialBody body : celestialBodies) {
             if (body.getDisplayName().toLowerCase().contains(name.toLowerCase())) {
                 return body;
             }
         }
         return null;
     }

    /**
     * Validates parameters for celestial body creation.
     */
    private static void validateCreateParameters(int size, int perTextureSize, Vec3 location,
                                               ResourceKey<Level> dimension, ResourceKey<Level> teleportTo) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive, got: " + size);
        }
        if (perTextureSize <= 0) {
            throw new IllegalArgumentException("Per texture size must be positive, got: " + perTextureSize);
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (dimension == null) {
            throw new IllegalArgumentException("Dimension cannot be null");
        }
        if (teleportTo == null) {
            throw new IllegalArgumentException("Teleport destination dimension cannot be null");
        }
    }

    /**
     * Validates and parses a texture resource location.
     */
    private static ResourceLocation validateTexture(String texture) {
        try {
            if (texture.contains(":")) {
                // Use tryParse for single parameter with namespace:path format
                return ResourceLocation.tryParse(texture);
            } else {
                // Use constructor for separate namespace and path
                return new ResourceLocation("interstellar", texture);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a test celestial body with OBJ model support at the specified location.
     */
    private static CelestialBody createTestCelestialBody(Vec3 location, ResourceKey<Level> dimension, int size) {
        LOGGER.info("Creating test celestial body at {} in dimension {} with size {}", location, dimension.location().getPath(), size);

        // Test parameters for OBJ model celestial body
        boolean isSun = false; // Default to planet for testing OBJ models
        String texturePath = "interstellar:planetbase.png";
        ResourceLocation texture = validateTexture(texturePath);

        if (texture == null) {
            throw new IllegalArgumentException("Invalid texture format: " + texturePath);
        }

        int perTextureSize = DEFAULT_PER_TEXTURE_SIZE;
        boolean inOrbit = false; // Test objects don't orbit by default
        int lightColor = isSun ? DEFAULT_LIGHT_COLOR : 0xFF6666FF; // Blue for planets, white for suns

        // Create celestial body with OBJ model support
        CelestialBody celestialBody = new CelestialBody(
            isSun,
            size,
            texture,
            perTextureSize,
            inOrbit,
            null, // No orbit target for test objects
            lightColor,
            location,
            dimension,
            dimension // Teleport to same dimension for simplicity
        );

        LOGGER.info("Test celestial body created: {} (sun: {}, size: {}, texture: {})",
            celestialBody.getDisplayName(), isSun, size, texturePath);
        return celestialBody;
    }

    /**
     * Shows general help for all celestial commands.
     */
    private static int showGeneralHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§6=== Celestial Commands Help ==="), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e/celestial make <name> <size> <texture> [options...]§f - Create a celestial body"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e/celestial spawnsolar§f - Spawn a complete solar system at your location"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e/celestial spawntestobject§f - Spawn a test OBJ model celestial body"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e/celestial list§f - List all celestial bodies in current dimension"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e/celestial remove <name>§f - Remove a celestial body"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§e/celestial help [subcommand]§f - Show help for all commands or specific subcommand"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§7Use '/celestial help <subcommand>' for detailed help on a specific command"), false);
        return 1;
    }

    /**
     * Shows help for a specific subcommand.
     */
    private static int showSubcommandHelp(CommandContext<CommandSourceStack> context) {
        try {
            String subcommand = StringArgumentType.getString(context, "subcommand").toLowerCase();

            switch (subcommand) {
                case MAKE_COMMAND:
                    return showMakeHelp(context);
                case "spawn":
                case "spawnsolar":
                    return showSpawnSolarHelp(context);
                case SPAWN_TEST_OBJECT_COMMAND:
                    return showSpawnTestObjectHelp(context);
                case LIST_COMMAND:
                    return showListHelp(context);
                case REMOVE_COMMAND:
                    return showRemoveHelp(context);
                case "help":
                    return showGeneralHelp(context);
                default:
                    context.getSource().sendFailure(Component.literal("Unknown subcommand: " + subcommand));
                    context.getSource().sendSuccess(() ->
                        Component.literal("Use '/celestial help' to see all available commands"), false);
                    return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to show subcommand help", e);
            context.getSource().sendFailure(Component.literal("Error showing help: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Shows detailed help for the make command.
     */
    private static int showMakeHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§6=== Make Command Help ==="), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eSyntax:§f /celestial make <name> <size> <texture> [optional parameters...]"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);

        context.getSource().sendSuccess(() ->
            Component.literal("§eRequired Parameters:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §b<name>§f - Name of the celestial body"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §b<size>§f - Size of the body (1-1000)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §b<texture>§f - Texture resource location (e.g., interstellar:textures/celestial/planet.png)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);

        context.getSource().sendSuccess(() ->
            Component.literal("§eOptional Parameters (can be used in any order):"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §aisSun <true/false>§f - Whether this is a sun (default: false)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §ainOrbit <true/false>§f - Whether this body orbits (default: true for non-suns)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §aperTextureSize <size>§f - Cubic texture size in pixels (default: 256, max: 4096)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §alightColor <hex>§f - Light color as hex value (e.g., FFFFFF or 0xFFFFFF)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §ax <x> y <y> z <z>§f - Position coordinates (default: player position)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §adimension <dim>§f - Dimension key (e.g., minecraft:overworld)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §ateleportTo <dim>§f - Teleport destination dimension"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);

        context.getSource().sendSuccess(() ->
            Component.literal("§eUsage Examples:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial make Earth 25 interstellar:textures/celestial/earth.png"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial make Sun 100 interstellar:textures/celestial/sun.png isSun true inOrbit false"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial make Mars 15 interstellar:textures/celestial/mars.png x 100 y 80 z 200 dimension minecraft:overworld"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial make Moon 8 interstellar:textures/celestial/moon.png inOrbit true perTextureSize 128 lightColor CCCCCC"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);

        context.getSource().sendSuccess(() ->
            Component.literal("§7Note: Parameters can be provided in any order after the required parameters"), false);
        return 1;
    }

    /**
     * Shows help for the spawn solar command.
     */
    private static int showSpawnSolarHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§6=== Spawn Solar Command Help ==="), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eSyntax:§f /celestial spawnsolar"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eDescription:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  Spawns a complete solar system at your current location"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  Creates a sun and multiple planets with moons in orbit"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  All celestial bodies are automatically registered in the current dimension"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eExample:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial spawnsolar"), false);
        return 1;
    }

    /**
     * Shows help for the list command.
     */
    private static int showListHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§6=== List Command Help ==="), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eSyntax:§f /celestial list"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eDescription:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  Lists all celestial bodies in the current dimension"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  Shows name, size, type (sun/planet), and position for each body"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eExample:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial list"), false);
        return 1;
    }

    /**
     * Shows help for the remove command.
     */
    private static int showRemoveHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§6=== Remove Command Help ==="), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eSyntax:§f /celestial remove <name>"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eDescription:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  Removes a celestial body from the current dimension"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  The name matching is case-insensitive and supports partial matches"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eParameters:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §b<name>§f - Name or partial name of the celestial body to remove"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eExamples:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial remove Earth"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial remove Sun"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial remove Mars (removes first body containing 'Mars')"), false);
        return 1;
    }

    /**
     * Shows help for the spawn test object command.
     */
    private static int showSpawnTestObjectHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
            Component.literal("§6=== Spawn Test Object Command Help ==="), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eSyntax:§f /celestial spawntestobject [size]"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eDescription:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  Spawns a test celestial body at your current location"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  Creates a planet using the OBJ model system with interstellar:planetbase.png texture"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  Useful for testing OBJ model rendering and celestial body systems"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eParameters:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §b[size]§f - Size of the celestial body (1-1000, default: 25)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eFeatures:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7• Uses OBJ model system (planet.obj)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7• interstellar:planetbase.png texture"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7• Configurable size for testing"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7• Planet type (not a sun)"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7• No orbital motion"), false);
        context.getSource().sendSuccess(() ->
            Component.literal(""), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§eExamples:"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial spawntestobject"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial spawntestobject 50"), false);
        context.getSource().sendSuccess(() ->
            Component.literal("  §7/celestial spawntestobject 100"), false);
        return 1;
    }
}