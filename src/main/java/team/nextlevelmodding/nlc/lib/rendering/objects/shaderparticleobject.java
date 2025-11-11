package team.nextlevelmodding.nlc.lib.rendering.objects;

import org.joml.Vector3f;

/**
 * Represents a shader-based particle in the world.
 * Stores position, shader ID, size, lifetime, and optional velocity.
 * Rendering will be handled by a future shader particle system.
 */
public class shaderparticleobject {

    private final String shaderid;
    private final Vector3f position;
    private Vector3f velocity;
    private float size;
    private int lifetime;
    private int age;

    /**
     * Creates a new shader particle object at a given position.
     *
     * @param shaderId Identifier of the shader effect
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    public shaderparticleobject(String shaderId, double x, double y, double z) {
        this.shaderid = shaderId;
        this.position = new Vector3f((float) x, (float) y, (float) z);
        this.velocity = new Vector3f(0, 0, 0);
        this.size = 1.0f;
        this.lifetime = 20; // default lifetime in ticks
        this.age = 0;
    }

    // -------------------------
    // Getters / Setters
    // -------------------------

    public String getShaderId() {
        return shaderid;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
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

    // -------------------------
    // Tick / update method
    // -------------------------

    /**
     * Updates the particle each tick.
     */
    public void tick() {
        // Move particle by velocity
        position.add(velocity);

        // Increment age
        age++;
    }

    /**
     * Check if the particle has expired.
     *
     * @return true if age >= lifetime
     */
    public boolean isExpired() {
        return age >= lifetime;
    }
}
