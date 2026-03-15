package pub.pigeon.yggdyy.hexmob.forge

import pub.pigeon.yggdyy.hexmob.HexMobClient
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

object ForgeHexMobClient {
    fun init() {
        HexMobClient.init()
        LOADING_CONTEXT.registerExtensionPoint(ConfigScreenFactory::class.java) {
            ConfigScreenFactory { _, parent -> HexMobClient.getConfigScreen(parent) }
        }
    }
}
