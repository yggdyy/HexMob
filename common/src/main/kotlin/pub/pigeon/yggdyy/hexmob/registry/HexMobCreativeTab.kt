package pub.pigeon.yggdyy.hexmob.registry

import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.now.EverythingInNowItem

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
                output.accept(HexMobItems.QUENCH_ALLAY_SPAWN_EGG.get())
                output.accept(HexMobItems.GUARD_ARCHER_SPAWN_EGG.get())
                output.accept(HexMobItems.GUARD_BRUTE_SPAWN_EGG.get())
                output.accept(HexMobItems.GUARD_GOLEM_SPAWN_EGG.get())
                // 大环核心
                output.accept(HexMobItems.UR_CIRCLE_CORE.get())
                // 淬灵媒质立方：创造栏预存满媒质（64 淬灵晶块 = 7680 万媒质）
                output.accept(
                    ItemMediaHolder.withMedia(
                        ItemStack(HexMobItems.EVERYTHING_IN_NOW.get()),
                        EverythingInNowItem.MAX_MEDIA,
                        EverythingInNowItem.MAX_MEDIA,
                    )
                )
                output.accept(HexMobBlocks.STIMULATED_SLATE_ITEM.get())
                output.accept(HexMobBlocks.INFESTED_EDIFIED_PLANKS_ITEM.get())
                output.accept(HexMobBlocks.INFESTED_EDIFIED_PANEL_ITEM.get())
                output.accept(HexMobBlocks.INFESTED_EDIFIED_TILE_ITEM.get())
            }
            .build()
    }
}
