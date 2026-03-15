package pub.pigeon.yggdyy.hexmob.forge

import dev.architectury.platform.Platform
import dev.architectury.platform.forge.EventBuses
import pub.pigeon.yggdyy.hexmob.HexMob
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntityRenderers
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(HexMob.MODID)
class ForgeHexMob {
    init {
        MOD_BUS.apply {
            EventBuses.registerModEventBus(HexMob.MODID, this)
            addListener{event: FMLClientSetupEvent -> ForgeHexMobClient.init()}
            addListener{event: FMLDedicatedServerSetupEvent -> ForgeHexMobServer.init()}
        }
        HexMob.init()
        if(Platform.getEnv().isClient) {
            HexMobEntityRenderers.init()
        }
    }
}
