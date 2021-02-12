package draylar.magna;

import draylar.magna.config.MagnaConfig;
import draylar.magna.item.ExcavatorItem;
import draylar.magna.item.HammerItem;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Magna implements ModInitializer {

    public static MagnaConfig CONFIG = AutoConfig.register(MagnaConfig.class, GsonConfigSerializer::new).getConfig();

    @Override
    public void onInitialize() {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Registry.register(
                    Registry.ITEM,
                    new Identifier("magna", "test"),
                    new HammerItem(ToolMaterials.DIAMOND, 0, 0, new Item.Settings())
            );

            Registry.register(
                    Registry.ITEM,
                    new Identifier("magna", "test2"),
                    new ExcavatorItem(ToolMaterials.WOOD, 0, 0, new Item.Settings())
            );
        }
    }

    /**
     * Returns an {@link Identifier} under the "magna" namespace.
     *
     * @param name  path of the {@link Identifier} to return
     * @return      {@link Identifier} with namespace of "magna" and path of the given name
     */
    public static Identifier id(String name) {
        return new Identifier("magna", name);
    }

    /**
     * Returns whether or not the mod Vanilla Hammers is installed.
     * <p>
     * Mods implementing their own hammers can do whatever they want with their content,
     * but this also provides the option for them to only add hammers when a
     * hammer-adding mod is installed inside the same environment.
     * <p>
     * For more information on Vanilla Hammers, visit the <a href=https://github.com/Draylar/vanilla-hammers">Vanilla Hammers GitHub repo</a>.
     *
     * @return  whether Vanilla Hammers is installed
     */
    public static boolean isVanillaHammersInstalled() {
        return FabricLoader.getInstance().isModLoaded("vanilla-hammers");
    }

    /**
     * Returns whether or not the mod Vanilla Excavators is installed.
     * <p>
     * Mods implementing their own excavators can do whatever they want with their content,
     * but this also provides the option for them to only add excavators when a
     * excavator-adding mod is installed inside the same environment.
     * <p>
     * For more information on Vanilla Excavators, visit the <a href=https://github.com/Draylar/vanilla-excavators">Vanilla Excavators GitHub repo</a>.
     *
     * @return  whether Vanilla Excavators is installed
     */
    public static boolean isVanillaExcavatorsInstalled() {
        return FabricLoader.getInstance().isModLoaded("vanillaexcavators");
    }
}
