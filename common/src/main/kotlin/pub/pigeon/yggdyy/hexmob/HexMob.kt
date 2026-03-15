package pub.pigeon.yggdyy.hexmob

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedSlateBlock
import pub.pigeon.yggdyy.hexmob.networking.HexMobNetworking
import pub.pigeon.yggdyy.hexmob.registry.*

object HexMob {
    const val MODID = "hexmob"
    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MODID)
    @JvmStatic
    fun id(path: String) = ResourceLocation(MODID, path)
    fun init() {
        if(LOGGER.isDebugEnabled) LOGGER.warn("Common Init")
        HexMobServerConfig.init()
        initRegistries(
            HexMobActions
        )
        HexMobItems.init()
        HexMobBlocks.init()
        HexMobEntities.init()
        HexMobNetworking.init()
        HexMobEntityAttributes.init()
        CastingEnvironment.addCreateEventListener{env, data -> StimulatedSlateBlock.applyMediaDiscount(env, data)}
    }
    fun initServer() {
        HexMobServerConfig.initServer()
    }
}
