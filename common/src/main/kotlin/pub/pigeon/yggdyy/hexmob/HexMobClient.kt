package pub.pigeon.yggdyy.hexmob

import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.gui.screens.Screen
import pub.pigeon.yggdyy.hexmob.config.HexMobClientConfig
import pub.pigeon.yggdyy.hexmob.registry.HexMobBlockEntityRenderers
import pub.pigeon.yggdyy.hexmob.registry.HexMobItemProperties

object HexMobClient {
    fun init() {
        if(HexMob.LOGGER.isDebugEnabled) HexMob.LOGGER.warn("Client Init")
        HexMobClientConfig.init()
        HexMobItemProperties.init()
        HexMobBlockEntityRenderers.init()
    }
    fun getConfigScreen(parent: Screen): Screen {
        return AutoConfig.getConfigScreen(HexMobClientConfig.GlobalConfig::class.java, parent).get()
    }
}
