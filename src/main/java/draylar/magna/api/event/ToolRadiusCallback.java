package draylar.magna.api.event;

import draylar.magna.api.MagnaTool;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;

public interface ToolRadiusCallback {

    Event<ToolRadiusCallback> EVENT = EventFactory.createArrayBacked(ToolRadiusCallback.class,
            listeners -> (tool, currentRadius) -> {
                int radius = ((MagnaTool) tool.getItem()).getRadius(tool);

                for (ToolRadiusCallback callback : listeners) {
                    radius = callback.getRadius(tool, radius);
                }

                return radius;
            });

    int getRadius(ItemStack tool, int currentRadius);
}
