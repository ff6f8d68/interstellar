package team.nextlevelmodding.ar2.data;

public class ThrustData {
    private final double thrustLevel;
    private final double maxThrust;

    public ThrustData(double thrustLevel, double maxThrust) {
        this.thrustLevel = thrustLevel;
        this.maxThrust = maxThrust;
    }

    public double getThrustLevel() {
        return thrustLevel;
    }

    public double getMaxThrust() {
        return maxThrust;
    }
}