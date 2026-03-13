package pub.pigeon.yggdyy.hexmob

import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import pub.pigeon.yggdyy.hexmob.networking.HexMobNetworking
import pub.pigeon.yggdyy.hexmob.registry.HexMobActions

object HexMob {
    const val MODID = "hexmob"
    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MODID)
    @JvmStatic
    fun id(path: String) = ResourceLocation(MODID, path)
    fun init() {
        HexMobServerConfig.init()
        initRegistries(
            HexMobActions,
        )
        HexMobNetworking.init()
    }
    fun initServer() {
        HexMobServerConfig.initServer()
    }
}
