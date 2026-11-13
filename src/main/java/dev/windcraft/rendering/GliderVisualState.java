package dev.windcraft.rendering;

/** Pure visual interpolation. Never reads or modifies physical player state. */
public final class GliderVisualState {
    public enum Phase { NORMAL, OPENING, GLIDING, CLOSING }
    public record Frame(float blend, float yaw, float pitch, float bank, Phase phase) {
        public static final Frame NONE = new Frame(0, 0, 0, 0, Phase.NORMAL);
        public boolean visible() { return blend > .001f; }
    }
    private float progress, oldProgress, yaw, oldYaw, pitch, oldPitch, bank, oldBank;
    private boolean initialized, active;
    public void tick(boolean target, float heading, float verticalPitch, float turn, boolean transitions) {
        if (!initialized) { yaw = oldYaw = heading; initialized = true; }
        oldProgress = progress; oldYaw = yaw; oldPitch = pitch; oldBank = bank; active = target;
        progress = transitions ? Math.clamp(progress + (target ? .1f : -.1f), 0, 1) : target ? 1 : 0;
        if (!transitions) oldProgress = progress;
        yaw += wrap(heading - yaw) * .18f;
        pitch += (Math.clamp(verticalPitch, -15, 15) - pitch) * .12f;
        bank += (Math.clamp(turn, -22, 22) - bank) * .16f;
    }
    public Frame frame(float partial) {
        float t = Math.clamp(partial, 0, 1);
        float amount = oldProgress + (progress - oldProgress) * t;
        amount = amount * amount * (3 - 2 * amount);
        Phase phase = progress == 0 && oldProgress == 0 ? Phase.NORMAL
            : active ? progress == 1 ? Phase.GLIDING : Phase.OPENING : Phase.CLOSING;
        return new Frame(amount, oldYaw + wrap(yaw - oldYaw) * t,
            oldPitch + (pitch - oldPitch) * t, oldBank + (bank - oldBank) * t, phase);
    }
    public void reset() { progress = oldProgress = yaw = oldYaw = pitch = oldPitch = bank = oldBank = 0; initialized = active = false; }
    public static float wrap(float degrees) { return (float)(degrees - Math.floor((degrees + 180) / 360) * 360); }
}
