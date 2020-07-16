package draylar.magna.item;

import draylar.magna.api.MagnaTool;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;

/**
 * Represents a tool that can mine stone (similar to {@link PickaxeItem}).
 *
 * <p>Effectiveness is determined by this tool's {@link ToolMaterial}.
 * By default, this {@link HammerItem} will have a radius of 1 (3x3 blocks broken).
 */
public class HammerItem extends PickaxeItem implements MagnaTool {

    private int breakRadius = 1;

    public HammerItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, int breakRadius) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.breakRadius = breakRadius;
    }

    public HammerItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getRadius(ItemStack stack) {
        return breakRadius;
    }

    @Override
    public boolean playBreakEffects() {
        return false;
    }
}
