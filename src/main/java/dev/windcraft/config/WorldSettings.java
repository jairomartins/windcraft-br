package dev.windcraft.config;
import java.nio.file.*;
import java.io.*;
import java.util.Properties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
/** Dimension overrides persisted beside the world. */
public final class WorldSettings {
    public double speed = -1, direction = -1;
    private final Path file;
    public WorldSettings(ServerLevel level) {
        String id = level.dimension().identifier().toString().replace(':', '_').replace('/', '_');
        file = level.getServer().getWorldPath(LevelResource.ROOT).resolve("serverconfig/windcraft-" + id + ".properties");
        if (Files.exists(file)) {
            try (var in = Files.newInputStream(file)) {
                var p = new Properties(); p.load(in);
                speed = parse(p.getProperty("speed", "-1"), 60);
                direction = parse(p.getProperty("direction", "-1"), 360);
            } catch (IOException | IllegalArgumentException e) { throw new IllegalStateException("Cannot load " + file, e); }
        }
    }
    private static double parse(String text, double max) {
        double v = Double.parseDouble(text);
        if (!Double.isFinite(v) || (v < 0 && v != -1) || v > max) throw new IllegalArgumentException("Invalid wind setting");
        return v;
    }
    public void save() throws IOException {
        Files.createDirectories(file.getParent());
        var p = new Properties(); p.setProperty("speed", Double.toString(speed)); p.setProperty("direction", Double.toString(direction));
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try (var out = Files.newOutputStream(temp)) { p.store(out, "WindCraft; -1 = automatic"); }
        try { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException e) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
    }
}
