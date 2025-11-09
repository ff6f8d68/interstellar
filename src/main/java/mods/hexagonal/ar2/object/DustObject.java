package mods.hexagonal.ar2.object;

import org.joml.Vector3d;

public class DustObject {
    Vector3d position;
    Vector3d velocity;
    Vector3d acceleration; // e.g., gravity, thrust
    float lifetime; // ticks left
}
