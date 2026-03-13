package pub.pigeon.yggdyy.hexmob.networking.msg

import dev.architectury.networking.NetworkChannel
import dev.architectury.networking.NetworkManager.PacketContext
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.networking.HexMobNetworking
import pub.pigeon.yggdyy.hexmob.networking.handler.applyOnClient
import pub.pigeon.yggdyy.hexmob.networking.handler.applyOnServer
import net.fabricmc.api.EnvType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import java.util.function.Supplier

sealed interface HexMobMessage

sealed interface HexMobMessageC2S : HexMobMessage {
    fun sendToServer() {
        HexMobNetworking.CHANNEL.sendToServer(this)
    }
}

sealed interface HexMobMessageS2C : HexMobMessage {
    fun sendToPlayer(player: ServerPlayer) {
        HexMobNetworking.CHANNEL.sendToPlayer(player, this)
    }

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        HexMobNetworking.CHANNEL.sendToPlayers(players, this)
    }
}

sealed interface HexMobMessageCompanion<T : HexMobMessage> {
    val type: Class<T>

    fun decode(buf: FriendlyByteBuf): T

    fun T.encode(buf: FriendlyByteBuf)

    fun apply(msg: T, supplier: Supplier<PacketContext>) {
        val ctx = supplier.get()
        when (ctx.env) {
            EnvType.SERVER, null -> {
                HexMob.LOGGER.debug("Server received packet from {}: {}", ctx.player.name.string, this)
                when (msg) {
                    is HexMobMessageC2S -> msg.applyOnServer(ctx)
                    else -> HexMob.LOGGER.warn("Message not handled on server: {}", msg::class)
                }
            }
            EnvType.CLIENT -> {
                HexMob.LOGGER.debug("Client received packet: {}", this)
                when (msg) {
                    is HexMobMessageS2C -> msg.applyOnClient(ctx)
                    else -> HexMob.LOGGER.warn("Message not handled on client: {}", msg::class)
                }
            }
        }
    }

    fun register(channel: NetworkChannel) {
        channel.register(type, { msg, buf -> msg.encode(buf) }, ::decode, ::apply)
    }
}
