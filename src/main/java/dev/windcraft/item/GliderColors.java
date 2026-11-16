package dev.windcraft.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import java.util.Set;

/** Vanilla item component persists and synchronizes the fabric color. Wood is never recolored. */
public final class GliderColors {
    private static final Set<String> COLORS=Set.of("white","orange","magenta","light_blue","yellow","lime",
        "pink","gray","light_gray","cyan","purple","blue","brown","green","red","black");
    private GliderColors() {}
    public static String valid(String color) { return COLORS.contains(color) ? color : "orange"; }
    public static String of(ItemStack stack) {
        var data=stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA,CustomModelData.EMPTY);
        var color=data.getString(0);
        return color==null ? "orange" : valid(color);
    }
}
