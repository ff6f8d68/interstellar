package team.nextlevelmodding.ar2.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlanetSystem {
    public List<PlanetEntry> planets;
    public List<MoonEntry> moons;

    public static class PlanetEntry {
        public String dimensionID;
        public double size;
        public double gravity;
        public double orbitRadius; // Sun distance
        public double yearLength;
        public float r, g, b;
    }

    public static class MoonEntry {
        public String dimensionID;
        public String parentDimensionID;
        public double size;
        public double gravity;
        public double orbitRadius;
        public double yearLength;
        public float r, g, b;
    }
}
