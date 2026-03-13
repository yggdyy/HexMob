package pub.pigeon.yggdyy.hexmob.networking

import dev.architectury.networking.NetworkChannel
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.networking.msg.HexMobMessageCompanion

object HexMobNetworking {
    val CHANNEL: NetworkChannel = NetworkChannel.create(HexMob.id("networking_channel"))

    fun init() {
        for (subclass in HexMobMessageCompanion::class.sealedSubclasses) {
            subclass.objectInstance?.register(CHANNEL)
        }
    }
}
