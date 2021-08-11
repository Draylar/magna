package draylar.magna.api;

import draylar.magna.api.reach.ReachDistanceHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public interface BlockFinder {
    BlockFinder DEFAULT = new BlockFinder() {};

    /**
     * Returns positions with a depth of 0.
     * @see BlockFinder#findPositions(World, PlayerEntity, int, int)
     */
    default List<BlockPos> findPositions(World world, PlayerEntity playerEntity, int radius) {
        return findPositions(world, playerEntity, radius, 0);
    }

    /**
     * Returns a list of {@link BlockPos} in the given radius considering the {@link PlayerEntity}'s facing direction.
     *
     * @param world         world to check in
     * @param playerEntity  player that is collecting blocks
     * @param radius        radius to collect blocks in
     * @param depth         the depth away from the player to break
     * @return              a list of blocks that would be broken with the given radius and tool
     */
    default List<BlockPos> findPositions(World world, PlayerEntity playerEntity, int radius, int depth) {
        ArrayList<BlockPos> potentialBrokenBlocks = new ArrayList<>();

        // collect information on camera
        Vec3d cameraPos = playerEntity.getCameraPosVec(1);
        Vec3d rotation = playerEntity.getRotationVec(1);
        double reachDistance = ReachDistanceHelper.getReachDistance(playerEntity);
        Vec3d combined = cameraPos.add(rotation.x * reachDistance, rotation.y * reachDistance, rotation.z * reachDistance);

        // find block the player is currently looking at
        BlockHitResult blockHitResult = world.raycast(new RaycastContext(cameraPos, combined, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, playerEntity));

        // only show an extended hitbox if the player is looking at a block
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            Direction.Axis axis = blockHitResult.getSide().getAxis();
            ArrayList<Vec3i> positions = new ArrayList<>();

            // create a box using the given radius
            for(int x = -radius; x <= radius; x++) {
                for(int y = -radius; y <= radius; y++) {
                    for(int z = -radius; z <= radius; z++) {
                        positions.add(new Vec3i(x, y, z));
                    }
                }
            }

            BlockPos origin = blockHitResult.getBlockPos();

            ItemStack handStack = playerEntity.getStackInHand(Hand.MAIN_HAND);
            Item item = handStack.getItem();
            if (item instanceof MagnaTool) {
                origin = ((MagnaTool) item).getCenterPosition(world, playerEntity, blockHitResult, handStack);
            }

            // check if each position inside the box is valid
            for(Vec3i pos : positions) {
                boolean valid = false;

                if(axis == Direction.Axis.Y) {
                    if(pos.getY() == 0) {
                        potentialBrokenBlocks.add(origin.add(pos));
                        valid = true;
                    }
                }

                else if (axis == Direction.Axis.X) {
                    if(pos.getX() == 0) {
                        potentialBrokenBlocks.add(origin.add(pos));
                        valid = true;
                    }
                }

                else if (axis == Direction.Axis.Z) {
                    if(pos.getZ() == 0) {
                        potentialBrokenBlocks.add(origin.add(pos));
                        valid = true;
                    }
                }

                // Operate on depth by extending the current block away from the player.
                if(valid) {
                    for (int i = 1; i <= depth; i++) {
                        Vec3i vec = blockHitResult.getSide().getOpposite().getVector();
                        potentialBrokenBlocks.add(origin.add(pos).add(vec.getX() * i, vec.getY() * i, vec.getZ() * i));
                    }
                }
            }
        }

        return potentialBrokenBlocks;
    }
}
