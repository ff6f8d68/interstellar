package team.nextlevelmodding.nlc.lib.rendering.objects;

import org.joml.Vector2f;

/**
 * Represents a GUI particle rendered on a screen.
 * Stores screen position, particle ID/texture, size, lifetime, and optional velocity.
 * Rendering will be handled by a GUI particle renderer later.
 */
public class guiparticleobject {

    private final String particleId;  // ID or texture reference
    private final Vector2f position;  // Screen-space position
    private Vector2f velocity;        // Movement per tick
    private float size;               // Scale of the particle
    private int lifetime;             // Total lifetime in ticks
    private int age;                  // Current age
    private float alpha;              // Transparency (0.0f - 1.0f)

    /**
     * Creates a new GUI particle at a screen position.
     *
     * @param particleId Identifier or texture name for the particle
     * @param x Screen X coordinate
     * @param y Screen Y coordinate
     */
    public guiparticleobject(String particleId, double x, double y) {
        this.particleId = particleId;
        this.position = new Vector2f((float)x, (float)y);
        this.velocity = new Vector2f(0, 0);
        this.size = 1.0f;
        this.lifetime = 40;  // default lifetime in ticks
        this.age = 0;
        this.alpha = 1.0f;   // fully opaque by default
    }

    // -------------------------
    // Getters / Setters
    // -------------------------

    public String getParticleId() {
        return particleId;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public int getAge() {
        return age;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    // -------------------------
    // Tick / update method
    // -------------------------

    /**
     * Updates the particle each tick.
     */
    public void tick() {
        position.add(velocity);
        age++;

        // Optional: fade out as particle ages
        alpha = Math.max(0.0f, 1.0f - ((float)age / lifetime));
    }

    /**
     * Checks if the particle has expired.
     *
     * @return true if age >= lifetime
     */
    public boolean isExpired() {
        return age >= lifetime;
    }
}
