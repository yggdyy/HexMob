@file:JvmName("HexMobAbstractionsImpl")

package pub.pigeon.yggdyy.hexmob.forge

import pub.pigeon.yggdyy.hexmob.registry.HexMobRegistrar
import net.minecraftforge.registries.RegisterEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

fun <T : Any> initRegistry(registrar: HexMobRegistrar<T>) {
    MOD_BUS.addListener { event: RegisterEvent ->
        event.register(registrar.registryKey) { helper ->
            registrar.init(helper::register)
        }
    }
}
