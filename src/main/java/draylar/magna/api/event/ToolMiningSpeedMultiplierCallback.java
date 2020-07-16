package draylar.magna.api.event;

import draylar.magna.api.MagnaTool;
import draylar.magna.api.optional.MagnaOptionals;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Used for defining custom mining speed behavior for Magna Tools under certain conditions.
 * <p>
 * The {@link MagnaOptionals#CURSE_OF_GIGANTISM} uses this to slow
 * down relevant tools by 80%. Custom tool classes should opt to use a custom {@link net.minecraft.item.ToolMaterial}
 * to define base speed, and use this callback for advanced behavior.
 */
public interface ToolMiningSpeedMultiplierCallback {

    Event<ToolMiningSpeedMultiplierCallback> EVENT = EventFactory.createArrayBacked(ToolMiningSpeedMultiplierCallback.class,
            listeners -> (tool, state, player, currentMultiplier) -> {
                for (ToolMiningSpeedMultiplierCallback callback : listeners) {
                    currentMultiplier = callback.getMultiplier(tool, state, player, currentMultiplier);
                }

                return currentMultiplier;
            });

    /**
     * Returns the mining speed multiplier for the given tool.
     * <p>
     * The base currentMultiplier value is gotten through {@link ItemStack#getMiningSpeedMultiplier(BlockState)}.
     *
     * @param tool               current {@link ItemStack} being used, with an underlying {@link net.minecraft.item.Item} that extends {@link MagnaTool}
     * @param state              current {@link BlockState} being broken or operated on by the tool
     * @param player             {@link net.minecraft.entity.player.PlayerEntity} using the tool
     * @param currentMultiplier  current speed multiplier, which may have been modified by previous callbacks already
     * @return                   the mining speed of the tool
     */
    float getMultiplier(ItemStack tool, BlockState state, PlayerEntity player, float currentMultiplier);
}

