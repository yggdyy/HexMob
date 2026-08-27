package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.world.item.SpawnEggItem
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleCoreItem

object HexMobItems {
    fun init() {
        ITEMS.register()
    }

    private const val WHITE: Int = 0xFF_FFFFFF.toInt()
    private const val AMETHYST_PURPLE: Int = 0xFF_7B2FBE.toInt()
    private const val ALLAY_CYAN: Int = 0xFF_7AC4E8.toInt()
    // 守卫怪蛋配色（可再调）：弓箭守卫=蓝灰，斧头守卫=棕，傀儡守卫=铁灰
    private const val GUARD_ARCHER_BASE: Int = 0xFF_5B6B7C.toInt()
    private const val GUARD_ARCHER_SPOT: Int = 0xFF_222B36.toInt()
    private const val GUARD_BRUTE_BASE: Int = 0xFF_7A4E2E.toInt()
    private const val GUARD_BRUTE_SPOT: Int = 0xFF_35200F.toInt()
    private const val GUARD_GOLEM_BASE: Int = 0xFF_9A9A9E.toInt()
    private const val GUARD_GOLEM_SPOT: Int = 0xFF_515156.toInt()

    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(HexMob.MODID, Registries.ITEM)

    // Note: HexMobEntities.init() must run BEFORE HexMobItems.init() so these
    // factories can resolve the EntityTypes when they are built.
    val IOTA_SHEEP_SPAWN_EGG: DeferredSupplier<SpawnEggItem> = ITEMS.register("iota_sheep_spawn_egg") {
        SpawnEggItem(
            HexMobEntities.IOTA_SHEEP.get(),
            WHITE,            // wool base
            AMETHYST_PURPLE,  // hex/amethyst spots
            Item.Properties(),
        )
    }
    val QUENCH_ALLAY_SPAWN_EGG: DeferredSupplier<SpawnEggItem> = ITEMS.register("quench_allay_spawn_egg") {
        SpawnEggItem(
            HexMobEntities.QUENCH_ALLAY.get(),
            ALLAY_CYAN,       // allay cyan base
            WHITE,            // bright spots
            Item.Properties(),
        )
    }
    val GUARD_ARCHER_SPAWN_EGG: DeferredSupplier<SpawnEggItem> = ITEMS.register("guard_archer_spawn_egg") {
        SpawnEggItem(
            HexMobEntities.GUARD_ARCHER.get(),
            GUARD_ARCHER_BASE,
            GUARD_ARCHER_SPOT,
            Item.Properties(),
        )
    }
    val GUARD_BRUTE_SPAWN_EGG: DeferredSupplier<SpawnEggItem> = ITEMS.register("guard_brute_spawn_egg") {
        SpawnEggItem(
            HexMobEntities.GUARD_BRUTE.get(),
            GUARD_BRUTE_BASE,
            GUARD_BRUTE_SPOT,
            Item.Properties(),
        )
    }
    val GUARD_GOLEM_SPAWN_EGG: DeferredSupplier<SpawnEggItem> = ITEMS.register("guard_golem_spawn_egg") {
        SpawnEggItem(
            HexMobEntities.GUARD_GOLEM.get(),
            GUARD_GOLEM_BASE,
            GUARD_GOLEM_SPOT,
            Item.Properties(),
        )
    }
    /** 大环核心：Boss 战利品（备用物品类，见 UrCircleCoreItem）。 */
    val UR_CIRCLE_CORE: DeferredSupplier<UrCircleCoreItem> = ITEMS.register("ur_circle_core") {
        UrCircleCoreItem(Item.Properties())
    }
}
