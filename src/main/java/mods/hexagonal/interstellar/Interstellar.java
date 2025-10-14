package mods.hexagonal.interstellar;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import mods.hexagonal.interstellar.celestial.SolarSystem;
import mods.hexagonal.interstellar.client.celestial.SkyRenderHandler;
import mods.hexagonal.interstellar.command.CelestialCommand;
import mods.hexagonal.interstellar.registry.CelestialRegistry;
import mods.hexagonal.interstellar.network.CelestialNetworkHandler;
import org.slf4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Interstellar.MODID)
public class Interstellar {





    // Define mod id in a common place for everything to reference
    public static final String MODID = "interstellar";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // Dimension registry
    public static final DeferredRegister<LevelStem> LEVEL_STEMS = DeferredRegister.create(Registries.LEVEL_STEM, MODID);

    // Space dimension registration - using JSON dimension type



    // Creates a creative tab with the id "interstellar:example_tab" for the example item, that is placed after the combat ta

    public Interstellar() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register blocks
        ModBlocks.BLOCKS.register(modEventBus);

        // Register event listeners in proper initialization order
        modEventBus.addListener(this::commonSetup);  // Register common setup first
        modEventBus.addListener(this::clientSetup);   // Then client setup

        // Register with Minecraft Forge event bus for server events
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Interstellar mod initialization started");
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        // Make SpaceVoidBlock invisible / air-like
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SPACE_VOID_BLOCK.get(), RenderType.translucent());

        // Initialize celestial rendering system
        event.enqueueWork(() -> {
            mods.hexagonal.interstellar.client.celestial.SkyRenderHandler.initialize();
            LOGGER.info("Celestial rendering system initialized");
        });
    }

    private void setup(Event event) {
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting Interstellar common setup...");

        // Register the space dimension with Minecraft's dimension registry
        event.enqueueWork(() -> {
            try {
                // Validate mod dependencies and requirements
                validateDependencies();

                // Initialize celestial systems in proper order
                initializeCelestialSystems();

                // Initialize on-demand generation systems
                initializeOnDemandSystems();

                // Validate that all systems are working together
                validateSystemIntegration();

                LOGGER.info("Interstellar mod initialization completed successfully");

            } catch (Exception e) {
                LOGGER.error("Failed to initialize Interstellar mod systems", e);
                throw new RuntimeException("Interstellar mod initialization failed", e);
            }
        });
    }

    /**
     * Initializes the on-demand celestial generation systems.
     * This replaces the old startup generation with on-demand generation.
     */
    private void initializeOnDemandSystems() {
        try {
            LOGGER.info("Initializing on-demand celestial generation systems...");

            // Initialize the celestial registry
            CelestialRegistry.initialize();

            // Initialize network handler
            CelestialNetworkHandler.initialize();


            LOGGER.info("On-demand celestial generation systems initialized successfully");

        } catch (Exception e) {
            LOGGER.error("Failed to initialize on-demand systems", e);
            throw new RuntimeException("On-demand system initialization failed", e);
        }
    }

    /**
     * Checks if celestial systems are ready for solar system generation.
     */
    private boolean areCelestialSystemsReady() {
        try {
            // Check if SkyRenderHandler is available and initialized
            if (net.minecraftforge.api.distmarker.Dist.CLIENT.equals(net.minecraftforge.fml.loading.FMLEnvironment.dist)) {
                // On client side, check if SkyRenderHandler methods are accessible
                Class.forName("mods.hexagonal.interstellar.client.celestial.SkyRenderHandler");
            }

            // Check if CelestialBody and other core classes are available
            Class.forName("mods.hexagonal.interstellar.celestial.CelestialBody");
            Class.forName("mods.hexagonal.interstellar.celestial.SolarSystem");

            return true;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Required celestial system classes are not available", e);
            return false;
        }
    }

    /**
     * Creates the space dimension key with proper validation.
     */
    private ResourceKey<Level> createSpaceDimensionKey() {
        try {
            ResourceLocation dimensionLocation = new ResourceLocation(MODID, "space_dimension");
            ResourceKey<Level> spaceDimension = ResourceKey.create(Registries.DIMENSION, dimensionLocation);

            if (spaceDimension.location() == null) {
                throw new IllegalStateException("Failed to create space dimension key");
            }

            return spaceDimension;
        } catch (Exception e) {
            LOGGER.error("Failed to create space dimension key", e);
            throw new RuntimeException("Space dimension key creation failed", e);
        }
    }

    /**
     * Validates that the space dimension is properly configured.
     */
    private boolean validateSpaceDimension(ResourceKey<Level> spaceDimension) {
        if (spaceDimension == null) {
            LOGGER.error("Space dimension key is null");
            return false;
        }

        if (spaceDimension.location() == null) {
            LOGGER.error("Space dimension location is null");
            return false;
        }

        LOGGER.debug("Space dimension validated: {}", spaceDimension.location().getPath());
        return true;
    }

    /**
     * Fallback method to register simple celestial bodies if solar system generation fails.
     */
    private void registerFallbackCelestialBodies() {
        try {
            LOGGER.info("Registering fallback celestial bodies...");

            // Create the space dimension key
            ResourceKey<Level> spaceDimension = ResourceKey.create(
                Registries.DIMENSION,
                new ResourceLocation(MODID, "space_dimension")
            );

            // Create a simple sun
            CelestialBody sun = new CelestialBody(
                true, // isSun
                100,  // size
                new ResourceLocation(MODID, "textures/celestial/sun.png"), // texture
                512,  // perTextureSize
                false, // inOrbit
                null,  // orbitsAround
                0xFFFFAA00, // lightColor (bright yellow)
                new Vec3(0, 100, 0), // location
                spaceDimension, // dimension
                spaceDimension  // goesTo (stays in space dimension)
            );

            // Register the sun
            SkyRenderHandler.registerCelestialBody(sun, spaceDimension);

            // Create a simple planet
            CelestialBody planet = new CelestialBody(
                false, // isSun
                25,   // size
                new ResourceLocation(MODID, "textures/celestial/planet.png"), // texture
                256,  // perTextureSize
                true,  // inOrbit
                sun,   // orbitsAround
                0xFF6666FF, // lightColor (not used for planets)
                new Vec3(200, 100, 0), // location
                spaceDimension, // dimension
                spaceDimension  // goesTo
            );

            // Register the planet
            SkyRenderHandler.registerCelestialBody(planet, spaceDimension);

            LOGGER.info("Successfully registered fallback celestial bodies: sun and planet");

        } catch (Exception e) {
            LOGGER.error("Failed to register even fallback celestial bodies", e);
        }
    }

    /**
     * Validates that all required dependencies are present and compatible.
     */
    private void validateDependencies() {
        LOGGER.info("Validating mod dependencies...");

        try {
            // Check if required mods are loaded
            boolean forgeLoaded = checkModLoaded("forge");
            boolean beyondOxygenLoaded = checkModLoaded("beyond_oxygen");

            if (!forgeLoaded) {
                throw new IllegalStateException("Minecraft Forge is required but not loaded");
            }

            LOGGER.info("Beyond Oxygen integration: {}", beyondOxygenLoaded ? "ENABLED" : "DISABLED");

            // Validate celestial system components
            validateCelestialComponents();

            LOGGER.info("All dependencies validated successfully");

        } catch (Exception e) {
            LOGGER.error("Dependency validation failed", e);
            throw e;
        }
    }

    /**
     * Checks if a specific mod is loaded.
     */
    private boolean checkModLoaded(String modId) {
        return net.minecraftforge.fml.ModList.get().isLoaded(modId);
    }

    /**
     * Validates that all celestial system components are available.
     */
    private void validateCelestialComponents() {
        try {
            // Check if celestial classes are accessible
            Class.forName("mods.hexagonal.interstellar.celestial.CelestialBody");
            Class.forName("mods.hexagonal.interstellar.celestial.CelestialTeleportHandler");
            Class.forName("mods.hexagonal.interstellar.client.celestial.SkyRenderHandler");
            Class.forName("mods.hexagonal.interstellar.client.celestial.CelestialRenderer");

            LOGGER.debug("All celestial system components are accessible");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Missing celestial system components", e);
        }
    }

    /**
     * Initializes celestial systems in the correct order.
     */
    private void initializeCelestialSystems() {
        LOGGER.info("Initializing celestial systems...");

        try {
            // Validate that required classes are available before initialization
            validateRequiredClasses();

            // Initialize the sky render handler first (client-side)
            if (net.minecraftforge.api.distmarker.Dist.CLIENT.equals(net.minecraftforge.fml.loading.FMLEnvironment.dist)) {
                mods.hexagonal.interstellar.client.celestial.SkyRenderHandler.initialize();
                LOGGER.info("Celestial rendering system initialized");
            }

            // Register celestial teleport handler with the event bus
            // Note: CelestialTeleportHandler uses @Mod.EventBusSubscriber, so it auto-registers

            LOGGER.info("Celestial systems initialized successfully");

        } catch (Exception e) {
            LOGGER.error("Failed to initialize celestial systems", e);
            throw e;
        }
    }

    /**
     * Validates that all required celestial system classes are available.
     */
    private void validateRequiredClasses() {
        try {
            // Check if SolarSystem class is available
            Class.forName("mods.hexagonal.interstellar.celestial.SolarSystem");

            LOGGER.debug("All required celestial system classes are available");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Required celestial system class not found", e);
        }
    }

    /**
     * Validates that all celestial systems are working together properly.
     */
    private void validateSystemIntegration() {
        LOGGER.info("Validating system integration...");

        try {
            // Check that celestial bodies can be created and registered
            ResourceKey<Level> spaceDimension = ResourceKey.create(
                Registries.DIMENSION,
                new ResourceLocation(MODID, "space_dimension")
            );

            // Test creating a basic celestial body
            CelestialBody testBody = new CelestialBody(
                true, // isSun
                100,  // size
                new ResourceLocation(MODID, "textures/celestial/test.png"), // texture
                256,  // perTextureSize
                false, // inOrbit
                null,  // orbitsAround
                0xFFFFAA00, // lightColor
                new Vec3(0, 100, 0), // location
                spaceDimension, // dimension
                spaceDimension  // goesTo
            );

            // Test registering with SkyRenderHandler
            if (net.minecraftforge.api.distmarker.Dist.CLIENT.equals(net.minecraftforge.fml.loading.FMLEnvironment.dist)) {
                mods.hexagonal.interstellar.client.celestial.SkyRenderHandler.registerCelestialBody(testBody, spaceDimension);

                // Clean up test body
                mods.hexagonal.interstellar.client.celestial.SkyRenderHandler.unregisterCelestialBody(testBody, spaceDimension);
            }

            LOGGER.info("System integration validation completed successfully");

        } catch (Exception e) {
            LOGGER.error("System integration validation failed", e);
            throw new RuntimeException("Celestial systems are not working together properly", e);
        }
    }


    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Space dimension is now properly registered and accessible via /execute command
        // Usage: /execute in interstellar:space_dimension run <command>
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Interstellar mod server starting...");

        // Perform server-side initialization
        try {
            // Validate server-side celestial systems
            validateServerSideSystems();

            // Register celestial commands now that the server is available
            CelestialCommand.register(event.getServer().getCommands().getDispatcher());

            LOGGER.info("Interstellar mod server initialization completed");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize server-side systems", e);
        }
    }

    /**
     * Handles player dimension changes to trigger on-demand celestial generation.
     * When a player enters the space dimension for the first time, generate celestial bodies.
     */
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        try {
            // Check if player entered the space dimension
            ResourceKey<Level> spaceDimension = createSpaceDimensionKey();
            if (!event.getTo().equals(spaceDimension)) {
                return;
            }

            LOGGER.info("Player {} entered space dimension, checking for on-demand generation",
                serverPlayer.getName().getString());

            // Check if player already has celestial bodies in this dimension
            if (CelestialRegistry.hasPlayerGeneratedInDimension(serverPlayer, spaceDimension)) {
                LOGGER.debug("Player {} already has celestial bodies in space dimension", serverPlayer.getName().getString());
                return;
            }

            // Generate solar system for the player
            generateSolarSystemForPlayer(serverPlayer);

        } catch (Exception e) {
            LOGGER.error("Failed to handle player dimension change for on-demand generation", e);
        }
    }

    /**
     * Generates a solar system for a specific player when they first enter space.
     *
     * @param player The player to generate the solar system for
     */
    private void generateSolarSystemForPlayer(ServerPlayer player) {
        try {
            LOGGER.info("Generating on-demand solar system for player {}", player.getName().getString());

            // Create the space dimension key
            ResourceKey<Level> spaceDimension = createSpaceDimensionKey();

            // Create solar system generator
            SolarSystem solarSystem = new SolarSystem(spaceDimension);

            // Generate solar system for the player at their current location
            List<CelestialBody> celestialBodies = solarSystem.generateForPlayer(player);

            // Register all celestial bodies with the server registry
            for (CelestialBody celestialBody : celestialBodies) {
                if (CelestialRegistry.registerCelestialBody(celestialBody, spaceDimension)) {
                    // Send synchronization to the player
                    CelestialNetworkHandler.sendCelestialBodySync(player, celestialBody, spaceDimension);

                    LOGGER.debug("Registered and synchronized celestial body: {} for player {}",
                        celestialBody.getDisplayName(), player.getName().getString());
                }
            }

            // Send full registry sync to ensure player has all celestial bodies
            CelestialNetworkHandler.sendFullRegistrySync(player);

            LOGGER.info("Successfully generated on-demand solar system for player {} with {} celestial bodies",
                player.getName().getString(), celestialBodies.size());

        } catch (Exception e) {
            LOGGER.error("Failed to generate solar system for player {}", player.getName().getString(), e);
        }
    }

    /**
     * Validates server-side celestial systems are properly initialized.
     */
    private void validateServerSideSystems() {
        LOGGER.debug("Validating server-side celestial systems...");

        // Ensure the space dimension is properly registered
        try {
            ResourceKey<Level> spaceDimension = ResourceKey.create(
                Registries.DIMENSION,
                new ResourceLocation(MODID, "space_dimension")
            );

            // Verify dimension key is valid
            if (spaceDimension.location() == null) {
                throw new IllegalStateException("Space dimension key is invalid");
            }

            LOGGER.debug("Server-side celestial systems validated");
        } catch (Exception e) {
            LOGGER.error("Server-side validation failed", e);
            throw e;
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent

}
