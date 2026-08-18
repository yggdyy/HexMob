package pub.pigeon.yggdyy.hexmob.content.iota_sheep

import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import pub.pigeon.yggdyy.hexmob.api.sheep.IotaSheepBehaviors

/**
 * The iota-sheep's stock behaviours, registered by stored iota type.
 * Call [init] once at mod start.
 *
 * Mods may add their own via [IotaSheepBehaviors] (no mixin needed).
 */
object IotaSheepDefaultBehaviors {
    fun init() {
        // Vec3Iota -> walk toward that position (re-steer every few ticks).
        IotaSheepBehaviors.register(Vec3Iota.TYPE) { sheep, iota ->
            val pos = (iota as Vec3Iota).vec3
            if (sheep.tickCount % 4 == 0) {
                sheep.moveToward(pos.x, pos.y, pos.z, 1.0)
            }
        }

        // EntityIota -> seek out that entity and walk toward it.
        IotaSheepBehaviors.register(EntityIota.TYPE) { sheep, iota ->
            val target = (iota as EntityIota).entity ?: return@register
            if (sheep.tickCount % 10 == 0) {
                sheep.moveToward(target, 1.0)
            }
        }

        // ListIota:
        //  - every element is a Vec3Iota  -> patrol those waypoints in order;
        //  - otherwise                     -> cycle the wool colour across the
        //                                    list's element colours.
        IotaSheepBehaviors.register(ListIota.TYPE) { sheep, iota ->
            val elements = (iota as ListIota).list.toList()
            if (elements.isEmpty()) return@register
            if (elements.all { it is Vec3Iota }) {
                // waypoint patrol: pick the current waypoint, re-steer often
                val index = (sheep.tickCount / 30) % elements.size
                val wp = (elements[index] as Vec3Iota).vec3
                if (sheep.tickCount % 4 == 0) {
                    sheep.moveToward(wp.x, wp.y, wp.z, 1.0)
                }
            } else {
                // colour cycle: one colour every ~0.4 s
                val index = (sheep.tickCount / 8) % elements.size
                sheep.setDynamicColour(elements[index].type.color())
            }
        }

        // NullIota -> docile: the sheep follows no iota-driven impulse.
        // Registered so the empty case is explicit and other mods can override it.
        IotaSheepBehaviors.register(NullIota.TYPE) { _, _ ->
            /* docile — the sheep behaves like an ordinary sheep */
        }
    }
}
