package pub.pigeon.yggdyy.hexmob.config

import at.petrak.hexcasting.api.misc.MediaConstants
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.utils.GameInstance
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.ConfigHolder
import me.shedaniel.autoconfig.ConfigManager
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.TransitiveObject
import me.shedaniel.autoconfig.serializer.PartitioningSerializer
import me.shedaniel.autoconfig.serializer.PartitioningSerializer.GlobalData
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.InteractionResult
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.networking.msg.MsgSyncConfigS2C

object HexMobServerConfig {
    @JvmStatic
    lateinit var holder: ConfigHolder<GlobalConfig>
    @JvmStatic
    val config get() = syncedServerConfig ?: holder.config.server
    // only used on the client
    private var syncedServerConfig: ServerConfig? = null

    /** 大环体积倍率上下限。 */
    const val MIN_UR_CIRCLE_SCALE = 0.25F
    const val MAX_UR_CIRCLE_SCALE = 16.0F

    /** 命令热调整大环体积倍率：改值 → 落盘 → 广播给所有在线玩家（客户端渲染同步）。 */
    @JvmStatic
    fun setUrCircleScale(value: Float) {
        config.urCircleScale = value.coerceIn(MIN_UR_CIRCLE_SCALE, MAX_UR_CIRCLE_SCALE)
        persistServerConfig()
        val server = GameInstance.getServer() ?: return
        val players = server.playerList.players
        if (players.isNotEmpty()) {
            MsgSyncConfigS2C(holder.config.server).sendToPlayers(players)
        }
    }

    /** 直接把当前 server 分区序列化到 config/hexmob.toml（绕过会拦截保存的 FAIL 监听器，写文件由命令触发）。 */
    private fun persistServerConfig() {
        try {
            val mgr = holder as ConfigManager<*>
            @Suppress("UNCHECKED_CAST")
            (mgr as ConfigManager<GlobalConfig>).getSerializer().serialize(holder.config)
        } catch (t: Throwable) {
            HexMob.LOGGER.warn("Failed to persist server config", t)
        }
    }

    fun init() {
        holder = AutoConfig.register(
            GlobalConfig::class.java,
            PartitioningSerializer.wrap(::Toml4jConfigSerializer),
        )

        // prevent this holder from saving the server config; that happens in the client config gui
        holder.registerSaveListener { _, _ -> InteractionResult.FAIL }
    }
    fun initServer() {
        PlayerEvent.PLAYER_JOIN.register { player ->
            MsgSyncConfigS2C(holder.config.server).sendToPlayer(player)
        }
    }
    fun onSyncConfig(serverConfig: ServerConfig?) {
        syncedServerConfig = serverConfig
    }
    @Config(name = HexMob.MODID)
    class GlobalConfig(
        @Category("server")
        @TransitiveObject
        val server: ServerConfig = ServerConfig(),
    ) : GlobalData()
    @Config(name = "server")
    class ServerConfig : ConfigData {
        /*
        @Tooltip
        var dummyServerConfigOption: Int = 64
            private set
         */
        @Tooltip
        var opTransformStimulatedPatternCost: Long = MediaConstants.DUST_UNIT * 10
        @Tooltip
        var opStimulatedSlateMediaDiscount: Double = 0.1
        @Tooltip
        var stimulatedSlateBlacklist: MutableList<String> = mutableListOf()
        @Tooltip
        var stimulatedPatternSpawnRate: Double = 0.5
        @Tooltip
        var cryingAmethystSpawnRate: Double = 0.5
        /** 大环体积倍率（0.25~4.0，默认 1.0 = 现状；渲染/半径/碰撞一致缩放）。 */
        @Tooltip
        var urCircleScale: Float = 4.0F
        fun encode(buf: FriendlyByteBuf) {
            buf.writeLong(opTransformStimulatedPatternCost)
            buf.writeDouble(opStimulatedSlateMediaDiscount)
            buf.writeDouble(stimulatedPatternSpawnRate)
            buf.writeFloat(urCircleScale)
        }
        fun decode(buf: FriendlyByteBuf): ServerConfig {
            opTransformStimulatedPatternCost = buf.readVarLong()
            opStimulatedSlateMediaDiscount = buf.readDouble()
            stimulatedPatternSpawnRate = buf.readDouble()
            urCircleScale = buf.readFloat()
            return this
        }
    }
}
