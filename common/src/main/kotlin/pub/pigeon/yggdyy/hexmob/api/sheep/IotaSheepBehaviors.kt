package pub.pigeon.yggdyy.hexmob.api.sheep

import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.content.iota_sheep.IotaSheepEntity
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry mapping an [IotaType] to an [IotaSheepBehavior].
 *
 * Registering here is the public, mixin-free way for any mod to give the
 * iota-sheep a behaviour keyed by an iota type. [IotaSheepEntity] calls
 * [tick] on the server every tick while it holds a stored iota.
 */
object IotaSheepBehaviors {

    private val behaviors = ConcurrentHashMap<IotaType<*>, IotaSheepBehavior>()

    /** Attach [behavior] to every stored iota of [type]. */
    fun register(type: IotaType<*>, behavior: IotaSheepBehavior) {
        behaviors[type] = behavior
    }

    /** Like [register], but keyed by the iota type's registry id (e.g. "hexcasting:entity"). */
    fun register(typeId: ResourceLocation, behavior: IotaSheepBehavior) {
        val type = HexIotaTypes.REGISTRY.get(typeId)
            ?: throw IllegalArgumentException("Unknown iota type: $typeId")
        behaviors[type] = behavior
    }

    fun get(type: IotaType<*>): IotaSheepBehavior? = behaviors[type]

    /**
     * Server-side dispatch, called by [IotaSheepEntity.tick]. Deserializes the
     * sheep's stored iota and runs the behaviour registered for its type.
     */
    fun tick(sheep: IotaSheepEntity) {
        val world = sheep.getServerLevel() ?: return
        val iota = IotaType.deserialize(sheep.getIotaNbt(), world)
        behaviors[iota.type]?.tick(sheep, iota)
    }
}
