package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.levelgen.structure.StructureType
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleArenaStructure

object HexMobStructures {
    fun init() {
        TYPES.register()
    }
    val TYPES: DeferredRegister<StructureType<*>> = DeferredRegister.create(HexMob.MODID, Registries.STRUCTURE_TYPE)
    val UR_CIRCLE_ARENA: DeferredSupplier<StructureType<*>> =
        TYPES.register("ur_circle_arena") { StructureType { UrCircleArenaStructure.CODEC } }
}
