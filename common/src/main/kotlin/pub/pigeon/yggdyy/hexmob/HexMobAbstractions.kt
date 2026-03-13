@file:JvmName("HexMobAbstractions")

package pub.pigeon.yggdyy.hexmob

import dev.architectury.injectables.annotations.ExpectPlatform
import pub.pigeon.yggdyy.hexmob.registry.HexMobRegistrar

fun initRegistries(vararg registries: HexMobRegistrar<*>) {
    for (registry in registries) {
        initRegistry(registry)
    }
}
@ExpectPlatform
fun <T : Any> initRegistry(registrar: HexMobRegistrar<T>) {
    throw AssertionError()
}
