package mods.hexagonal.interstellar.celestial;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates and manages a complete solar system with a central sun,
 * multiple planets, and optional moons. Provides realistic orbital
 * mechanics and teleportation destinations for each celestial body.
 */
public class SolarSystem {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Solar system configuration
    private static final int MIN_PLANETS = 4;
    private static final int MAX_PLANETS = 6;
    private static final float MIN_PLANET_DISTANCE = 200.0f;
    private static final float MAX_PLANET_DISTANCE = 800.0f;
    private static final float MIN_PLANET_SIZE = 10.0f;
    private static final float MAX_PLANET_SIZE = 30.0f;
    private static final float MOON_PROBABILITY = 0.6f; // 60% chance for a planet to have a moon

    // Sun configuration
    private static final int SUN_SIZE_MIN = 50;
    private static final int SUN_SIZE_MAX = 100;
    private static final int SUN_LIGHT_COLOR = 0xFFFFFFAA; // Bright yellow

    // Orbital mechanics
    private static final double BASE_ORBIT_SPEED = Math.PI / 200.0; // Base orbital speed
    private static final double ORBIT_SPEED_VARIATION = 0.3; // Variation in orbital speeds

    private final ResourceKey<Level> spaceDimension;
    private final List<CelestialBody> celestialBodies;
    private final Random random;

    // Solar system properties
    private CelestialBody sun;
    private boolean generated;

    /**
     * Creates a new solar system generator for the specified dimension.
     *
     * @param spaceDimension The dimension where the solar system will be generated
     * @throws IllegalArgumentException if spaceDimension is null
     */
    public SolarSystem(ResourceKey<Level> spaceDimension) {
        if (spaceDimension == null) {
            throw new IllegalArgumentException("Space dimension cannot be null");
        }

        this.spaceDimension = spaceDimension;
        this.celestialBodies = new ArrayList<>();
        this.random = new Random();
        this.generated = false;

        LOGGER.info("Created SolarSystem generator for dimension: {}",
            spaceDimension.location().getPath());
    }

    /**
     * Generates the complete solar system with sun, planets, and moons.
     * This method can only be called once per SolarSystem instance.
     *
     * @return List of all generated celestial bodies
     * @throws IllegalStateException if the solar system has already been generated
     */
    public List<CelestialBody> generateSolarSystem() {
        if (generated) {
            throw new IllegalStateException("Solar system has already been generated");
        }

        try {
            LOGGER.info("Generating solar system in dimension: {}",
                spaceDimension.location().getPath());

            // Validate that the space dimension is available
            validateDimension();

            // Generate central sun
            generateSun();

            // Generate planets around the sun
            generatePlanets();

            // Generate moons for some planets
            generateMoons();

            // Validate the generated solar system
            validateSolarSystem();

            generated = true;

            LOGGER.info("Successfully generated solar system with {} celestial bodies: 1 sun, {} planets, {} moons",
                celestialBodies.size(),
                countPlanets(),
                countMoons());

            return new ArrayList<>(celestialBodies);

        } catch (Exception e) {
            LOGGER.error("Failed to generate solar system", e);
            throw new RuntimeException("Solar system generation failed", e);
        }
    }

    /**
     * Validates that the space dimension is properly configured.
     */
    private void validateDimension() {
        if (spaceDimension == null) {
            throw new IllegalStateException("Space dimension is null");
        }

        if (spaceDimension.location() == null) {
            throw new IllegalStateException("Space dimension location is null");
        }

        LOGGER.debug("Validated space dimension: {}", spaceDimension.location().getPath());
    }

    /**
     * Validates the generated solar system for consistency.
     */
    private void validateSolarSystem() {
        if (sun == null) {
            throw new IllegalStateException("Solar system must have a sun");
        }

        if (celestialBodies.isEmpty()) {
            throw new IllegalStateException("Solar system has no celestial bodies");
        }

        int planetCount = countPlanets();
        if (planetCount < MIN_PLANETS) {
            LOGGER.warn("Solar system has fewer planets than minimum: {} < {}",
                planetCount, MIN_PLANETS);
        }

        // Validate that all planets orbit the sun
        for (CelestialBody planet : getPlanets()) {
            if (planet.getOrbitsAround() != sun) {
                LOGGER.warn("Planet {} does not orbit the sun", planet.getDisplayName());
            }
        }

        LOGGER.debug("Solar system validation completed");
    }

