package dev.windcraft.glider;

/** Flight state is independent of the permanently open item model. */
public final class GliderState {
    public enum Phase { CLOSED, GLIDING }
    private Phase phase = Phase.CLOSED;
    public boolean open() {
        if (phase != Phase.CLOSED) return false;
        phase = Phase.GLIDING; return true;
    }
    public void close() { phase = Phase.CLOSED; }
    public boolean flying() { return phase == Phase.GLIDING; }
    public Phase phase() { return phase; }
}
