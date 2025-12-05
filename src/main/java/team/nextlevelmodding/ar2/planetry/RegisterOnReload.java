package team.nextlevelmodding.ar2.planetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import team.nextlevelmodding.ar2.data.PlanetSystem;
import team.nextlevelmodding.ar2.ar2;
import shipwrights.genesis.GenesisMod;

import java.io.*;

public class RegisterOnReload {
    private static final File CONFIG_FILE = new File("config/ar2/planets.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Loads planets and moons from JSON and registers them.
     */
    public static void loadAndRegister() {
        if (!CONFIG_FILE.exists()) {
            createEmptyConfig();
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            PlanetSystem system = GSON.fromJson(reader, PlanetSystem.class);
            if (system == null) system = new PlanetSystem();

            // Register planets
            if (system.planets != null) {
                for (PlanetSystem.PlanetEntry planet : system.planets) {
                    GenesisMod.registerPlanet(
                            new ResourceLocation(planet.dimensionID),
                            planet.size,
                            planet.gravity,
                            planet.orbitRadius,
                            planet.yearLength,
                            planet.r,
                            planet.g,
                            planet.b
                    );
                }
            }

            // Register moons
            if (system.moons != null) {
                for (PlanetSystem.MoonEntry moon : system.moons) {
                    GenesisMod.registerMoon(
                            new ResourceLocation(moon.dimensionID),
                            new ResourceLocation(moon.parentDimensionID),
                            moon.size,
                            moon.gravity,
                            moon.orbitRadius,
                            moon.yearLength,
                            moon.r,
                            moon.g,
                            moon.b
                    );
                }
            }

            GenesisMod.finalizeMoons();

        } catch (IOException e) {
            ar2.LOGGER.error("Failed to load planet config from " + CONFIG_FILE.getPath(), e);
        }
    }

    /**
     * Creates a new empty planets.json file.
     */
    public static void createEmptyConfig() {
        if (CONFIG_FILE.exists()) {
            ar2.LOGGER.info("Planet config already exists at " + CONFIG_FILE.getPath());
            return; // Do nothing if file exists
        }

        PlanetSystem empty = new PlanetSystem();
        empty.planets = new java.util.ArrayList<>();
        empty.moons = new java.util.ArrayList<>();

        CONFIG_FILE.getParentFile().mkdirs(); // ensure config/ar2 exists

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(empty, writer);
            ar2.LOGGER.info("Created empty planet config at " + CONFIG_FILE.getPath());
        } catch (IOException e) {
            ar2.LOGGER.error("Failed to create empty planet config", e);
        }
    }


    /**
     * Adds a new planet to the JSON config.
     */
    public static void addPlanet(PlanetSystem.PlanetEntry planet) {
        modifyConfig(system -> {
            if (system.planets == null) system.planets = new java.util.ArrayList<>();
            system.planets.add(planet);
        });
    }

    /**
     * Adds a new moon to the JSON config.
     */
    public static void addMoon(PlanetSystem.MoonEntry moon) {
        modifyConfig(system -> {
            if (system.moons == null) system.moons = new java.util.ArrayList<>();
            system.moons.add(moon);
        });
    }

    /**
     * Helper method to safely modify the JSON config.
     */
    private static void modifyConfig(java.util.function.Consumer<PlanetSystem> modifier) {
        PlanetSystem system;
        try {
            if (!CONFIG_FILE.exists()) createEmptyConfig();

            try (Reader reader = new FileReader(CONFIG_FILE)) {
                system = GSON.fromJson(reader, PlanetSystem.class);
                if (system == null) system = new PlanetSystem();
            }

            modifier.accept(system); // apply changes

            try (Writer writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(system, writer);
            }

        } catch (IOException e) {
            ar2.LOGGER.error("Failed to modify planet config", e);
        }
    }
}
