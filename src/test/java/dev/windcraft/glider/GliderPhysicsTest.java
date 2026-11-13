package dev.windcraft.glider;

import dev.windcraft.simulation.WindSample;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GliderPhysicsTest {
    private GliderPhysics.Velocity cruise(double speed, double direction, double updraft, GliderPhysics.Controls controls) {
        var v = new GliderPhysics.Velocity(0, -.2, .4);
        for (int tick = 0; tick < 2000; tick++) v = GliderPhysics.step(v, 0, 0, controls,
            new GliderPhysics.Conditions(new WindSample(speed, direction), updraft, tick, 17), GliderPhysics.DEFAULT);
        return v;
    }
    @Test void liftAndDragFollowDynamicPressure() {
        var s = GliderPhysics.DEFAULT;
        assertEquals(.5 * 1.225 * 100 * 12 * 1.2, GliderPhysics.liftNewtons(10, 1.2, s), 1e-9);
        assertEquals(4 * GliderPhysics.liftNewtons(5, 1.2, s), GliderPhysics.liftNewtons(10, 1.2, s), 1e-9);
        assertEquals(0, GliderPhysics.liftNewtons(0, 1.2, s));
        assertEquals(.5 * 1.225 * 100 * 12 * .1, GliderPhysics.dragNewtons(10, .1, s), 1e-9);
    }
    @Test void stillAirGlideDescendsAndMovesForward() {
        var v = cruise(0, 0, 0, GliderPhysics.Controls.IDLE);
        assertTrue(v.y() < 0 && v.y() > -.5, "sink=" + v.y());
        assertTrue(v.z() > .15, "forward=" + v.z());
    }
    @Test void noRelativeAirMeansNoArtificialLift() {
        var v = GliderPhysics.step(new GliderPhysics.Velocity(0, 0, 0), 0, 0, GliderPhysics.Controls.IDLE,
            new GliderPhysics.Conditions(new WindSample(0, 0), 0, 0, 0), GliderPhysics.DEFAULT);
        assertEquals(-9.81 / 400, v.y(), 1e-10); assertEquals(0, v.x()); assertEquals(0, v.z());
    }
    @Test void tailwindChangesGroundRangeAndHeadwindHasNoFixedLiftPenalty() {
        var calm = cruise(0, 0, 0, GliderPhysics.Controls.IDLE);
        var tail = cruise(10, 180, 0, GliderPhysics.Controls.IDLE);
        var head = cruise(10, 0, 0, GliderPhysics.Controls.IDLE);
        assertTrue(tail.z() > calm.z()); assertTrue(head.z() < calm.z());
        assertEquals(calm.y(), head.y(), .01); assertEquals(calm.y(), tail.y(), .01);
    }
    @Test void crosswindCreatesDrift() {
        assertTrue(cruise(10, 90, 0, GliderPhysics.Controls.IDLE).x() > 0);
        assertTrue(cruise(10, 270, 0, GliderPhysics.Controls.IDLE).x() < 0);
    }
    @Test void sufficientUpdraftAllowsAltitudeGain() {
        var still = cruise(0, 0, 0, GliderPhysics.Controls.IDLE);
        var thermal = cruise(0, 0, 8, GliderPhysics.Controls.IDLE);
        assertTrue(thermal.y() > still.y()); assertTrue(thermal.y() > 0, "climb=" + thermal.y());
    }
    @Test void strongWindIsBoundedAndTurbulenceIsDeterministic() {
        var input = new GliderPhysics.Velocity(.2, -.2, .4);
        var a = new GliderPhysics.Conditions(new WindSample(30, 90), 2, 50, 7);
        var b = new GliderPhysics.Conditions(new WindSample(30, 90), 2, 60, 7);
        assertEquals(GliderPhysics.step(input, 0, 0, GliderPhysics.Controls.IDLE, a, GliderPhysics.DEFAULT),
            GliderPhysics.step(input, 0, 0, GliderPhysics.Controls.IDLE, a, GliderPhysics.DEFAULT));
        assertNotEquals(GliderPhysics.step(input, 0, 0, GliderPhysics.Controls.IDLE, a, GliderPhysics.DEFAULT),
            GliderPhysics.step(input, 0, 0, GliderPhysics.Controls.IDLE, b, GliderPhysics.DEFAULT));
        for (int direction = 0; direction < 360; direction += 30) {
            var v = cruise(60, direction, 16, GliderPhysics.Controls.IDLE);
            assertTrue(Math.hypot(v.x(), v.z()) <= GliderPhysics.DEFAULT.maxSpeed() + 1e-9);
            assertTrue(v.y() <= .25 && v.y() >= -4);
        }
    }
    @Test void lateDeploymentDoesNotInstantlyCancelFallDamage() {
        var v = GliderPhysics.step(new GliderPhysics.Velocity(0, -3, .1), 0, 0, GliderPhysics.Controls.IDLE,
            new GliderPhysics.Conditions(new WindSample(0, 0), 0, 1, 1), GliderPhysics.DEFAULT);
        assertTrue(v.y() < -2.8);
        assertFalse(GliderPhysics.safeLanding(1, v.y()));
        assertFalse(GliderPhysics.safeLanding(50, -1)); assertTrue(GliderPhysics.safeLanding(50, -.16));
    }
    @Test void steeringChangesHeading() {
        var left = cruise(0, 0, 0, new GliderPhysics.Controls(0, -1, false));
        var right = cruise(0, 0, 0, new GliderPhysics.Controls(0, 1, false));
        assertTrue(left.x() > 0); assertTrue(right.x() < 0);
    }
    @Test void mouseLookSteersWithoutKeyboardInput() {
        for (float yaw : new float[]{-90, 90, 179, -179}) {
            var v = new GliderPhysics.Velocity(0, -.15, .6);
            var conditions = new GliderPhysics.Conditions(new WindSample(0, 0), 0, 0, 1);
            for (int tick = 0; tick < 500; tick++)
                v = GliderPhysics.step(v, yaw, 0, GliderPhysics.Controls.IDLE, conditions, GliderPhysics.DEFAULT);
            double alignment = -Math.sin(Math.toRadians(yaw)) * v.x() + Math.cos(Math.toRadians(yaw)) * v.z();
            assertTrue(alignment > .15, "look yaw=" + yaw + ", velocity=" + v);
        }
    }
    @Test void immediateDeploymentAtJumpApexIsFiniteAndDoesNotGrantFreeFlight() {
        var v = new GliderPhysics.Velocity(0, 0, 0);
        for (int tick = 0; tick < 40; tick++) {
            v = GliderPhysics.step(v, 0, 0, GliderPhysics.Controls.IDLE,
                new GliderPhysics.Conditions(new WindSample(0, 0), 0, tick, 1), GliderPhysics.DEFAULT);
            assertTrue(Double.isFinite(v.x()) && Double.isFinite(v.y()) && Double.isFinite(v.z()));
            assertTrue(v.y() < 0, "Opening at zero fall distance must not generate free lift");
        }
    }
    @Test void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> GliderPhysics.step(new GliderPhysics.Velocity(Double.NaN, 0, 0), 0, 0,
            GliderPhysics.Controls.IDLE, new GliderPhysics.Conditions(new WindSample(0, 0), 0, 0, 0), GliderPhysics.DEFAULT));
    }
    @Test void pitchingTradesHeightForSpeed() {
        var normal = cruise(0, 0, 0, GliderPhysics.Controls.IDLE);
        var dive = cruise(0, 0, 0, new GliderPhysics.Controls(1, 0, false));
        var slow = cruise(0, 0, 0, new GliderPhysics.Controls(-1, 0, false));
        assertTrue(dive.z() > normal.z()); assertTrue(dive.y() < normal.y());
        assertTrue(slow.z() < normal.z());
    }
    @Test void configuredLimitIsApplied() {
        var s = GliderPhysics.DEFAULT;
        var limited = new GliderPhysics.Settings(s.density(), s.area(), s.lift(), s.drag(), s.wind(), .4, s.sensitivity(), s.turbulence());
        var v = GliderPhysics.step(new GliderPhysics.Velocity(2, -.2, 2), 0, 0, GliderPhysics.Controls.IDLE,
            new GliderPhysics.Conditions(new WindSample(60, 90), 8, 100, 0), limited);
        assertEquals(.4, Math.hypot(v.x(), v.z()), 1e-10);
    }
    @Test void flareUsesAirspeedAndIncreasesDrag() {
        var conditions = new GliderPhysics.Conditions(new WindSample(0, 0), 0, 100, 0);
        var fast = new GliderPhysics.Velocity(0, -.1, .7);
        var normal = GliderPhysics.step(fast, 0, 0, GliderPhysics.Controls.IDLE, conditions, GliderPhysics.DEFAULT);
        var flare = GliderPhysics.step(fast, 0, 0, new GliderPhysics.Controls(0, 0, true), conditions, GliderPhysics.DEFAULT);
        assertTrue(flare.y() > normal.y());
        assertTrue(flare.z() < normal.z());
        var still = GliderPhysics.step(new GliderPhysics.Velocity(0, 0, 0), 0, 0,
            new GliderPhysics.Controls(0, 0, true), conditions, GliderPhysics.DEFAULT);
        assertTrue(still.y() < 0);
    }
}
