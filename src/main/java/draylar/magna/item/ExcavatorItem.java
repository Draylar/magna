package draylar.magna.item;

import com.google.common.collect.Sets;
import draylar.magna.api.MagnaTool;
import draylar.magna.mixin.MiningToolItemAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;

import java.util.Set;

/**
 * Represents a tool that can mine stone (similar to {@link PickaxeItem}).
 *
 * <p>Effectiveness is determined by this tool's {@link ToolMaterial}.
 * By default, this {@link ExcavatorItem} will have a radius of 1 (3x3 blocks broken).
 */
public class ExcavatorItem extends ShovelItem implements MagnaTool {

    private static final Set<Material> EFFECTIVE_MATERIALS = Sets.newHashSet(
        Material.SOIL, Material.SNOW_BLOCK, Material.SNOW_LAYER, Material.SOLID_ORGANIC, Material.AGGREGATE
    );

    private int breakRadius = 1;

    public ExcavatorItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, int breakRadius) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.breakRadius = breakRadius;
    }

    public ExcavatorItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean isEffectiveOn(BlockState state) {
        return super.isEffectiveOn(state) || EFFECTIVE_MATERIALS.contains(state.getMaterial());
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return ((MiningToolItemAccessor) this).getEffectiveBlocks().contains(state.getBlock()) ? this.miningSpeed : 1.0F;
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
