package pub.pigeon.yggdyy.hexmob.forge

import pub.pigeon.yggdyy.hexmob.HexMob
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent

object ForgeHexMobServer {
    @Suppress("UNUSED_PARAMETER")
    fun init(event: FMLDedicatedServerSetupEvent) {
        HexMob.initServer()
    }
}
