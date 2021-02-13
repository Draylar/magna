package draylar.magna;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import draylar.magna.item.ExcavatorItem;
import draylar.magna.item.HammerItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * This class is responsible for registering development-environment test items.
 * The registration logic is isolated to prevent import errors in production.
 */
public class MagnaTest {

    public static void initialize() {
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
    }
}
