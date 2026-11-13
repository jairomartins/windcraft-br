package dev.windcraft.rendering;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GliderHudLayoutTest {
    @Test void panelFitsSmallAndLargeGuiViewports() {
        for (int width : new int[]{160, 320, 640, 1920}) {
            var box = GliderHudLayout.fit(width, 120, 410, 2, 2, 8, 8);
            assertTrue(box.x() + box.width() * box.scale() <= width + .01);
            assertTrue(box.y() + box.height() * box.scale() <= 120 + .01);
        }
    }
    @Test void offsetsAreClampedInsideTheViewport() {
        var box = GliderHudLayout.fit(320, 180, 260, 2, 1, 2000, 2000);
        assertTrue(box.x() + box.width() * box.scale() <= 320);
        assertTrue(box.y() + box.height() * box.scale() <= 180);
    }
    @Test void configuredScaleIsPreservedWhenThereIsRoom() {
        assertEquals(1.5f, GliderHudLayout.fit(1920, 1080, 300, 2, 1.5, 8, 8).scale());
    }
}
