package pub.pigeon.yggdyy.hexmob

import pub.pigeon.yggdyy.hexmob.config.HexMobClientConfig
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.gui.screens.Screen

object HexMobClient {
    fun init() {
        HexMobClientConfig.init()
    }

    fun getConfigScreen(parent: Screen): Screen {
        return AutoConfig.getConfigScreen(HexMobClientConfig.GlobalConfig::class.java, parent).get()
    }
}
