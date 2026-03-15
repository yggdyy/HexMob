package pub.pigeon.yggdyy.hexmob.config

import at.petrak.hexcasting.api.misc.MediaConstants
import dev.architectury.event.events.common.PlayerEvent
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.ConfigHolder
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
        fun encode(buf: FriendlyByteBuf) {
            buf.writeLong(opTransformStimulatedPatternCost)
            buf.writeDouble(opStimulatedSlateMediaDiscount)
            buf.writeDouble(stimulatedPatternSpawnRate)
        }
        fun decode(buf: FriendlyByteBuf): ServerConfig {
            opTransformStimulatedPatternCost = buf.readVarLong()
            opStimulatedSlateMediaDiscount = buf.readDouble()
            stimulatedPatternSpawnRate = buf.readDouble()
            return this
        }
    }
}
