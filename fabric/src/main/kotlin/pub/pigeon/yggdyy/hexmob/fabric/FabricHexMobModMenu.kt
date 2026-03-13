package pub.pigeon.yggdyy.hexmob.fabric

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import pub.pigeon.yggdyy.hexmob.HexMobClient

object FabricHexMobModMenu : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(HexMobClient::getConfigScreen)
}
