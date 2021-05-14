package draylar.magna;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import draylar.magna.api.MagnaTool;
import draylar.magna.item.ExcavatorItem;
import draylar.magna.item.HammerItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * This class is responsible for registering development-environment test items.
 * The registration logic is isolated to prevent import errors in production.
 */
public class MagnaTest {

    public static void initialize() {
        // Standard Hammer with a tool material of Diamond.
        Registry.register(
                Registry.ITEM,
                new Identifier("magna", "hammer_test"),
                new HammerItem(ToolMaterials.DIAMOND, 0, 0, new Item.Settings())
        );

        // Standard Hammer with a tool material of Diamond and a modified depth.
        Registry.register(
                Registry.ITEM,
                new Identifier("magna", "depth_test"),
                new HammerItem(ToolMaterials.DIAMOND, 0, 0, new Item.Settings()) {

                    @Override
                    public int getDepth(ItemStack stack) {
                        return 5;
                    }
                }
        );

        // Standard Excavator with a tool material of Wood.
        Registry.register(
                Registry.ITEM,
                new Identifier("magna", "excavator_test"),
                new ExcavatorItem(ToolMaterials.WOOD, 0, 0, new Item.Settings())
        );

        // Hammer with extended reach.
        Registry.register(
                Registry.ITEM,
                new Identifier("magna", "reach_test"),
                new HammerItem(ToolMaterials.WOOD, 0, 0, new Item.Settings()) {
                    @Override
                    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
                        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(ReachEntityAttributes.REACH, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", 10, EntityAttributeModifier.Operation.ADDITION));
                        return builder.build();
                    }
                }
        );

        // Hammer with a huge radius and no outline.
        Registry.register(
                Registry.ITEM,
                new Identifier("magna", "outline_test"),
                new HammerItem(ToolMaterials.WOOD, 0, 0, new Item.Settings()) {
                    @Override
                    public boolean renderOutline(World world, BlockHitResult ray, PlayerEntity player, ItemStack stack) {
                        return false;
                    }

                    @Override
                    public int getRadius(ItemStack stack) {
                        return 15;
                    }

                    @Override
                    public int getDepth(ItemStack stack) {
                        return 15;
                    }
                }
        );
    }

    private MagnaTest() {
        // NO-OP
    }
}
