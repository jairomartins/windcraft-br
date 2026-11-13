package dev.windcraft.rendering;
import dev.windcraft.network.WindNetwork;
import dev.windcraft.simulation.WindSample;
import dev.windcraft.config.WindVisualConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;

public final class WindClient {
    private static WindNetwork.Snapshot target;
    private static double vx, vz;
    private static int age;
    private static final java.util.List<net.minecraft.client.particle.Particle> carried = new java.util.ArrayList<>();
    private static net.minecraft.client.multiplayer.ClientLevel previousLevel;
    private static final WindAnimation animation = new WindAnimation();
    private static float framePartial;
    public static boolean cloudsActive;

    /** Called once per frame when Minecraft uploads its camera/global uniforms. */
    public static void prepareFrame(float partial) {
        framePartial = Math.clamp(partial, 0, 1);
        cloudsActive = WindVisualConfig.CLOUDS.get() && target != null;
    }
    public static net.minecraft.world.phys.Vec3 cloudCamera(net.minecraft.world.phys.Vec3 camera) {
        return camera.add(-animation.cloudX(framePartial), 0, -animation.cloudZ(framePartial));
    }
    public static void install() {
        WindNetwork.receiver = p -> { target = p; age = 0; };
        TickEvent.ClientTickEvent.Post.BUS.addListener(WindClient::tick);
    }
    private static void tick(TickEvent.ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (previousLevel != mc.level) {
            carried.forEach(net.minecraft.client.particle.Particle::remove);
            carried.clear(); previousLevel = mc.level; vx = vz = 0;
            animation.reset(); target = null; cloudsActive = false;
        }
        if (mc.level == null || mc.player == null) { target = null; vx = vz = 0; return; }
        if (mc.isPaused()) return;
        if (target == null || ++age > 120 || !target.dimension().equals(mc.level.dimension().identifier().toString())) {
            vx = vz = 0; animation.tick(0, 0, WindVisualConfig.CLOUD_SCALE.get()); return;
        }
        animation.tick(target.atmosphericSpeed(), target.direction(), WindVisualConfig.CLOUD_SCALE.get());
        WindSample wind = new WindSample(target.speed(), target.direction());
        vx += (wind.xPerTick() - vx) * 0.12;
        vz += (wind.zPerTick() - vz) * 0.12;
        if (!target.particles()) { carried.forEach(net.minecraft.client.particle.Particle::remove); carried.clear(); return; }
        carried.removeIf(p -> {
            if (!p.isAlive()) return true;
            if (p.getPos().distanceToSqr(mc.player.position()) > 32 * 32) { p.remove(); return true; }
            p.setParticleSpeed(vx, -0.015, vz);
            return false;
        });
        double speed = Math.hypot(vx, vz) * 20;
        var random = mc.level.getRandom();
        int count = Math.min(5, (int)(speed / 4) + (random.nextDouble() < speed / 4 % 1 ? 1 : 0));
        for (int i = 0; i < count; i++) {
            if (carried.size() >= 160) break;
            double x = mc.player.getX() + random.nextDouble() * 32 - 16;
            double y = mc.player.getY() + random.nextDouble() * 10 - 2;
            double z = mc.player.getZ() + random.nextDouble() * 32 - 16;
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!mc.level.hasChunkAt(pos) || mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > y) continue;
            var particle = mc.particleEngine.createParticle(i % 3 == 0 ? ParticleTypes.CHERRY_LEAVES : ParticleTypes.FALLING_SPORE_BLOSSOM, x, y, z, vx, 0, vz);
            if (particle != null) {
                particle.setParticleSpeed(vx, -0.015, vz);
                particle.setLifetime(60 + random.nextInt(40));
                carried.add(particle);
            }
        }
    }
}
