package pub.pigeon.yggdyy.hexmob

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import dev.architectury.event.events.common.TickEvent
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import pub.pigeon.yggdyy.hexmob.content.crystal_spikes.HexMobFeatures
import pub.pigeon.yggdyy.hexmob.content.iota_sheep.IotaSheepDefaultBehaviors
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedSlateBlock
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.HexMobBacklash
import pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell.BeamTicker
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
        HexMobStructures.init()
        HexMobFeatures.init()
        HexMobCreativeTab.init()
        HexMobEntitySpawns.init()
        IotaSheepDefaultBehaviors.init()
        CastingEnvironment.addCreateEventListener{env, data -> StimulatedSlateBlock.applyMediaDiscount(env, data)}
        // 反向过度施法：玩家每施法一次给附近大环积累反噬值（第 5 步）
        CastingEnvironment.addCreateEventListener{env, _ -> HexMobBacklash.onCast(env)}
        // 持续光束（ur_beam 法术）服务端每 tick 驱动：用 Architectury 跨平台事件，
        // fabric/forge 与单机（集成服务器）都生效。放 init() 而非 initServer()——
        // Fabric 的 DedicatedServerModInitializer 只在专服运行，单机需要这里注册。
        TickEvent.SERVER_POST.register { server -> BeamTicker.tickAll(server) }
    }
    fun initServer() {
        HexMobServerConfig.initServer()
    }
}
