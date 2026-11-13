package dev.windcraft.rendering;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WindAnimationTest {
    @Test void cloudsFollowCardinalWind() {
        var east = new WindAnimation();
        var north = new WindAnimation();
        for (int i = 0; i < 200; i++) { east.tick(20, 90, .25); north.tick(20, 0, .25); }
        assertTrue(east.cloudX(1) > 45);
        assertEquals(0, east.cloudZ(1), 1e-10);
        assertTrue(north.cloudZ(1) < -45);
        assertEquals(0, north.cloudX(1), 1e-10);
    }
    @Test void renderingDoesNotAdvanceSimulation() {
        var state = new WindAnimation();
        state.tick(20, 90, .25);
        double before = state.cloudX(1);
        for (int i = 0; i < 144; i++) state.cloudX(i / 144f);
        assertEquals(before, state.cloudX(1));
        assertEquals(state.cloudX(1) * .5, state.cloudX(.5f), 1e-10);
    }
    @Test void directionWrapUsesVectorsAndCalmStops() {
        var state = new WindAnimation();
        for (int i = 0; i < 200; i++) state.tick(10, 359, .25);
        state.tick(10, 1, .25);
        assertTrue(Math.abs(state.x(1)) < .02);
        assertTrue(state.z(1) < -.49);
        for (int i = 0; i < 200; i++) state.tick(0, 0, .25);
        assertEquals(0, state.x(1), 1e-9);
        assertEquals(0, state.z(1), 1e-9);
    }
    @Test void resetRemovesPreviousWorldAndDisabledCloudScaleStops() {
        var state = new WindAnimation();
        for (int i = 0; i < 100; i++) state.tick(20, 90, 0);
        assertEquals(0, state.cloudX(1));
        state.reset();
        assertEquals(0, state.x(1)); assertEquals(0, state.cloudZ(1));
        assertEquals(0, state.seconds(0));
        state.tick(0, 0, .25);
        assertEquals(.05f, state.seconds(1));
    }
}
