package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.world.item.SpawnEggItem
import pub.pigeon.yggdyy.hexmob.HexMob

object HexMobItems {
    fun init() {
        ITEMS.register()
    }

    private const val WHITE: Int = 0xFF_FFFFFF.toInt()
    private const val AMETHYST_PURPLE: Int = 0xFF_7B2FBE.toInt()

    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(HexMob.MODID, Registries.ITEM)

    // Note: HexMobEntities.init() must run BEFORE HexMobItems.init() so this
    // factory can resolve IOTA_SHEEP's EntityType when it is built.
    val IOTA_SHEEP_SPAWN_EGG: DeferredSupplier<SpawnEggItem> = ITEMS.register("iota_sheep_spawn_egg") {
        SpawnEggItem(
            HexMobEntities.IOTA_SHEEP.get(),
            WHITE,            // wool base
            AMETHYST_PURPLE,  // hex/amethyst spots
            Item.Properties(),
        )
    }
}
