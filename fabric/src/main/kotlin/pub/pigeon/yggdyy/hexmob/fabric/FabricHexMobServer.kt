package pub.pigeon.yggdyy.hexmob.fabric

import pub.pigeon.yggdyy.hexmob.HexMob
import net.fabricmc.api.DedicatedServerModInitializer

object FabricHexMobServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        HexMob.initServer()
    }
}
