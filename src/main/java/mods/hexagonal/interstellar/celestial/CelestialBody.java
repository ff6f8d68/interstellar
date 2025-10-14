package mods.hexagonal.interstellar.celestial;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents a celestial body in the Interstellar mod.
 * This class handles planets, moons, stars, and other celestial objects
 * with support for orbital mechanics and dimensional teleportation.
 */
public class CelestialBody {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Core properties
    private final boolean isSun;
    private final int size;
    private final ResourceLocation texture;
    private final int perTextureSize;
    private final boolean inOrbit;
    private final int lightColor;
    private final Vec3 location;
    private final ResourceKey<Level> dimension;
    private final ResourceKey<Level> goesTo;

    // Orbital mechanics
    @Nullable
    private final CelestialBody orbitsAround;
    private double orbitRadius;
    private double orbitSpeed;
    private double orbitAngle;
    private long orbitStartTime;

    /**
     * Creates a new CelestialBody with the specified parameters.
     *
     * @param isSun Whether this celestial body is a sun/star
     * @param size Size in blocks for hitbox and visual representation
     * @param texture ResourceLocation for the cubemap texture
     * @param perTextureSize Cubic texture size in pixels
     * @param inOrbit Whether this body orbits around another celestial body
     * @param orbitsAround The celestial body this orbits around (null if not orbiting)
     * @param lightColor Hex color for sun shader (used only if isSun is true)
     * @param location Initial x,y,z coordinates in the world
     * @param dimension The dimension this celestial body exists in
     * @param goesTo The dimension players teleport to when interacting with this body
     */
    public CelestialBody(
            boolean isSun,
            int size,
            ResourceLocation texture,
            int perTextureSize,
            boolean inOrbit,
            @Nullable CelestialBody orbitsAround,
            int lightColor,
            Vec3 location,
            ResourceKey<Level> dimension,
            ResourceKey<Level> goesTo
    ) {
        LOGGER.info("=== Creating CelestialBody ===");
        LOGGER.info("Parameters: isSun={}, size={}, texture={}, perTextureSize={}, inOrbit={}, lightColor={}, location={}, dimension={}, goesTo={}",
            isSun, size, texture, perTextureSize, inOrbit, lightColor, location,
            dimension != null ? dimension.location().getPath() : "null",
            goesTo != null ? goesTo.location().getPath() : "null");

        // Validation
        LOGGER.info("Validating parameters...");
        validateParameters(size, texture, perTextureSize, location, dimension, goesTo);
        LOGGER.info("Parameter validation completed");

        this.isSun = isSun;
        this.size = size;
        this.texture = texture;
        this.perTextureSize = perTextureSize;
        this.inOrbit = inOrbit;
        this.orbitsAround = orbitsAround;
        this.lightColor = lightColor;
        this.location = location;
        this.dimension = dimension;
        this.goesTo = goesTo;

        // Initialize orbital mechanics if needed
        if (inOrbit && orbitsAround != null) {
            LOGGER.info("Initializing orbit mechanics...");
            initializeOrbit();
            LOGGER.info("Orbit initialization completed");
        } else {
            this.orbitRadius = 0;
            this.orbitSpeed = 0;
            this.orbitAngle = 0;
            this.orbitStartTime = 0;
            LOGGER.info("Skipping orbit initialization (inOrbit={}, orbitsAround={})", inOrbit, orbitsAround != null);
        }

        LOGGER.info("Successfully created CelestialBody: {} (sun: {}, size: {}) in dimension {}",
            texture.getPath(), isSun, size, dimension.location().getPath());
        LOGGER.info("=== CelestialBody creation completed ===");
    }

