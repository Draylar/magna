package draylar.magna.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Provides information on whether a {@link BlockState} can be broken.
 */
@FunctionalInterface
public interface BreakValidator {

    /**
     * Implementers should return whether the given {@link BlockState} can be broken.
     *
     * @param view  {@link BlockView} of the {@link BlockState}
     * @param pos  {@link BlockPos} of the {@link BlockState}
     * @return  whether the given {@link BlockState} can be broken
     */
    boolean canBreak(BlockView view, BlockPos pos);
}