package draylar.magna.api.optional;

import draylar.magna.api.event.ToolMiningSpeedMultiplierCallback;
import draylar.magna.api.event.ToolRadiusCallback;
import draylar.magna.enchantment.CurseOfGigantismEnchantment;
import draylar.magna.item.ExcavatorItem;
import draylar.magna.item.HammerItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class MagnaOptionals {

    public static Enchantment CURSE_OF_GIGANTISM;
    private static final List<StackPredicate> validForCurseOfGigantism = new ArrayList<>();

    /**
     * "Opts-in" for the Curse of Gigantism, which registers the enchantment if it has not already been registered.
     * <p>
     * Mods that want this mechanic should opt in. Vanilla Hammers and Vanilla Excavators
     * will always opt-in, so you can do it yourself, or rely on them being in the same environment.
     * <p>
     * The Curse of Gigantism works on all {@link draylar.magna.item.ExcavatorItem} and {@link draylar.magna.item.HammerItem} tools,
     * and will increase the radius of the tool by 1 at the cost of 80% speed.
     */
    public static void optInForCurse() {
        if(CURSE_OF_GIGANTISM == null) {
            CURSE_OF_GIGANTISM = Registry.register(
                    Registry.ENCHANTMENT,
                    new Identifier("magna", "gigantism_curse"),
                    new CurseOfGigantismEnchantment()
            );
        }

        // setup 80% speed decrease while using the curse
        ToolMiningSpeedMultiplierCallback.EVENT.register((tool, state, player, currentMultiplier) -> {
            if(EnchantmentHelper.getLevel(MagnaOptionals.CURSE_OF_GIGANTISM, tool) > 0) {
                return currentMultiplier * 0.2f;
            }

            return currentMultiplier;
        });

        // setup radius increase while the curse is applied to a tool
        ToolRadiusCallback.EVENT.register((tool, currentRadius) -> {
            if(EnchantmentHelper.getLevel(MagnaOptionals.CURSE_OF_GIGANTISM, tool) > 0) {
                return currentRadius + 1;
            }

            return currentRadius;
        });

        // register base tools as valid for curse
        registerCurseOfGigantismTool(stack -> {
            return stack.getItem() instanceof ExcavatorItem || stack.getItem() instanceof HammerItem;
        });
    }

    /**
     * Registers a {@link StackPredicate} used to determine whether an {@link net.minecraft.item.ItemStack} is valid for the Curse of Gigantism.
     *
     * @param predicate  predicate that determines whether an {@link net.minecraft.item.ItemStack} is valid for the Curse of Gigantism
     */
    public static void registerCurseOfGigantismTool(StackPredicate predicate) {
        validForCurseOfGigantism.add(predicate);
    }

    public static List<StackPredicate> getValidForCurseOfGigantism() {
        return validForCurseOfGigantism;
    }

    private MagnaOptionals() {
        // NO-OP private constructor
    }
}
