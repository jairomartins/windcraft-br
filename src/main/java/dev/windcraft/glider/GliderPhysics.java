package dev.windcraft.glider;

import dev.windcraft.simulation.WindSample;

/** Pure flight model, in blocks/tick. No world reads or Minecraft classes. */
public final class GliderPhysics {
    public static final double MAX_HORIZONTAL_SPEED = 1.1;
    public static final double SAFE_LANDING_SPEED = .35;
    public record Velocity(double x, double y, double z) {}
    public record Conditions(WindSample wind, double updraft, long tick, long seed) {
        public Conditions {
            if(wind==null || !Double.isFinite(updraft)) throw new IllegalArgumentException("Invalid wind field");
            updraft=Math.clamp(updraft,-16,16);
        }
    }
    public record Controls(float forward, float turn, boolean flare) {
        public static final Controls IDLE = new Controls(0, 0, false);
        public Controls {
            if(!Float.isFinite(forward) || !Float.isFinite(turn)) throw new IllegalArgumentException("Invalid flight controls");
            forward = Math.clamp(forward, -1, 1); turn = Math.clamp(turn, -1, 1);
        }
    }
    public record Settings(double density, double area, double lift, double drag, double wind,
                           double maxSpeed, double sensitivity, double turbulence,
                           double maxBank, double bankSeconds, double levelSeconds) {
        public Settings {
            for(double value:new double[]{density,area,lift,drag,wind,maxSpeed,sensitivity,turbulence,maxBank,bankSeconds,levelSeconds})
                if(!Double.isFinite(value) || value<0) throw new IllegalArgumentException("Invalid flight settings");
            if(maxSpeed==0 || bankSeconds==0 || levelSeconds==0 || maxBank>40)
                throw new IllegalArgumentException("Invalid flight limits");
        }
        public Settings(double density,double area,double lift,double drag,double wind,double maxSpeed,double sensitivity,double turbulence) {
            this(density,area,lift,drag,wind,maxSpeed,sensitivity,turbulence,25,.4,.55);
        }
    }
    public static final Settings DEFAULT = new Settings(1.225, 12, 1, .08, .35, 1.1, 1, 1);
    private static final double MASS = 90;

    private GliderPhysics() {}

    /** Constant bounded angular rate; 20 Hz, with slower return to level. Degrees. */
    public static float updateBank(float current,float turn,Settings settings) {
        if(!Float.isFinite(current) || !Float.isFinite(turn)) return 0;
        double target=Math.clamp(turn*settings.sensitivity,-1,1)*settings.maxBank;
        double seconds=Math.abs(turn)<.001 ? settings.levelSeconds : settings.bankSeconds;
        double step=settings.maxBank/(20*Math.max(.1,seconds));
        return (float)Math.clamp(current+Math.clamp(target-current,-step,step),-settings.maxBank,settings.maxBank);
    }

    public static Velocity step(Velocity velocity, float yaw, float pitch, Controls controls, Conditions conditions, Settings settings) {
        // Convenience for standalone callers; live flight supplies its persistent bank.
        double heading=Math.atan2(-velocity.x,velocity.z);
        double error=Math.atan2(Math.sin(Math.toRadians(yaw)-heading),Math.cos(Math.toRadians(yaw)-heading));
        float bank=(float)Math.toDegrees(Math.clamp(error*1.3+controls.turn*.55,-Math.toRadians(settings.maxBank),Math.toRadians(settings.maxBank)));
        return step(velocity,yaw,pitch,controls,conditions,settings,bank);
    }

    public static Velocity step(Velocity velocity, float yaw, float pitch, Controls controls, Conditions conditions, Settings settings,float bankDegrees) {
        if (!Double.isFinite(velocity.x) || !Double.isFinite(velocity.y) || !Double.isFinite(velocity.z)
            || !Float.isFinite(yaw) || !Float.isFinite(pitch)) throw new IllegalArgumentException("Non-finite flight input");
        double angle = Math.toRadians(yaw);
        double ax = velocity.x * 20 - conditions.wind.xPerTick() * 20 * settings.wind;
        double ay = velocity.y * 20 - conditions.updraft;
        double az = velocity.z * 20 - conditions.wind.zPerTick() * 20 * settings.wind;
        double h = Math.hypot(ax, az), airSpeed = Math.sqrt(h * h + ay * ay);
        double dx = h > .001 ? ax / h : -Math.sin(angle), dz = h > .001 ? az / h : Math.cos(angle);
        if(!Float.isFinite(bankDegrees)) throw new IllegalArgumentException("Non-finite bank");
        double bank = Math.toRadians(Math.clamp(bankDegrees,-settings.maxBank,settings.maxBank));
        double attack = 8 - controls.forward * 6 - Math.clamp(pitch, -60, 60) * .08 + (controls.flare ? 12 : 0);
        double coefficient = settings.lift * Math.clamp(.8 + attack * .045, .1, 1.9);
        if (attack > 20) coefficient *= .65; // stall: pulling up indefinitely loses efficiency
        double pressureSpeed = Math.min(airSpeed, 35);
        double lift = liftNewtons(pressureSpeed, coefficient, settings);
        double drag = dragNewtons(pressureSpeed, settings.drag + coefficient * coefficient * .07
            + (controls.flare ? .18 : 0), settings);
        double inverseSpeed = airSpeed > .001 ? 1 / airSpeed : 0;
        double liftX = -ay * inverseSpeed * dx * Math.cos(bank) - dz * Math.sin(bank);
        double liftZ = -ay * inverseSpeed * dz * Math.cos(bank) + dx * Math.sin(bank);
        double liftY = h * inverseSpeed * Math.cos(bank);
        // Newtons / mass / 400 converts m/s² to blocks/tick²; 20 ticks = one second.
        double accelerationX = (lift * liftX - drag * ax * inverseSpeed) / MASS / 400;
        double accelerationY = (lift * liftY - drag * ay * inverseSpeed - MASS * 9.81) / MASS / 400;
        double accelerationZ = (lift * liftZ - drag * az * inverseSpeed) / MASS / 400;
        double turbulence = Math.clamp((conditions.wind.speed() - 12) / 18, 0, 1) * settings.turbulence;
        double phase = conditions.tick * .17 + Math.floorMod(conditions.seed, 4096) * .01;
        double x = velocity.x + Math.clamp(accelerationX, -.06, .06) + Math.sin(phase) * .004 * turbulence;
        double z = velocity.z + Math.clamp(accelerationZ, -.06, .06) + Math.cos(phase * 1.37) * .004 * turbulence;
        double horizontal = Math.hypot(x, z);
        if (horizontal > settings.maxSpeed) { x *= settings.maxSpeed / horizontal; z *= settings.maxSpeed / horizontal; }
        double y = Math.clamp(velocity.y + Math.clamp(accelerationY, -.06, .08)
            + Math.sin(phase * .73) * .003 * turbulence, -4, .25);
        return new Velocity(x, y, z);
    }
    public static double liftNewtons(double airSpeed, double coefficient, Settings settings) {
        return .5 * settings.density * airSpeed * airSpeed * settings.area * coefficient;
    }
    public static double dragNewtons(double airSpeed, double coefficient, Settings settings) {
        return .5 * settings.density * airSpeed * airSpeed * settings.area * coefficient;
    }

    public static boolean safeLanding(int flightTicks, double verticalSpeed) {
        return flightTicks >= 10 && verticalSpeed >= -SAFE_LANDING_SPEED && verticalSpeed <= .4;
    }
}
