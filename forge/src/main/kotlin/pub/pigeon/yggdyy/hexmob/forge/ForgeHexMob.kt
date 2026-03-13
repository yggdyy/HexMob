package pub.pigeon.yggdyy.hexmob.forge

import dev.architectury.platform.forge.EventBuses
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.forge.datagen.ForgeHexMobDatagen
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(HexMob.MODID)
class ForgeHexMob {
    init {
        MOD_BUS.apply {
            EventBuses.registerModEventBus(HexMob.MODID, this)
            addListener(ForgeHexMobClient::init)
            addListener(ForgeHexMobDatagen::init)
            addListener(ForgeHexMobServer::init)
        }
        HexMob.init()
    }
}