    /**
     * Generates the central sun for the solar system.
     */
    private void generateSun() {
        // Create sun with random size within bounds
        int sunSize = SUN_SIZE_MIN + random.nextInt(SUN_SIZE_MAX - SUN_SIZE_MIN + 1);

        // Position sun at center of solar system
        Vec3 sunPosition = new Vec3(0, 100, 0);

        // Create sun celestial body
        sun = new CelestialBody(
            true, // isSun
            sunSize, // size
            new ResourceLocation("interstellar", "textures/celestial/sun.png"), // texture
            512, // perTextureSize
            false, // inOrbit (sun doesn't orbit)
            null, // orbitsAround (sun is central)
            SUN_LIGHT_COLOR, // lightColor
            sunPosition, // location
            spaceDimension, // dimension
            spaceDimension // goesTo (stays in space dimension for now)
        );

        celestialBodies.add(sun);
        LOGGER.debug("Generated sun with size {} at position {}", sunSize, sunPosition);
    }

    /**
     * Generates planets orbiting around the sun with relative positioning.
     */
    private void generatePlanets() {
        int planetCount = MIN_PLANETS + random.nextInt(MAX_PLANETS - MIN_PLANETS + 1);

        for (int i = 0; i < planetCount; i++) {
            CelestialBody planet = generatePlanet(i);
            celestialBodies.add(planet);
        }
    }

    /**
     * Generates planets with relative positioning around a center point.
     * This method creates a more natural distribution of planets in the solar system.
     *
     * @param center The center point to generate planets around
     * @param count The number of planets to generate
     */
    public void generatePlanetsAroundLocation(Vec3 center, int count) {
        if (center == null) {
            throw new IllegalArgumentException("Center location cannot be null");
        }

        if (count <= 0) {
            throw new IllegalArgumentException("Planet count must be positive");
        }

        LOGGER.debug("Generating {} planets around location: {}", count, center);

        for (int i = 0; i < count; i++) {
            CelestialBody planet = generatePlanetAroundLocation(center, i);
            celestialBodies.add(planet);
        }
    }

    /**
     * Generates a single planet at a relative position around a center point.
     *
     * @param center The center point to orbit around
     * @param index The index of the planet (for unique positioning)
     * @return The generated planet
     */
    private CelestialBody generatePlanetAroundLocation(Vec3 center, int index) {
        // Calculate orbital distance with some variation
        float baseDistance = MIN_PLANET_DISTANCE + (index * 120);
        float distanceVariation = random.nextFloat() * 80 - 40; // ±40 variation
        float orbitRadius = Math.max(MIN_PLANET_DISTANCE, baseDistance + distanceVariation);

        // Calculate planet size with variation
        float planetSize = MIN_PLANET_SIZE + random.nextFloat() * (MAX_PLANET_SIZE - MIN_PLANET_SIZE);
        planetSize *= (0.8f + random.nextFloat() * 0.4f); // Additional size variation

        // Calculate evenly distributed angle with some randomization
        double baseAngle = (index * 2 * Math.PI) / (MIN_PLANETS + MAX_PLANETS) / 2;
        double angleVariation = (random.nextDouble() - 0.5) * Math.PI / 6; // ±15 degrees
        double angle = baseAngle + angleVariation;

        // Calculate position relative to center
        double x = center.x + Math.cos(angle) * orbitRadius;
        double z = center.z + Math.sin(angle) * orbitRadius;
        Vec3 planetPosition = new Vec3(x, center.y, z);

        // Calculate orbital speed (closer planets orbit faster, with variation)
        double orbitSpeed = BASE_ORBIT_SPEED / (1.0 + (orbitRadius / 500.0));
        orbitSpeed += (random.nextDouble() - 0.5) * ORBIT_SPEED_VARIATION;

        // Create unique dimension for this planet
        String planetName = getPlanetName(index);
        ResourceKey<Level> planetDimension = createPlanetDimension(planetName);

        // Create planet celestial body
        CelestialBody planet = new CelestialBody(
            false, // isSun
            (int) Math.ceil(planetSize), // size
            new ResourceLocation("interstellar", "textures/celestial/planet_" + planetName + ".png"), // texture
            256, // perTextureSize
            true, // inOrbit
            sun, // orbitsAround
            0xFF6666FF, // lightColor (blue-ish for planets)
            planetPosition, // location
            spaceDimension, // dimension (appears in space)
            planetDimension // goesTo (teleports to planet dimension)
        );

        LOGGER.debug("Generated planet {} around location {}: size={}, orbitRadius={}, position={}",
            planetName, center, planetSize, orbitRadius, planetPosition);

        return planet;
    }

