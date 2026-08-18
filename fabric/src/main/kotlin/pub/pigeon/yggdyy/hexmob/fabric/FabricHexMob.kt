package pub.pigeon.yggdyy.hexmob.fabric

import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.registry.HexMobActions
import net.fabricmc.api.ModInitializer

object FabricHexMob : ModInitializer {
    override fun onInitialize() {
        HexMob.init()
        initRegistry(HexMobActions)
    }
}
