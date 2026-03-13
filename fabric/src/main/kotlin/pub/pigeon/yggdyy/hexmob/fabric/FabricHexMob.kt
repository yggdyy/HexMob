package pub.pigeon.yggdyy.hexmob.fabric

import pub.pigeon.yggdyy.hexmob.HexMob
import net.fabricmc.api.ModInitializer

object FabricHexMob : ModInitializer {
    override fun onInitialize() {
        HexMob.init()
    }
}
