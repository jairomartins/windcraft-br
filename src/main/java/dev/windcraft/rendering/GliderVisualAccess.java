package dev.windcraft.rendering;

/** Client-only extension of the extracted avatar render state. */
public interface GliderVisualAccess {
    GliderVisualState.Frame windcraft$frame();
    void windcraft$frame(GliderVisualState.Frame frame);
}
