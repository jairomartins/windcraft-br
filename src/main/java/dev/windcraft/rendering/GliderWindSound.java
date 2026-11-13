package dev.windcraft.rendering;

import dev.windcraft.config.WindVisualConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** One listener-relative vanilla wind loop, never a repeated server sound packet. */
public final class GliderWindSound extends AbstractTickableSoundInstance {
    private static GliderWindSound current;
    private final net.minecraft.client.player.LocalPlayer pilot;
    private float targetVolume, targetPitch;

    private GliderWindSound(net.minecraft.client.player.LocalPlayer pilot) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.pilot = pilot;
        looping = true; relative = true; delay = 0;
        attenuation = SoundInstance.Attenuation.NONE;
        volume = .001f; pitch = .8f;
    }
    @Override public boolean canStartSilent() { return true; }
    public static void update(boolean active, double airspeed) {
        var mc = Minecraft.getInstance();
        if (!WindVisualConfig.GLIDER_SOUND.get() || mc.player == null) { clear(); return; }
        if (current != null && (current.isStopped() || current.pilot != mc.player)) clear();
        if (active && current == null) {
            current = new GliderWindSound(mc.player);
            mc.getSoundManager().play(current);
        }
        if (current != null) {
            current.targetVolume = active ? (float)(WindVisualConfig.GLIDER_SOUND_VOLUME.get()
                * Math.clamp(airspeed / 28, .04, 1)) : 0;
            current.targetPitch = (float)Math.clamp(.7 + airspeed / 65, .7, 1.3);
        }
    }
    public static void clear() {
        if (current != null) {
            current.stop(); Minecraft.getInstance().getSoundManager().stop(current); current = null;
        }
    }
    @Override public void tick() {
        var mc = Minecraft.getInstance();
        if (mc.player != pilot || !pilot.isAlive() || mc.level != pilot.level()) { stop(); return; }
        volume += (targetVolume - volume) * .15f;
        pitch += (targetPitch - pitch) * .1f;
        if (targetVolume == 0 && volume < .002f) stop();
    }
}
