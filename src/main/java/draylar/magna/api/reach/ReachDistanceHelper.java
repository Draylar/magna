package draylar.magna.api.reach;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

public class ReachDistanceHelper {

    public static double getReachDistance(PlayerEntity playerEntity) {
        double base = playerEntity.isCreative() ? 5.0F : 4.5F;

        // This prevents ClassNotFound exceptions with the optional Reach Entity Attributes dependency.
        if(FabricLoader.getInstance().isModLoaded("reach-entity-attributes")) {
            return ReachDistanceIsolated.getReachDistance(playerEntity, base);
        }

        return base;
    }

    private ReachDistanceHelper() {
        // NO-OP
    }
}
