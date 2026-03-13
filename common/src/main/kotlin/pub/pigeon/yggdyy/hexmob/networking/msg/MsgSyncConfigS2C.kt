package pub.pigeon.yggdyy.hexmob.networking.msg

import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import net.minecraft.network.FriendlyByteBuf

data class MsgSyncConfigS2C(val serverConfig: HexMobServerConfig.ServerConfig) : HexMobMessageS2C {
    companion object : HexMobMessageCompanion<MsgSyncConfigS2C> {
        override val type = MsgSyncConfigS2C::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgSyncConfigS2C(
            serverConfig = HexMobServerConfig.ServerConfig().decode(buf),
        )

        override fun MsgSyncConfigS2C.encode(buf: FriendlyByteBuf) {
            serverConfig.encode(buf)
        }
    }
}
