package dev.windcraft.rendering;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GliderVisualStateTest {
    @Test void opensAndClosesWithoutChangingPhysicsState() {
        var pose = new GliderVisualState();
        assertFalse(pose.frame(1).visible());
        pose.tick(true, 0, 0, 0, true);
        assertEquals(GliderVisualState.Phase.OPENING, pose.frame(1).phase());
        assertTrue(pose.frame(1).blend() > 0 && pose.frame(1).blend() < 1);
        for (int i = 0; i < 15; i++) pose.tick(true, 0, 0, 0, true);
        assertEquals(GliderVisualState.Phase.GLIDING, pose.frame(1).phase()); assertEquals(1, pose.frame(1).blend());
        pose.tick(false, 0, 0, 0, true);
        assertEquals(GliderVisualState.Phase.CLOSING, pose.frame(1).phase());
        for (int i = 0; i < 15; i++) pose.tick(false, 0, 0, 0, true);
        assertEquals(GliderVisualState.Phase.NORMAL, pose.frame(1).phase());
    }
    @Test void wrapAroundDoesNotRotateAnEntireCircle() {
        var pose = new GliderVisualState(); pose.tick(true, 179, 0, 0, true);
        pose.tick(true, -179, 0, 0, true);
        assertTrue(pose.frame(1).yaw() > 179 && pose.frame(1).yaw() < 181);
    }
    @Test void pitchAndBankAreBoundedDuringGusts() {
        var pose = new GliderVisualState();
        for (int i = 0; i < 100; i++) pose.tick(true, 0, 900, -900, true);
        assertTrue(Math.abs(pose.frame(1).pitch()) <= 15);
        assertTrue(Math.abs(pose.frame(1).bank()) <= 40);
    }
    @Test void renderSamplingDoesNotAdvanceTheAnimation() {
        var pose = new GliderVisualState(); pose.tick(true, 0, 0, 0, true);
        var before = pose.frame(1);
        for (int i = 0; i < 144; i++) pose.frame(i / 144f);
        assertEquals(before, pose.frame(1));
        assertTrue(pose.frame(.5f).blend() < before.blend());
    }
    @Test void interruptedOpeningAndResetDoNotLeaveStalePose() {
        var pose = new GliderVisualState(); pose.tick(true, 90, 12, 20, true);
        pose.tick(false, 90, 0, 0, true); pose.tick(false, 90, 0, 0, true);
        assertFalse(pose.frame(1).visible());
        pose.tick(true, 90, 0, 0, true); pose.reset();
        assertEquals(GliderVisualState.Frame.NONE, pose.frame(1));
    }
    @Test void transitionsCanBeDisabled() {
        var pose = new GliderVisualState(); pose.tick(true, 0, 0, 0, false);
        assertEquals(1, pose.frame(1).blend()); pose.tick(false, 0, 0, 0, false);
        assertEquals(0, pose.frame(1).blend());
        assertEquals(0, pose.frame(0).blend());
    }
}
