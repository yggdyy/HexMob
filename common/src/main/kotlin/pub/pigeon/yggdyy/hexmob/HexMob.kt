package pub.pigeon.yggdyy.hexmob

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import pub.pigeon.yggdyy.hexmob.content.crystal_spikes.HexMobFeatures
import pub.pigeon.yggdyy.hexmob.content.iota_sheep.IotaSheepDefaultBehaviors
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedSlateBlock
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.HexMobBacklash
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
        HexMobCommands.init()
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
        HexMobFeatures.init()
        HexMobCreativeTab.init()
        HexMobEntitySpawns.init()
        IotaSheepDefaultBehaviors.init()
        CastingEnvironment.addCreateEventListener{env, data -> StimulatedSlateBlock.applyMediaDiscount(env, data)}
        // 反向过度施法：玩家每施法一次给附近大环积累反噬值（第 5 步）
        CastingEnvironment.addCreateEventListener{env, _ -> HexMobBacklash.onCast(env)}
    }
    fun initServer() {
        HexMobServerConfig.initServer()
    }
}
