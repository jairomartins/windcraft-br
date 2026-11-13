package dev.windcraft.rendering;

/** Fit to GUI-scaled viewport; offsets allow avoiding overlays that expose no bounds. */
public record GliderHudLayout(int x, int y, int width, int height, float scale) {
    public static GliderHudLayout fit(int screenWidth, int screenHeight, int textWidth, int rows,
                                     double requestedScale, int requestedX, int requestedY) {
        int width = Math.max(40, textWidth + 12), height = rows * 11 + 10;
        double scale = Math.max(.01, Math.min(requestedScale, Math.min(
            Math.max(1, screenWidth - 8) / (double)width, Math.max(1, screenHeight - 8) / (double)height)));
        int x = Math.clamp(requestedX, 0, Math.max(0, screenWidth - (int)Math.ceil(width * scale) - 4));
        int y = Math.clamp(requestedY, 0, Math.max(0, screenHeight - (int)Math.ceil(height * scale) - 4));
        return new GliderHudLayout(x, y, width, height, (float)scale);
    }
}
