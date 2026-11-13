package dev.windcraft.simulation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WindEngineTest {
    private WindSample sample(long t, boolean rain, boolean thunder, double exposure) {
        return WindEngine.sample(123, t, .25, rain, thunder, 1, 0, exposure, 5, -1, -1);
    }
    @Test void deterministicAndBounded() {
        for (long t = 0; t < 240000; t += 20) {
            var w = sample(t, false, false, 1);
            assertEquals(w, sample(t, false, false, 1));
            assertTrue(w.speed() >= 0 && w.speed() <= 60);
            assertTrue(w.direction() >= 0 && w.direction() < 360);
        }
    }
    @Test void weatherAndShelter() {
        assertTrue(sample(500, true, false, 1).speed() > sample(500, false, false, 1).speed());
        assertTrue(sample(500, true, true, 1).speed() > sample(500, true, false, 1).speed());
        assertEquals(sample(500, false, false, 1).speed() * .05, sample(500, false, false, .05).speed(), 1e-10);
    }
    @Test void smoothAcrossNoiseBoundaries() {
        for (long t = 1; t < 240000; t++) {
            assertTrue(Math.abs(sample(t, false, false, 1).speed() - sample(t-1, false, false, 1).speed()) < .1);
            assertTrue(Math.abs(sample(t, false, false, 1).direction() - sample(t-1, false, false, 1).direction()) < .2);
        }
    }
    @Test void overridesAndUnits() {
        var w = WindEngine.sample(1, 100, 0, true, true, 2, 100, 1, 5, 20, 90);
        assertEquals(20, w.speed()); assertEquals(1, w.xPerTick(), 1e-10); assertEquals(0, w.zPerTick(), 1e-10);
        assertEquals("E", w.cardinal()); assertEquals(0, new WindSample(1, 360).direction());
        assertThrows(IllegalArgumentException.class, () -> new WindSample(Double.NaN, 0));
    }
    @Test void altitudeBiomeAndDayInfluence() {
        var base = WindEngine.sample(9, 500, .25, false, false, 1, 0, 1, 5, -1, -1);
        var high = WindEngine.sample(9, 500, .25, false, false, 1, 128, 1, 5, -1, -1);
        var forest = WindEngine.sample(9, 500, .25, false, false, .65, 0, 1, 5, -1, -1);
        var night = WindEngine.sample(9, 500, .75, false, false, 1, 0, 1, 5, -1, -1);
        assertTrue(high.speed() > base.speed());
        assertTrue(forest.speed() < base.speed());
        assertTrue(night.speed() < base.speed());
    }
}
