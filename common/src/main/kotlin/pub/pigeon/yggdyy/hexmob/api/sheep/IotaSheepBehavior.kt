package pub.pigeon.yggdyy.hexmob.api.sheep

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import pub.pigeon.yggdyy.hexmob.content.iota_sheep.IotaSheepEntity

/**
 * A per-server-tick behaviour an [IotaSheepEntity] runs while it stores an iota
 * of a registered [IotaType]. The mapping `IotaType → behavior` lives in
 * [IotaSheepBehaviors].
 *
 * This is the **mixin-free public API**: any mod can give the iota-sheep a
 * behaviour simply by registering a type:
 *
 * ```
 * IotaSheepBehaviors.register(MyIota.TYPE) { sheep, iota ->
 *     sheep.moveToward(...) // whatever the iota should make the sheep do
 * }
 * ```
 */
fun interface IotaSheepBehavior {

    /**
     * Called on every server tick while the sheep stores an iota of the
     * registered type. Runs server-side only.
     *
     * @param sheep the iota-sheep executing the behaviour.
     * @param iota  the sheep's currently stored iota (already deserialized; its
     *              type is the registered key).
     */
    fun tick(sheep: IotaSheepEntity, iota: Iota)
}
