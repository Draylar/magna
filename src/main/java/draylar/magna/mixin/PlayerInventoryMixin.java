package draylar.magna.mixin;

import draylar.magna.api.event.ToolMiningSpeedMultiplierCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow @Final public PlayerEntity player;
    @Shadow @Final public DefaultedList<ItemStack> main;
    @Shadow public int selectedSlot;

    /**
     * Hijacks the tool break speed check to account for the {@link ToolMiningSpeedMultiplierCallback}.
     *
     * @param state  {@link BlockState} being broken
     * @param cir    mixin callback info
     */
    @Inject(
            method = "getBlockBreakingSpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        float value = cir.getReturnValue();
        ItemStack heldStack = this.main.get(this.selectedSlot);
        cir.setReturnValue(ToolMiningSpeedMultiplierCallback.EVENT.invoker().getMultiplier(heldStack, state, player, value));
    }
}
