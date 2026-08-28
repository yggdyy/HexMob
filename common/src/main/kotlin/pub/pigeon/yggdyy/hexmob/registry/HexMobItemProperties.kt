package pub.pigeon.yggdyy.hexmob.registry

import at.petrak.hexcasting.common.items.storage.ItemSlate
import dev.architectury.registry.client.rendering.ColorHandlerRegistry
import dev.architectury.registry.item.ItemPropertiesRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SpawnEggItem
import pub.pigeon.yggdyy.hexmob.client.HexMobGaslightingTracker

object HexMobItemProperties {
    fun init() {
        ItemPropertiesRegistry.register(HexMobBlocks.STIMULATED_SLATE_ITEM.get(), ItemSlate.WRITTEN_PRED
        ) { stack, _, _, _ ->
            if (ItemSlate.hasPattern(stack)) 1f else 0f
        }

        // 淬灵媒质立方：煤气灯换皮（hexcasting 淬灵晶碎片同款）——
// 相位 = GaslightingTracker 计数 % 3；盯着看不动，不看 40 tick 悄悄换一档
        ItemPropertiesRegistry.register(
            HexMobItems.EVERYTHING_IN_NOW.get(),
            ResourceLocation("hexmob", "phase"),
        ) { _, _, _, _ ->
            Math.abs(HexMobGaslightingTracker.getGaslightingAmount() % 3).toFloat()
        }

        // Spawn eggs are drawn as a tinted vanilla egg shape; without an item
        // color handler the "texture" is invisible. 1.20.1 has no static
        // SpawnEggItem.getColor(stack, tint), so read it off the item instance.
        ColorHandlerRegistry.registerItemColors(
            { stack: ItemStack, tintIndex: Int -> (stack.item as SpawnEggItem).getColor(tintIndex) },
            HexMobItems.IOTA_SHEEP_SPAWN_EGG.get(),
            HexMobItems.QUENCH_ALLAY_SPAWN_EGG.get(),
        )
    }
}
