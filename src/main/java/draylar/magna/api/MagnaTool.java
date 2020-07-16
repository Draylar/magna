package draylar.magna.api;

import draylar.magna.Magna;
import draylar.magna.api.event.ToolRadiusCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Represents a tool that should break in a certain radius.
 * <p>
 * Implementers can either make a custom tool using this interface,
 * or use one of the provided base item classes,
 * {@link draylar.magna.item.HammerItem} and {@link draylar.magna.item.ExcavatorItem}.
 */
public interface MagnaTool {

    /**
     * Returns the base breaking radius of this {@link MagnaTool}.
     * <p>
     * The full area of a break is calculated with: 1 + (2 * getRadius()), or:
     *   - 3x3 for a radius of 1
     *   - 5x5 for a radius of 2
     *   - 7x7 for a radius of 3
     * and so on.
     * Tools that have a dynamic radius can either change their radius based on {@link ItemStack},
     * or add a listener to {@link ToolRadiusCallback}.
     *
     * @param stack  current {@link MagnaTool} stack being used
     * @return       breaking radius of stack
     */
    int getRadius(ItemStack stack);

    boolean playBreakEffects();

    /**
     * Provides simple functionality for tools attempting to break blocks in a certain radius.
     * <p>
     * Before breaking, config options are checked, hardness is checked, effectiveness is checked,
     * and radius events are triggered.
     *
     * @param world        world to attempt to break blocks in
     * @param pos          center position to break at
     * @param player       player breaking the blocks
     * @param breakRadius  radius to break blocks in, 1 is 3x3, 2 is 5x5
     * @return             whether the break was successful
     */
    default boolean attemptBreak(World world, BlockPos pos, PlayerEntity player, int breakRadius) {
        if (Magna.CONFIG.breakSingleBlockWhenSneaking && player.isSneaking()) {
            return true;
        }

        // calculate initial hardness & get current breaking stack
        float originHardness = world.getBlockState(pos).getHardness(world, pos);
        ItemStack mainHandStack = player.getMainHandStack();

        // only do a 3x3 break if the player's tool is effective on the block they are breaking
        // this makes it so breaking gravel doesn't break nearby stone
        if (mainHandStack.isEffectiveOn(world.getBlockState(pos))) {
            int radius = ToolRadiusCallback.EVENT.invoker().getRadius(mainHandStack, breakRadius);

            // break blocks
            BlockBreaker.breakInRadius(world, player, radius, (breakState) -> {
                double hardness = breakState.getHardness(null, null);
                boolean isEffective = mainHandStack.isEffectiveOn(breakState);
                boolean verifyHardness = hardness < originHardness * 5 && hardness > 0;
                return isEffective && verifyHardness;
            }, true);
        }

        return true;
    }
}
