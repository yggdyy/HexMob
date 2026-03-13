package pub.pigeon.yggdyy.hexmob.forge

import pub.pigeon.yggdyy.hexmob.HexMobClient
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

object ForgeHexMobClient {
    @Suppress("UNUSED_PARAMETER")
    fun init(event: FMLClientSetupEvent) {
        HexMobClient.init()
        LOADING_CONTEXT.registerExtensionPoint(ConfigScreenFactory::class.java) {
            ConfigScreenFactory { _, parent -> HexMobClient.getConfigScreen(parent) }
        }
    }
}
