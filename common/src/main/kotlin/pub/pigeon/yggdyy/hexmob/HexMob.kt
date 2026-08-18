package pub.pigeon.yggdyy.hexmob

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import pub.pigeon.yggdyy.hexmob.content.iota_sheep.IotaSheepDefaultBehaviors
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
        // Hex actions are registered per-platform in each platform's entrypoint
        // (the common @ExpectPlatform path was not being transformed at runtime).
        // Entities must come before items: the spawn egg's factory resolves
        // HexMobEntities.IOTA_SHEEP.get() at registration time.
        HexMobEntities.init()
        HexMobItems.init()
        HexMobBlocks.init()
        HexMobNetworking.init()
        HexMobEntityAttributes.init()
        HexMobStructurePieceTypes.init()
        HexMobCreativeTab.init()
        HexMobEntitySpawns.init()
        IotaSheepDefaultBehaviors.init()
        CastingEnvironment.addCreateEventListener{env, data -> StimulatedSlateBlock.applyMediaDiscount(env, data)}
    }
    fun initServer() {
        HexMobServerConfig.initServer()
    }
}
