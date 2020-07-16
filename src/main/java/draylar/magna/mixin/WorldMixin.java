package draylar.magna.mixin;

import draylar.magna.api.MagnaTool;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable {

    @Unique private Entity magna_cachedEntity = null;

    @Inject(
            method = "breakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"
            )
    )
    private void storeContext(BlockPos pos, boolean drop, Entity breakingEntity, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        magna_cachedEntity = breakingEntity;
    }

    @Redirect(
            method = "breakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;syncWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V"
            )
    )
    private void cancelSyncWorldEvent(World world, int eventId, BlockPos pos, int data) {
        BlockState blockState = this.getBlockState(pos);

        // ensure cachedEntity is valid
        if(magna_cachedEntity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) magna_cachedEntity;
            ItemStack heldStack = player.getMainHandStack();

            // cancel particles & sounds if the tool used was a MagnaTool
            if(heldStack.getItem() instanceof MagnaTool && !((MagnaTool) heldStack.getItem()).playBreakEffects()) {
                return;
            }
        }

        this.syncWorldEvent(2001, pos, Block.getRawIdFromState(blockState));
    }
}
