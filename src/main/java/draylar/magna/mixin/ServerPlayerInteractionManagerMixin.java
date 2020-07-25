package draylar.magna.mixin;

import draylar.magna.api.MagnaTool;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public ServerWorld world;

    @Inject(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"
            ),
            cancellable = true
    )
    private void tryBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ItemStack heldStack = player.getMainHandStack();

        if (heldStack.getItem() instanceof MagnaTool) {
            boolean v = ((MagnaTool) heldStack.getItem()).attemptBreak(world, pos, player, ((MagnaTool) heldStack.getItem()).getRadius(heldStack), ((MagnaTool) heldStack.getItem()).getProcessor(world, player, pos, heldStack));

            // only cancel if the break was successful (false is returned if the player is sneaking)
            if(v) {
                cir.setReturnValue(true);
            }
        }
    }
}