    /**
     * Generates a single planet with random properties.
     *
     * @param index The index of the planet (for unique naming and positioning)
     * @return The generated planet
     */
    private CelestialBody generatePlanet(int index) {
        // Calculate orbital distance (increases with each planet)
        float baseDistance = MIN_PLANET_DISTANCE + (index * 100);
        float distanceVariation = random.nextFloat() * 50;
        float orbitRadius = baseDistance + distanceVariation;

        // Calculate planet size
        float planetSize = MIN_PLANET_SIZE + random.nextFloat() * (MAX_PLANET_SIZE - MIN_PLANET_SIZE);

        // Calculate initial position on orbit
        double angle = (index * 2 * Math.PI) / (MIN_PLANETS + MAX_PLANETS) / 2; // Spread planets evenly
        double x = Math.cos(angle) * orbitRadius;
        double z = Math.sin(angle) * orbitRadius;
        Vec3 planetPosition = new Vec3(x, 100, z);

        // Calculate orbital speed (closer planets orbit faster)
        double orbitSpeed = BASE_ORBIT_SPEED / (1.0 + (orbitRadius / 500.0));
        orbitSpeed += (random.nextDouble() - 0.5) * ORBIT_SPEED_VARIATION;

        // Create unique dimension for this planet
        String planetName = getPlanetName(index);
        ResourceKey<Level> planetDimension = createPlanetDimension(planetName);

        // Create planet celestial body
        CelestialBody planet = new CelestialBody(
            false, // isSun
            (int) Math.ceil(planetSize), // size
            new ResourceLocation("interstellar", "textures/celestial/planet_" + planetName + ".png"), // texture
            256, // perTextureSize
            true, // inOrbit
            sun, // orbitsAround
            0xFF6666FF, // lightColor (blue-ish for planets)
            planetPosition, // location
            spaceDimension, // dimension (appears in space)
            planetDimension // goesTo (teleports to planet dimension)
        );

        LOGGER.debug("Generated planet {}: size={}, orbitRadius={}, position={}",
            planetName, planetSize, orbitRadius, planetPosition);

        return planet;
    }

    /**
     * Generates moons for some planets.
     */
    private void generateMoons() {
        List<CelestialBody> planets = getPlanets();

        for (CelestialBody planet : planets) {
            if (random.nextFloat() < MOON_PROBABILITY) {
                CelestialBody moon = generateMoon(planet);
                if (moon != null) {
                    celestialBodies.add(moon);
                }
            }
        }
    }