    /**
     * Validates all required parameters for the CelestialBody.
     */
    private void validateParameters(int size, ResourceLocation texture, int perTextureSize,
                                   Vec3 location, ResourceKey<Level> dimension, ResourceKey<Level> goesTo) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive, got: " + size);
        }
        if (texture == null) {
            throw new IllegalArgumentException("Texture cannot be null");
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
        if (goesTo == null) {
            throw new IllegalArgumentException("Teleport destination dimension cannot be null");
        }
    }

    /**
     * Initializes orbital mechanics for celestial bodies that orbit around others.
     */
    private void initializeOrbit() {
        if (orbitsAround == null) {
            throw new IllegalStateException("Cannot initialize orbit without a central celestial body");
        }

        // Calculate orbit radius based on distance between bodies
        this.orbitRadius = Math.sqrt(
            Math.pow(location.x - orbitsAround.getLocation().x, 2) +
            Math.pow(location.y - orbitsAround.getLocation().y, 2) +
            Math.pow(location.z - orbitsAround.getLocation().z, 2)
        );

        // Calculate initial orbit angle
        Vec3 relativePos = location.subtract(orbitsAround.getLocation());
        this.orbitAngle = Math.atan2(relativePos.z, relativePos.x);

        // Set orbit speed based on distance (closer = faster for realistic orbits)
        this.orbitSpeed = Math.PI / (100.0 + orbitRadius * 2.0); // Adjust timing as needed

        // Record start time for consistent orbital calculations
        this.orbitStartTime = System.currentTimeMillis();

        LOGGER.debug("Initialized orbit for {} around {} with radius {} and speed {}",
            texture.getPath(), orbitsAround.getTexture().getPath(), orbitRadius, orbitSpeed);
    }

    // Getters

    public boolean isSun() {
        return isSun;
    }

    public int getSize() {
        return size;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public int getPerTextureSize() {
        return perTextureSize;
    }

    public boolean isInOrbit() {
        return inOrbit;
    }

    @Nullable
    public CelestialBody getOrbitsAround() {
        return orbitsAround;
    }

    public int getLightColor() {
        return lightColor;
    }

    public Vec3 getLocation() {
        return location;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public ResourceKey<Level> getTeleportDestination() {
        return goesTo;
    }

    // Orbital mechanics methods

    /**
     * Updates the position of this celestial body if it's in orbit.
     * Should be called regularly (e.g., each tick) to animate orbital motion.
     */
    public void updateOrbit() {
        if (!inOrbit || orbitsAround == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - orbitStartTime) / 1000.0; // Convert to seconds

        // Update orbit angle based on speed and time
        orbitAngle += orbitSpeed * deltaTime;

        // Calculate new position relative to the central body
        double x = orbitsAround.getLocation().x + orbitRadius * Math.cos(orbitAngle);
        double y = orbitsAround.getLocation().y; // Keep Y coordinate same for simplicity
        double z = orbitsAround.getLocation().z + orbitRadius * Math.sin(orbitAngle);

        // Update start time for next calculation
        orbitStartTime = currentTime;
    }

    /**
     * Gets the current orbital position, accounting for orbital motion.
     * @return Current position including orbital offset
     */
    public Vec3 getCurrentPosition() {
        if (!inOrbit || orbitsAround == null) {
            return location;
        }

        // Calculate current orbital position
        double x = orbitsAround.getLocation().x + orbitRadius * Math.cos(orbitAngle);
        double y = orbitsAround.getLocation().y;
        double z = orbitsAround.getLocation().z + orbitRadius * Math.sin(orbitAngle);

        return new Vec3(x, y, z);
    }

    /**
     * Gets the orbit radius for this celestial body.
     * @return Orbit radius, or 0 if not in orbit
     */
    public double getOrbitRadius() {
        return orbitRadius;
    }

    /**
     * Gets the current orbit angle in radians.
     * @return Current orbit angle, or 0 if not in orbit
     */
    public double getOrbitAngle() {
        return orbitAngle;
    }

    // Utility methods

    /**
     * Checks if this celestial body can teleport players to its destination dimension.
     * @return true if teleportation is possible
     */
    public boolean canTeleport() {
        return goesTo != null && !goesTo.equals(dimension);
    }

    /**
     * Gets a human-readable name for this celestial body based on its texture path.
     * @return Display name for the celestial body
     */
    public String getDisplayName() {
        String path = texture.getPath();
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.replace('_', ' ').toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CelestialBody that = (CelestialBody) obj;
        return Objects.equals(texture, that.texture) &&
               Objects.equals(dimension, that.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, dimension);
    }

    @Override
    public String toString() {
        return "CelestialBody{" +
                "isSun=" + isSun +
                ", size=" + size +
                ", texture=" + texture +
                ", dimension=" + dimension.location() +
                ", inOrbit=" + inOrbit +
                '}';
    }
}