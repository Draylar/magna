package dev.draylar.magna.mixin;

import dev.draylar.magna.api.MagnaTool;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * A mixin that calculates the block breaking delta for {@link MagnaTool MagnaTool}s.
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    /**
     * Compute the block breaking delta for {@link MagnaTool}s. If the held item is not a MagnaTool, this mixin
     * does nothing.
     *
     * <p>The delta is computed by taking the minimum delta of the affected blocks. Mining speed is effectively limited
     * by the hardest block being broken.
     */
    @Inject(
            method = "calcBlockBreakingDelta",
            at = @At("HEAD"),
            cancellable = true
    )
    public void calcBlockBreakingDelta(PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = player.getInventory().getMainHandStack();
        if (stack.getItem() instanceof MagnaTool tool) {
            cir.cancel();
            int radius = tool.getRadius(stack);
            cir.setReturnValue(calculateDelta(tool, player, radius));
        }
    }

    @Unique
    private static float calculateDelta(final MagnaTool tool, final PlayerEntity player, final int radius) {
        World world = player.getWorld();

        // Even though we already have the BlockPosition that the player is targeting, the side information wasn't
        // passed along, which is necessary for figuring out how to expand the radius. Just throw that away and
        // recompute.
        List<BlockPos> blocks = tool.getBlockFinder().findPositions(world, player, radius);

        return blocks.stream()
                .map(pos -> {
                    BlockState state = world.getBlockState(pos);
                    //noinspection deprecation
                    return state.getBlock().calcBlockBreakingDelta(state, player, world, pos);
                })
                .min(Float::compare).orElseThrow();
    }
}
