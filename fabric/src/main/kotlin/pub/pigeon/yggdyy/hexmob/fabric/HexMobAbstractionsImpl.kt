@file:JvmName("HexMobAbstractionsImpl")

package pub.pigeon.yggdyy.hexmob.fabric

import pub.pigeon.yggdyy.hexmob.registry.HexMobRegistrar
import net.minecraft.core.Registry

fun <T : Any> initRegistry(registrar: HexMobRegistrar<T>) {
    val registry = registrar.registry
    registrar.init { id, value -> Registry.register(registry, id, value) }
}
