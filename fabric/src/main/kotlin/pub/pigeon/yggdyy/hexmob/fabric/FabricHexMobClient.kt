package pub.pigeon.yggdyy.hexmob.fabric

import pub.pigeon.yggdyy.hexmob.HexMobClient
import net.fabricmc.api.ClientModInitializer

object FabricHexMobClient : ClientModInitializer {
    override fun onInitializeClient() {
        HexMobClient.init()
    }
}
