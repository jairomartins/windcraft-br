package dev.windcraft.simulation;

/** Direction is where air travels: 0 north (-Z), 90 east (+X). */
public record WindSample(double speed, double direction) {
    public WindSample {
        if (!Double.isFinite(speed) || !Double.isFinite(direction)) throw new IllegalArgumentException("Non-finite wind");
        speed = Math.clamp(speed, 0, 60);
        direction = ((direction % 360) + 360) % 360;
    }
    public double xPerTick() { return Math.sin(Math.toRadians(direction)) * speed / 20; }
    public double zPerTick() { return -Math.cos(Math.toRadians(direction)) * speed / 20; }
    public String cardinal() { return new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"}[(int)Math.round(direction / 45) % 8]; }
    public String intensity() { return speed < 0.5 ? "calmo" : speed < 3.4 ? "brisa leve" : speed < 8 ? "moderado" : speed < 14 ? "forte" : "muito forte"; }
    public String describe() { return String.format(java.util.Locale.ROOT, "Vento: %.1f m/s | %.0f° %s | %s", speed, direction, cardinal(), intensity()); }
}
