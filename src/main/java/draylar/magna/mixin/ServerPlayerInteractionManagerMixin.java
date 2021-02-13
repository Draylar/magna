package draylar.magna.mixin;

import draylar.magna.api.MagnaTool;
import draylar.magna.impl.MagnaPlayerInteractionManagerExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// This class has a lower priority than default so it runs after claim mods/FAPI checks for block breaking validity.
@Mixin(value = ServerPlayerInteractionManager.class, priority = 1001)
public class ServerPlayerInteractionManagerMixin implements MagnaPlayerInteractionManagerExtension {

    @Shadow public ServerPlayerEntity player;
    @Shadow public ServerWorld world;
    @Unique private boolean magna_isMining = false;

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
            // This is to avoid recursion, but the goal is to make sure every block it doesn't override cancelled block breaks using Fabric's callbacks. This was made to support claim mods.
            boolean v = magna_isMining || ((MagnaTool) heldStack.getItem()).attemptBreak(world, pos, player, ((MagnaTool) heldStack.getItem()).getRadius(heldStack), ((MagnaTool) heldStack.getItem()).getProcessor(world, player, pos, heldStack));

            // only cancel if the break was successful (false is returned if the player is sneaking)
            if(v) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public boolean magna_isMining() {
        return this.magna_isMining;
    }

    @Override
    public void magna_setMining(boolean mining) {
        this.magna_isMining = mining;
    }
}
