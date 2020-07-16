package draylar.magna.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;

public interface ToolRadiusCallback {

    Event<ToolRadiusCallback> EVENT = EventFactory.createArrayBacked(ToolRadiusCallback.class,
            listeners -> (tool, currentRadius) -> {
                for (ToolRadiusCallback callback : listeners) {
                    currentRadius = callback.getRadius(tool, currentRadius);
                }

                return currentRadius;
            });

    int getRadius(ItemStack tool, int currentRadius);
}
