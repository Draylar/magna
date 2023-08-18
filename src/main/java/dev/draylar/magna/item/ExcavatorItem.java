package dev.draylar.magna.item;

import dev.draylar.magna.api.MagnaTool;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;

/**
 * Represents a tool that can mine dirt (similar to {@link ShovelItem}).
 *
 * <p>Effectiveness is determined by this tool's {@link ToolMaterial}.
 * By default, this {@link ExcavatorItem} will have a radius of 1 (3x3 blocks broken).
 */
public class ExcavatorItem extends ShovelItem implements MagnaTool {

    private int breakRadius = 1;

    public ExcavatorItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, int breakRadius) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.breakRadius = breakRadius;
    }

    public ExcavatorItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
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
