package dev.windcraft.glider;

import dev.windcraft.simulation.WindSample;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GliderHeadingTest {
    private static final GliderPhysics.Settings SETTINGS = GliderPhysics.DEFAULT;

    @Test void tailwindCannotLaunchAStationaryFallingPilotBackTowardsTheCliff() {
        for (float heading : new float[]{0, 90, 180, -90, 179, -179}) {
            double angle = Math.toRadians(heading);
            var wind = new WindSample(10, heading + 180);
            var v = new GliderPhysics.Velocity(0, -.15, 0);
            for (int tick = 0; tick < 40; tick++) {
                var conditions = new GliderPhysics.Conditions(wind, 0, tick, 1);
                float nextHeading = GliderPhysics.updateHeading(heading, 0, v, conditions, SETTINGS);
                assertEquals(GliderPhysics.wrapHeading(heading), nextHeading);
                v = GliderPhysics.stepWithHeading(v, nextHeading, 0, GliderPhysics.Controls.IDLE, conditions, SETTINGS, 0);
                double forward = -Math.sin(angle) * v.x() + Math.cos(angle) * v.z();
                assertTrue(forward >= -1e-10, "No upwind catapult at heading=" + heading + ", tick=" + tick + ": " + v);
            }
        }
    }

    @Test void crosswindDriftsDownwindInsteadOfSelectingAnUpwindNose() {
        for (int direction : new int[]{90, 270}) {
            var wind = new WindSample(10, direction);
            var conditions = new GliderPhysics.Conditions(wind, 0, 0, 1);
            var v = GliderPhysics.stepWithHeading(new GliderPhysics.Velocity(0, -.15, 0), 0, 0,
                GliderPhysics.Controls.IDLE, conditions, SETTINGS, 0);
            assertTrue(v.x() * wind.xPerTick() > 0, "Side drift must follow wind, not oppose it: " + v);
            assertTrue(v.z() >= 0, "Descending airflow can build forward motion without yawing into the wind");
        }
    }

    @Test void unbankedNoseDoesNotFollowWindOrReversedGroundMotion() {
        var reversed = new GliderPhysics.Velocity(.1, -.2, -.4);
        for (int direction = 0; direction < 360; direction += 15) {
            var conditions = new GliderPhysics.Conditions(new WindSample(30, direction), 0, 1, 1);
            assertEquals(37, GliderPhysics.updateHeading(37, 0, reversed, conditions, SETTINGS));
        }
    }

    @Test void bankTurnsBothWaysAndWrapsAtNorthWithoutSnapping() {
        var conditions = new GliderPhysics.Conditions(new WindSample(0, 0), 0, 0, 1);
        var v = new GliderPhysics.Velocity(0, -.15, .6);
        float right = GliderPhysics.updateHeading(0, 25, v, conditions, SETTINGS);
        float left = GliderPhysics.updateHeading(0, -25, v, conditions, SETTINGS);
        assertTrue(right > 0 && right < 3);
        assertEquals(-right, left, 1e-5);
        float wrapped = GliderPhysics.updateHeading(179.9f, 25, new GliderPhysics.Velocity(0, -.15, -.6), conditions, SETTINGS);
        assertTrue(wrapped >= -180 && wrapped < -177);
    }

    @Test void liveBankedFlightRemainsBoundedAndLosesHeightWithoutWind() {
        var v = new GliderPhysics.Velocity(0, -.15, .6);
        float heading = 0;
        double height = 0;
        for (int tick = 0; tick < 4000; tick++) {
            var conditions = new GliderPhysics.Conditions(new WindSample(0, 0), 0, tick, 1);
            heading = GliderPhysics.updateHeading(heading, 25, v, conditions, SETTINGS);
            v = GliderPhysics.stepWithHeading(v, heading, 0, GliderPhysics.Controls.IDLE, conditions, SETTINGS, 25);
            assertTrue(Double.isFinite(v.x()) && Double.isFinite(v.y()) && Double.isFinite(v.z()));
            assertTrue(Math.hypot(v.x(), v.z()) <= SETTINGS.maxSpeed() + 1e-9);
            height += v.y();
        }
        assertTrue(height < -50, "Banking must not create sustained altitude: " + height);
    }

    @Test void liveDeploymentWithoutAirspeedDoesNotCreateThrustOrLift() {
        var conditions = new GliderPhysics.Conditions(new WindSample(0, 0), 0, 0, 1);
        var v = GliderPhysics.stepWithHeading(new GliderPhysics.Velocity(0, 0, 0), 75, 0,
            GliderPhysics.Controls.IDLE, conditions, SETTINGS, 0);
        assertEquals(0, v.x()); assertEquals(0, v.z());
        assertEquals(-9.81 / 400, v.y(), 1e-10);
    }

    @Test void strongWindAndThermalsRemainBoundedForEveryTakeoffDirection() {
        for (int direction = 0; direction < 360; direction += 15) {
            var v = new GliderPhysics.Velocity(0, -.15, .1);
            for (int tick = 0; tick < 200; tick++) {
                var conditions = new GliderPhysics.Conditions(new WindSample(60, direction), 16, tick, 1);
                assertEquals(0, GliderPhysics.updateHeading(0, 0, v, conditions, SETTINGS));
                v = GliderPhysics.stepWithHeading(v, 0, 0, GliderPhysics.Controls.IDLE, conditions, SETTINGS, 0);
                assertTrue(Double.isFinite(v.x()) && Double.isFinite(v.y()) && Double.isFinite(v.z()));
                assertTrue(Math.hypot(v.x(), v.z()) <= SETTINGS.maxSpeed() + 1e-9);
                assertTrue(v.y() >= -4 && v.y() <= .25);
            }
        }
    }

    @Test void alignedForwardFlightRetainsExistingForces() {
        var velocity = new GliderPhysics.Velocity(0, -.15, .6);
        var conditions = new GliderPhysics.Conditions(new WindSample(10, 0), 0, 1, 1);
        assertEquals(GliderPhysics.step(velocity, 0, 0, GliderPhysics.Controls.IDLE, conditions, SETTINGS, 0),
            GliderPhysics.stepWithHeading(velocity, 0, 0, GliderPhysics.Controls.IDLE, conditions, SETTINGS, 0));
    }
}
