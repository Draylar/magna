package draylar.magna.api.reach;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public class ReachDistanceIsolated {

    public static double getReachDistance(PlayerEntity playerEntity, double base) {
        return ReachEntityAttributes.getReachDistance(playerEntity, base);
    }

    private ReachDistanceIsolated() {
        // NO-OP
    }
}
