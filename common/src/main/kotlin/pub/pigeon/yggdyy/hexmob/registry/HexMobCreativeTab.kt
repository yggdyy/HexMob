package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import pub.pigeon.yggdyy.hexmob.HexMob

object HexMobCreativeTab {
    fun init() {
        TABS.register()
    }

    private val TABS: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(HexMob.MODID, Registries.CREATIVE_MODE_TAB)

    val MAIN: DeferredSupplier<CreativeModeTab> = TABS.register("main") {
        CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.hexmob"))
            .icon { ItemStack(HexMobItems.IOTA_SHEEP_SPAWN_EGG.get()) }
            .displayItems { _, output ->
                output.accept(HexMobItems.IOTA_SHEEP_SPAWN_EGG.get())
                output.accept(HexMobBlocks.STIMULATED_SLATE_ITEM.get())
                output.accept(HexMobBlocks.INFESTED_EDIFIED_PLANKS_ITEM.get())
                output.accept(HexMobBlocks.INFESTED_EDIFIED_PANEL_ITEM.get())
                output.accept(HexMobBlocks.INFESTED_EDIFIED_TILE_ITEM.get())
            }
            .build()
    }
}
