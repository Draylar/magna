package draylar.magna.mixin;

import draylar.magna.Magna;
import draylar.magna.api.BlockBreaker;
import draylar.magna.api.MagnaTool;
import draylar.magna.api.event.ToolRadiusCallback;
import draylar.magna.config.MagnaConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private double lastCameraX;
    @Shadow private double lastCameraY;
    @Shadow private double lastCameraZ;

    @Shadow private static void drawShapeOutline(MatrixStack matrixStack,
                                                 VertexConsumer vertexConsumer,
                                                 VoxelShape voxelShape,
                                                 double d,
                                                 double e,
                                                 double f,
                                                 float g,
                                                 float h,
                                                 float i,
                                                 float j) { }

    @Shadow private ClientWorld world;

    @Inject(at = @At("HEAD"), method = "drawBlockOutline", cancellable = true)
    private void drawBlockOutline(MatrixStack stack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        MagnaConfig config = Magna.CONFIG;

        // ensure player is not null
        if(this.client.player == null) {
            return;
        }

        // ensure world is not null
        if(this.client.world == null) {
            return;
        }

        // show extended outline if the player is holding a magna tool
        ItemStack heldStack = this.client.player.inventory.getMainHandStack();
        if (heldStack.getItem() instanceof MagnaTool && config.enableExtendedHitbox) {

            // do not show extended outline if player is sneaking and the config option is enabled
            if (!config.disableExtendedHitboxWhileSneaking || !client.player.isSneaking()) {

                // only show extended outline for block raytraces
                if (client.crosshairTarget instanceof BlockHitResult) {
                    BlockHitResult crosshairTarget = (BlockHitResult) client.crosshairTarget;
                    BlockPos crosshairPos = crosshairTarget.getBlockPos();
                    BlockState crosshairState = client.world.getBlockState(crosshairPos);

                    // ensure we are not looking at air or an invalid block
                    if (!crosshairState.isAir() && client.world.getWorldBorder().contains(crosshairPos)) {
                        int radius = ToolRadiusCallback.EVENT.invoker().getRadius(heldStack, ((MagnaTool) heldStack.getItem()).getRadius(heldStack));
                        List<BlockPos> positions = BlockBreaker.findPositions(world, client.player, radius);
                        List<VoxelShape> outlineShapes = new ArrayList<>();
                        outlineShapes.add(VoxelShapes.empty());

                        // assemble outline shape
                        for (BlockPos position : positions) {
                            BlockPos diffPos = position.subtract(crosshairPos);
                            BlockState offsetShape = world.getBlockState(position);

                            // if enableFull3x3 is 'true', all blocks will gain an outline, even if they are air
                            if (!offsetShape.isAir() || config.highlightAirBlocks) {
                                // if fullBlockHitbox is 'true', all blocks will have a 16x16x16 hitbox regardless of their outline shape
                                if (!config.fullBlockShapes) {
                                    if (!config.individualBlockOutlines) {
                                        outlineShapes.set(0, VoxelShapes.union(outlineShapes.get(0), offsetShape.getOutlineShape(world, position).offset(diffPos.getX(), diffPos.getY(), diffPos.getZ())));
                                    } else {
                                        outlineShapes.add(offsetShape.getOutlineShape(world, position).offset(diffPos.getX(), diffPos.getY(), diffPos.getZ()));
                                    }
                                } else {
                                    if (!config.individualBlockOutlines) {
                                        outlineShapes.set(0, VoxelShapes.union(outlineShapes.get(0), VoxelShapes.fullCube().offset(diffPos.getX(), diffPos.getY(), diffPos.getZ())));
                                    } else {
                                        outlineShapes.add(VoxelShapes.fullCube().offset(diffPos.getX(), diffPos.getY(), diffPos.getZ()));
                                    }
                                }
                            }
                        }

                        outlineShapes.forEach(shape -> {
                            // draw extended hitbox
                            drawShapeOutline(
                                    stack,
                                    vertexConsumer,
                                    shape,
                                    (double) crosshairPos.getX() - lastCameraX,
                                    (double) crosshairPos.getY() - lastCameraY,
                                    (double) crosshairPos.getZ() - lastCameraZ,
                                    0.0F,
                                    0.0F,
                                    0.0F,
                                    0.4F);
                        });

                        // cancel 1x1 hitbox that would normally render
                        ci.cancel();
                    }
                }
            }
        }
    }
}