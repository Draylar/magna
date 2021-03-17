package draylar.magna.api;

import draylar.magna.Magna;
import draylar.magna.api.reach.ReachDistanceHelper;
import draylar.magna.impl.MagnaPlayerInteractionManagerExtension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockBreaker {

    /**
     * Breaks blocks within the given radius in the direction the {@link PlayerEntity} is facing.
     * <p>
     * Example: If the {@link PlayerEntity} is facing in the X axis direction and the radius is 1, a 3x3 area will be destroyed on the X axis.
     *
     * @param world           world to break blocks in
     * @param player          the player using the tool to break the blocks
     * @param radius          radius to break blocks in
     * @param breakValidator  predicate to see if a block can be broken
     * @param damageTool      whether or not the tool being used should be damaged
     */
    public static void breakInRadius(World world, PlayerEntity player, int radius, BreakValidator breakValidator, BlockProcessor smelter, boolean damageTool) {
        breakInRadius(world, player, radius, 0, breakValidator, smelter, damageTool);
    }

    /**
     * Breaks blocks within the given radius in the direction the {@link PlayerEntity} is facing.
     * <p>
     * Example: If the {@link PlayerEntity} is facing in the X axis direction and the radius is 1, a 3x3 area will be destroyed on the X axis.
     *
     * @param world           world to break blocks in
     * @param player          the player using the tool to break the blocks
     * @param radius          radius to break blocks in
     * @param depth           depth to break blocks in
     * @param breakValidator  predicate to see if a block can be broken
     * @param damageTool      whether or not the tool being used should be damaged
     */
    public static void breakInRadius(World world, PlayerEntity player, int radius, int depth, BreakValidator breakValidator, BlockProcessor smelter, boolean damageTool) {
        if(!world.isClient) {
            // Flag ServerPlayerInteractionManager as saying we are now breaking in Hammer context.
            // See the large block of comments down below for a more in-depth explanation.
            ServerPlayerInteractionManager interactionManager = ((ServerPlayerEntity) player).interactionManager;
            ((MagnaPlayerInteractionManagerExtension) (interactionManager)).magna_setMining(true);

            // collect all potential blocks to break and attempt to break them
            List<BlockPos> brokenBlocks = findPositions(world, player, radius, depth);
            for(BlockPos pos : brokenBlocks) {
                BlockState state = world.getBlockState(pos);
                BlockEntity blockEntity = world.getBlockState(pos).getBlock().hasBlockEntity() ? world.getBlockEntity(pos) : null;

                // ensure the tool or mechanic can break the given state
                if(breakValidator.canBreak(world, pos) && !state.isAir()) {
                    state.getBlock().onBreak(world, pos, state, player);
                    if (!interactionManager.tryBreakBlock(pos)) {
                        continue;
                    }

                    // The following check is wacky. To start, a Hammer breaking a block is redirected
                    //   into a 3x3 break by ServerPlayerInteractionManagerMixin. At the same time,
                    //   we want to ensure all 3x3 are valid breaks (for things like callbacks or claim protection),
                    //   so we again check tryBreakBlock for validity. To avoid recursively breaking blocks,
                    //   and to cancel the actual block broken logic / item drops, ServerPlayerInteractionManagerMixin
                    //   checks a flag set at the top of this method.
                    // In other words: if this part is reached, only the first 50% of tryBreakBlock was called, and it is a valid break.
                    boolean bl = world.removeBlock(pos, false);
                    if (bl) {
                        state.getBlock().onBroken(world, pos, state);
                    }

                    // only drop items in creative
                    if(!player.isCreative()) {
                        Vec3d offsetPos = new Vec3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);

                        // obtain dropped stacks for the given block
                        List<ItemStack> droppedStacks = Block.getDroppedStacks(state, (ServerWorld) world, pos, blockEntity, player, player.getMainHandStack());
                        List<ItemStack> processed = new ArrayList<>();

                        // attempt to process stack for mechanics like autosmelt
                        droppedStacks.forEach(stack -> processed.add(smelter.process(player.inventory.getMainHandStack(), stack)));

                        // drop items
                        dropItems(player, world, processed, offsetPos);
                        state.onStacksDropped((ServerWorld) world, pos, player.getMainHandStack());
    
                        if (damageTool) {
                            ItemStack itemStack = player.getMainHandStack();
                            boolean usingEffectiveTool = player.isUsingEffectiveTool(state);
                            itemStack.postMine(world, state, pos, player);
                            if (usingEffectiveTool) {
                                player.incrementStat(Stats.MINED.getOrCreateStat(state.getBlock()));
                                player.addExhaustion(0.005F);
                            }
                        }
                    }
                }
            }
            ((MagnaPlayerInteractionManagerExtension) (interactionManager)).magna_setMining(false);
        }
    }

    /**
     * Drops each {@link ItemStack} from the given {@link List} into the given {@link World} at the given {@link BlockPos}.
     *
     * @param world   world to drop items in
     * @param stacks  list of {@link ItemStack}s to drop in the world
     * @param pos     position to drop items at
     */
    private static void dropItems(PlayerEntity player, World world, List<ItemStack> stacks, Vec3d pos) {
        for(ItemStack stack : stacks) {
            if (Magna.CONFIG.autoPickup) {
                player.inventory.insertStack(stack);
            }

            // The stack passed in to insertStack is mutated, so we can operate on it here without worrying about duplicated items.
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                world.spawnEntity(itemEntity);
            }
        }
    }

    /**
     * Returns positions with a depth of 0.
     * @see BlockBreaker#findPositions(World, PlayerEntity, int, int)
     */
    public static List<BlockPos> findPositions(World world, PlayerEntity playerEntity, int radius) {
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
    public static List<BlockPos> findPositions(World world, PlayerEntity playerEntity, int radius, int depth) {
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
            ArrayList<BlockPos> positions = new ArrayList<>();

            // create a box using the given radius
            for(int x = -radius; x <= radius; x++) {
                for(int y = -radius; y <= radius; y++) {
                    for(int z = -radius; z <= radius; z++) {
                        positions.add(new BlockPos(x, y, z));
                    }
                }
            }

            BlockPos origin = blockHitResult.getBlockPos();

            ItemStack handStack = playerEntity.getStackInHand(Hand.MAIN_HAND);
            Item item = handStack.getItem();
            if (item instanceof MagnaTool) {
                origin = ((MagnaTool) item).getCenterPosition(world, playerEntity, origin, handStack);
            }

            // check if each position inside the box is valid
            for(BlockPos pos : positions) {
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
                        potentialBrokenBlocks.add(origin.add(pos).add(blockHitResult.getSide().getOpposite().getVector()));
                    }
                }
            }
        }

        return potentialBrokenBlocks;
    }

    private BlockBreaker() {
        // NO-OP
    }
}
