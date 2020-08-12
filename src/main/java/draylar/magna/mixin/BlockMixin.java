package draylar.magna.mixin;

import draylar.magna.api.BlockProcessor;
import draylar.magna.api.MagnaTool;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Shadow public static void dropStack(World world, BlockPos pos, ItemStack stack) { }
    @Shadow public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack) { return null; }

    /**
     * Covers an edge-case where shifting and breaking a block with a tool wouldn't apply processors.
     *
     * @param state        state of the block that was broken
     * @param world        world the break occurred in
     * @param pos          position the broke occurred at
     * @param blockEntity  blockEntity being broken, if any
     * @param entity       entity causing the drops, if any
     * @param tool         tool used to break the block
     * @param ci           mixin callback info
     */
    @Inject(
            method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void smeltItems(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        if(!world.isClient && tool.getItem() instanceof MagnaTool && entity instanceof PlayerEntity) {
            MagnaTool magnaItem = (MagnaTool) tool.getItem();
            BlockProcessor processor = magnaItem.getProcessor(world, (PlayerEntity) entity, pos, tool);

            // drop each stack, smelted
            getDroppedStacks(state, (ServerWorld)world, pos, blockEntity, entity, tool).forEach((drop) -> {
                dropStack(world, pos, processor.process(tool, drop));
            });

            // cancel
            state.onStacksDropped((ServerWorld) world, pos, tool);
            ci.cancel();
        }
    }
}
