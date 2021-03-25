package draylar.magna.api;

import draylar.magna.Magna;
import draylar.magna.api.event.ToolRadiusCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
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

    /**
     * Returns the depth of this {@link MagnaTool}.
     * <p>
     * Unlike radius, which defines the number of perpendicular blocks broken, around a central point, to the player,
     *   depth is how far back it goes. A depth of 0 (default) means a single layer is broken, while a depth of 3 with a
     *   radius of 3 represents a 3x3 cube with the origin 1 block away from the central block being broken.
     *
     * @param stack   current {@link MagnaTool} stack being used
     * @return        breaking depth of stack
     */
    default int getDepth(ItemStack stack) {
        return 0;
    }

    /**
     * @return whether or not this {@link MagnaTool} should run sound/particle effects when neighboring blocks are broken.
     */
    boolean playBreakEffects();

    /**
     * Modifies which block will be considered the center of the radius.
     * <p>
     * This is useful for tools with a big radius to avoid breaking blocks under the player.
     *
     * @param world           world the block is breaking
     * @param player          player that is breaking
     * @param blockHitResult  raycast result from where the player is looking to the block being mined
     * @param toolStack       {@link MagnaTool} currently being held by the player
     * @return                a {@link BlockPos} that will define the center of the radius
     */
    default BlockPos getCenterPosition(World world, PlayerEntity player, BlockHitResult blockHitResult, ItemStack toolStack) {
        return blockHitResult.getBlockPos();
    }

    /**
     * Defines behavior about how this {@link MagnaTool} should process block drops.
     * <p>
     * This is useful for mechanics such as auto-smelt or removing stacks that shouldn't be dropped while using a certain tool.
     *
     * @param world      world the stack is being dropped in
     * @param player     player that caused the stack to drop
     * @param pos        position of the block dropping the stack
     * @param heldStack  {@link MagnaTool} currently being held by the player
     * @return           a {@link BlockProcessor} that defines information about how this tool should process dropped items
     */
    default BlockProcessor getProcessor(World world, PlayerEntity player, BlockPos pos, ItemStack heldStack) {
        return (tool, input) -> input;
    }

    /**
     * Returns whether the block at the given position in the given world is a valid breaking target for this tool.
     * <p>
     * Whether a block is valid for breaking is a rough definition of effectiveness mixed with speed and tool requirements.
     *
     * @param view   world to look in
     * @param pos    position to look at
     * @param stack  stack to try to break with
     * @return       whether the stack is roughly effective on the given location
     */
    default boolean isBlockValidForBreaking(BlockView view, BlockPos pos, ItemStack stack) {
        BlockState blockState = view.getBlockState(pos);

        if (blockState.getHardness(view, pos) == -1.0) {
            return false;
        }

        if (stack.isEffectiveOn(blockState)) {
            return true;
        }

        if (blockState.isToolRequired()) {
            return false;
        }
    
        return stack.getMiningSpeedMultiplier(blockState) > 1.0F;
    }

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
    default boolean attemptBreak(World world, BlockPos pos, PlayerEntity player, int breakRadius, BlockProcessor processor) {
        if (Magna.CONFIG.breakSingleBlockWhenSneaking && player.isSneaking()) {
            return false;
        }

        // calculate initial hardness & get current breaking stack
        ItemStack mainHandStack = player.getMainHandStack();

        // only do a 3x3 break if the player's tool is effective on the block they are breaking
        // this makes it so breaking gravel doesn't break nearby stone
        if (isBlockValidForBreaking(world, pos, mainHandStack)) {
            int radius = ToolRadiusCallback.EVENT.invoker().getRadius(mainHandStack, breakRadius);
            int depth = getDepth(mainHandStack);

            // break blocks
            BlockBreaker.breakInRadius(world, player, radius, depth, (view, breakPos) -> isBlockValidForBreaking(view, breakPos, mainHandStack), processor, true);
            return true;
        }

        return false;
    }
}
