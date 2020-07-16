package draylar.magna.api;

import net.minecraft.item.ItemStack;

public interface BlockProcessor {

    /**
     * Defines behavior for how the given {@link ItemStack} should "process" the input {@link ItemStack}.
     *
     * @param tool   tool being used to process the stack
     * @param input  stack being processed
     * @return       processed stack
     */
    ItemStack process(ItemStack tool, ItemStack input);
}