    /**
     * Generates a moon orbiting around a planet.
     *
     * @param planet The planet to orbit around
     * @return The generated moon, or null if generation failed
     */
    private CelestialBody generateMoon(CelestialBody planet) {
        try {
            // Calculate moon orbit radius (smaller than planet orbit)
            double planetOrbitRadius = planet.getOrbitRadius();
            double moonOrbitRadius = 50 + random.nextDouble() * 100; // 50-150 blocks from planet

            // Calculate moon size (smaller than planet)
            int planetSize = planet.getSize();
            int moonSize = Math.max(5, random.nextInt(planetSize / 2));

            // Calculate initial position relative to planet
            double angle = random.nextDouble() * 2 * Math.PI;
            Vec3 planetPos = planet.getCurrentPosition();
            double x = planetPos.x + Math.cos(angle) * moonOrbitRadius;
            double z = planetPos.z + Math.sin(angle) * moonOrbitRadius;
            Vec3 moonPosition = new Vec3(x, planetPos.y, z);

            // Calculate orbital speed (faster than planet)
            double moonOrbitSpeed = BASE_ORBIT_SPEED * 3 + random.nextDouble() * BASE_ORBIT_SPEED;

            // Create unique dimension for this moon
            String moonName = "moon_" + getPlanetNameFromBody(planet);
            ResourceKey<Level> moonDimension = createPlanetDimension(moonName);

            // Create moon celestial body
            CelestialBody moon = new CelestialBody(
                false, // isSun
                moonSize, // size
                new ResourceLocation("interstellar", "textures/celestial/" + moonName + ".png"), // texture
                128, // perTextureSize
                true, // inOrbit
                planet, // orbitsAround
                0xFF888888, // lightColor (gray for moons)
                moonPosition, // location
                spaceDimension, // dimension (appears in space)
                moonDimension // goesTo (teleports to moon dimension)
            );

            LOGGER.debug("Generated moon for planet: size={}, orbitRadius={}, position={}",
                moonSize, moonOrbitRadius, moonPosition);

            return moon;

        } catch (Exception e) {
            LOGGER.warn("Failed to generate moon for planet", e);
            return null;
        }
    }

    /**
     * Gets a unique name for a planet based on its index.
     */
    private String getPlanetName(int index) {
        String[] planetNames = {"mercury", "venus", "earth", "mars", "jupiter", "saturn"};
        return planetNames[Math.min(index, planetNames.length - 1)];
    }

    /**
     * Extracts planet name from a celestial body (for moon naming).
     */
    private String getPlanetNameFromBody(CelestialBody planet) {
        String texturePath = planet.getTexture().getPath();
        int lastSlash = texturePath.lastIndexOf('/');
        String filename = texturePath.substring(lastSlash + 1);
        return filename.replace(".png", "").replace("planet_", "");
    }

