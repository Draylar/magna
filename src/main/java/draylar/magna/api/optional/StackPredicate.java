package draylar.magna.api.optional;

import net.minecraft.item.ItemStack;

public interface StackPredicate {
    boolean isValid(ItemStack stack);
}
