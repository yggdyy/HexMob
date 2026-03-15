package pub.pigeon.yggdyy.hexmob.registry

import at.petrak.hexcasting.common.items.storage.ItemSlate
import dev.architectury.registry.item.ItemPropertiesRegistry

object HexMobItemProperties {
    fun init() {
        ItemPropertiesRegistry.register(HexMobBlocks.STIMULATED_SLATE_ITEM.get(), ItemSlate.WRITTEN_PRED
        ) { stack, _, _, _ ->
            if (ItemSlate.hasPattern(stack)) 1f else 0f
        }
    }
}