    /**
     * Creates a unique dimension key for a planet or moon.
     */
    private ResourceKey<Level> createPlanetDimension(String name) {
        return ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation("interstellar", name + "_dimension")
        );
    }

    /**
     * Gets all planets in the solar system (excludes sun and moons).
     */
    public List<CelestialBody> getPlanets() {
        return celestialBodies.stream()
            .filter(body -> !body.isSun() && body.getOrbitsAround() == sun)
            .collect(ArrayList::new, (list, body) -> list.add(body), ArrayList::addAll);
    }

    /**
     * Gets all moons in the solar system.
     */
    public List<CelestialBody> getMoons() {
        return celestialBodies.stream()
            .filter(body -> !body.isSun() && body.getOrbitsAround() != sun && body.getOrbitsAround() != null)
            .collect(ArrayList::new, (list, body) -> list.add(body), ArrayList::addAll);
    }

    /**
     * Gets the central sun of the solar system.
     */
    public CelestialBody getSun() {
        return sun;
    }

    /**
     * Gets all celestial bodies in the solar system.
     */
    public List<CelestialBody> getAllCelestialBodies() {
        return new ArrayList<>(celestialBodies);
    }

    /**
     * Checks if the solar system has been generated.
     */
    public boolean isGenerated() {
        return generated;
    }

    /**
     * Gets the number of planets in the solar system.
     */
    private int countPlanets() {
        return (int) celestialBodies.stream()
            .filter(body -> !body.isSun() && body.getOrbitsAround() == sun)
            .count();
    }

    /**
     * Gets the number of moons in the solar system.
     */
    private int countMoons() {
        return (int) celestialBodies.stream()
            .filter(body -> !body.isSun() && body.getOrbitsAround() != sun && body.getOrbitsAround() != null)
            .count();
    }

    /**
     * Validates that all celestial bodies have valid teleportation destinations.
     *
     * @return true if all destinations are valid
     */
    public boolean validateTeleportationDestinations() {
        for (CelestialBody body : celestialBodies) {
            if (!body.canTeleport()) {
                LOGGER.warn("Celestial body {} has invalid teleportation destination", body.getDisplayName());
                return false;
            }
        }
        return true;
    }

    /**
     * Generates a complete solar system at a specific location.
     * This method can only be called once per SolarSystem instance.
     *
     * @param center The center location for the solar system
     * @return List of all generated celestial bodies
     * @throws IllegalStateException if the solar system has already been generated
     */
    public List<CelestialBody> generateSolarSystemAtLocation(Vec3 center) {
        if (generated) {
            throw new IllegalStateException("Solar system has already been generated");
        }

        if (center == null) {
            throw new IllegalArgumentException("Center location cannot be null");
        }

        try {
            LOGGER.info("=== Starting generateSolarSystemAtLocation ===");
            LOGGER.info("Center location: {}, Dimension: {}", center, spaceDimension.location().getPath());

            // Validate that the space dimension is available
            LOGGER.info("Validating dimension...");
            validateDimension();
            LOGGER.info("Dimension validation completed");

            // Generate central sun at specified location
            LOGGER.info("Generating sun at location...");
            generateSunAtLocation(center);
            LOGGER.info("Sun generation completed. Sun: {}", sun != null ? sun.getDisplayName() : "null");

            // Generate planets around the center location with relative positioning
            int planetCount = MIN_PLANETS + random.nextInt(MAX_PLANETS - MIN_PLANETS + 1);
            LOGGER.info("Generating {} planets...", planetCount);
            generatePlanetsAroundLocation(center, planetCount);
            LOGGER.info("Planet generation completed. Total celestial bodies: {}", celestialBodies.size());

            // Generate moons for some planets
            LOGGER.info("Generating moons...");
            generateMoons();
            LOGGER.info("Moon generation completed. Total celestial bodies: {}", celestialBodies.size());

            // Validate the generated solar system
            LOGGER.info("Validating solar system...");
            validateSolarSystem();
            LOGGER.info("Solar system validation completed");

            generated = true;

            LOGGER.info("Successfully generated solar system at {} with {} celestial bodies: 1 sun, {} planets, {} moons",
                center, celestialBodies.size(), countPlanets(), countMoons());

            for (int i = 0; i < celestialBodies.size(); i++) {
                CelestialBody body = celestialBodies.get(i);
                LOGGER.info("Body {}: {} (sun: {}, size: {}, texture: {}, dimension: {})",
                    i + 1, body.getDisplayName(), body.isSun(), body.getSize(),
                    body.getTexture(), body.getDimension().location().getPath());
            }

            LOGGER.info("=== generateSolarSystemAtLocation completed successfully ===");
            return new ArrayList<>(celestialBodies);

        } catch (Exception e) {
            LOGGER.error("=== generateSolarSystemAtLocation failed ===", e);
            throw new RuntimeException("Solar system generation failed", e);
        }
    }

    /**
     * Generates a solar system specifically for a player.
     * This ensures player-specific generation tracking and relative positioning.
     *
     * @param player The player to generate the solar system for
     * @return List of all generated celestial bodies
     */
    public List<CelestialBody> generateForPlayer(ServerPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        Vec3 playerPos = player.position();

        LOGGER.info("Generating player-specific solar system for {} at location: {}",
            player.getName().getString(), playerPos);

        // Generate solar system at player's location
        List<CelestialBody> celestialBodies = generateSolarSystemAtLocation(playerPos);

        // Record player-specific generation for tracking
        mods.hexagonal.interstellar.registry.CelestialRegistry.recordPlayerGeneration(player, spaceDimension);

        LOGGER.info("Generated player-specific solar system for {} with {} celestial bodies",
            player.getName().getString(), celestialBodies.size());

        return celestialBodies;
    }

    /**
     * Generates the central sun at a specific location.
     *
     * @param center The center location for the sun
     */
    private void generateSunAtLocation(Vec3 center) {
        // Create sun with random size within bounds
        int sunSize = SUN_SIZE_MIN + random.nextInt(SUN_SIZE_MAX - SUN_SIZE_MIN + 1);

        // Position sun at specified center location
        Vec3 sunPosition = new Vec3(center.x, center.y, center.z);

        // Create sun celestial body
        sun = new CelestialBody(
            true, // isSun
            sunSize, // size
            new ResourceLocation("interstellar", "textures/celestial/sun.png"), // texture
            512, // perTextureSize
            false, // inOrbit (sun doesn't orbit)
            null, // orbitsAround (sun is central)
            SUN_LIGHT_COLOR, // lightColor
            sunPosition, // location
            spaceDimension, // dimension
            spaceDimension // goesTo (stays in space dimension for now)
        );

        celestialBodies.add(sun);
        LOGGER.debug("Generated sun with size {} at position {}", sunSize, sunPosition);
    }

    /**
     * Updates orbital positions for all celestial bodies.
     * Should be called regularly to animate the solar system.
     */
    public void updateOrbits() {
        if (!generated) {
            return;
        }

        for (CelestialBody body : celestialBodies) {
            if (body.isInOrbit()) {
                body.updateOrbit();
            }
        }
    }

    /**
     * Gets the center location of this solar system.
     *
     * @return The center location, or null if not generated
     */
    public Vec3 getCenterLocation() {
        return sun != null ? sun.getLocation() : null;
    }

    /**
     * Checks if this solar system was generated for a specific player.
     *
     * @param player The player to check
     * @return true if the solar system was generated for the player
     */
    public boolean isGeneratedForPlayer(ServerPlayer player) {
        if (player == null || !generated) {
            return false;
        }

        return mods.hexagonal.interstellar.registry.CelestialRegistry.hasPlayerGeneratedInDimension(player, spaceDimension);
    }

    /**
     * Gets all celestial bodies within a certain radius of a location.
     *
     * @param location The location to check from
     * @param radius The radius to check within
     * @return List of celestial bodies within the radius
     */
    public List<CelestialBody> getCelestialBodiesWithinRadius(Vec3 location, double radius) {
        if (location == null || !generated) {
            return new ArrayList<>();
        }

        return celestialBodies.stream()
            .filter(body -> body.getCurrentPosition().distanceTo(location) <= radius)
            .collect(ArrayList::new, (list, body) -> list.add(body), ArrayList::addAll);
    }

    /**
     * Generates a solar system with a custom relative positioning pattern.
     * This allows for different distribution patterns beyond the default orbital layout.
     *
     * @param center The center location for the solar system
     * @param pattern The positioning pattern to use ("orbital", "spiral", "cluster", "random")
     * @param planetCount The number of planets to generate
     * @return List of generated celestial bodies
     */
    public List<CelestialBody> generateSolarSystemWithPattern(Vec3 center, String pattern, int planetCount) {
        if (center == null) {
            throw new IllegalArgumentException("Center location cannot be null");
        }

        if (planetCount <= 0 || planetCount > 20) {
            throw new IllegalArgumentException("Planet count must be between 1 and 20");
        }

        if (pattern == null) {
            pattern = "orbital";
        }

        try {
            LOGGER.info("Generating solar system with '{}' pattern at location: {} with {} planets",
                pattern, center, planetCount);

            // Generate central sun at specified location
            generateSunAtLocation(center);

            // Generate planets using the specified pattern
            switch (pattern.toLowerCase()) {
                case "spiral":
                    generateSpiralPattern(center, planetCount);
                    break;
                case "cluster":
                    generateClusterPattern(center, planetCount);
                    break;
                case "random":
                    generateRandomPattern(center, planetCount);
                    break;
                case "orbital":
                default:
                    generatePlanetsAroundLocation(center, planetCount);
                    break;
            }

            // Generate moons for some planets
            generateMoons();

            // Validate the generated solar system
            validateSolarSystem();

            generated = true;

            LOGGER.info("Successfully generated solar system with '{}' pattern: 1 sun, {} planets, {} moons",
                pattern, countPlanets(), countMoons());

            return new ArrayList<>(celestialBodies);

        } catch (Exception e) {
            LOGGER.error("Failed to generate solar system with pattern", e);
            throw new RuntimeException("Solar system generation with pattern failed", e);
        }
    }

    /**
     * Generates planets in a spiral pattern around the center.
     */
    private void generateSpiralPattern(Vec3 center, int planetCount) {
        double goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0)); // Golden angle in radians
        double radiusIncrement = 150.0;

        for (int i = 0; i < planetCount; i++) {
            double radius = radiusIncrement * Math.sqrt(i + 1);
            double angle = i * goldenAngle;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            Vec3 position = new Vec3(x, center.y, z);
            CelestialBody planet = createPlanetAtPosition(position, i);
            celestialBodies.add(planet);
        }
    }

    /**
     * Generates planets in a clustered pattern around the center.
     */
    private void generateClusterPattern(Vec3 center, int planetCount) {
        double clusterRadius = 300.0;
        int clusters = Math.max(1, planetCount / 3);

        for (int i = 0; i < planetCount; i++) {
            int clusterIndex = i % clusters;
            double clusterAngle = (clusterIndex * 2 * Math.PI) / clusters;
            double clusterDistance = clusterRadius * (0.5 + 0.5 * random.nextDouble());

            double offsetAngle = (random.nextDouble() - 0.5) * Math.PI / 4; // ±22.5 degrees
            double offsetDistance = random.nextDouble() * 100; // 0-100 blocks

            double angle = clusterAngle + offsetAngle;
            double distance = clusterDistance + offsetDistance;

            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;

            Vec3 position = new Vec3(x, center.y, z);
            CelestialBody planet = createPlanetAtPosition(position, i);
            celestialBodies.add(planet);
        }
    }

    /**
     * Generates planets in a random pattern around the center.
     */
    private void generateRandomPattern(Vec3 center, int planetCount) {
        double maxRadius = 800.0;

        for (int i = 0; i < planetCount; i++) {
            double distance = MIN_PLANET_DISTANCE + random.nextDouble() * (maxRadius - MIN_PLANET_DISTANCE);
            double angle = random.nextDouble() * 2 * Math.PI;

            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;

            Vec3 position = new Vec3(x, center.y, z);
            CelestialBody planet = createPlanetAtPosition(position, i);
            celestialBodies.add(planet);
        }
    }

    /**
     * Creates a planet at a specific position with the given index.
     */
    private CelestialBody createPlanetAtPosition(Vec3 position, int index) {
        // Calculate planet size
        float planetSize = MIN_PLANET_SIZE + random.nextFloat() * (MAX_PLANET_SIZE - MIN_PLANET_SIZE);

        // Calculate orbital mechanics (even for non-orbital planets)
        double orbitRadius = Math.sqrt(
            Math.pow(position.x - sun.getLocation().x, 2) +
            Math.pow(position.z - sun.getLocation().z, 2)
        );

        // Create unique dimension for this planet
        String planetName = getPlanetName(index);
        ResourceKey<Level> planetDimension = createPlanetDimension(planetName);

        // Create planet celestial body
        return new CelestialBody(
            false, // isSun
            (int) Math.ceil(planetSize), // size
            new ResourceLocation("interstellar", "textures/celestial/planet_" + planetName + ".png"), // texture
            256, // perTextureSize
            true, // inOrbit
            sun, // orbitsAround
            0xFF6666FF, // lightColor (blue-ish for planets)
            position, // location
            spaceDimension, // dimension (appears in space)
            planetDimension // goesTo (teleports to planet dimension)
        );
    }
}