package dev.windcraft.glider;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GliderStateTest {
    @Test void activationIsImmediateAndCannotDuplicate() {
        var state = new GliderState();
        assertEquals(GliderState.Phase.CLOSED, state.phase());
        assertTrue(state.open()); assertFalse(state.open()); assertTrue(state.flying());
        assertEquals(GliderState.Phase.GLIDING, state.phase());
        state.close(); assertFalse(state.flying());
        assertEquals(GliderState.Phase.CLOSED, state.phase());
        assertTrue(state.open());
    }
    @Test void deactivationIsIdempotent() {
        var state = new GliderState(); state.open(); state.close(); state.close();
        assertEquals(GliderState.Phase.CLOSED, state.phase()); assertFalse(state.flying());
    }
}
