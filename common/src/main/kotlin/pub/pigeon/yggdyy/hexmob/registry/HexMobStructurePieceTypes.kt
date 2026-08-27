package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.akashic_library.AkashicLibraryPiece
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleArenaPiece

object HexMobStructurePieceTypes {
    fun init() {
        TYPES.register()
    }
    val TYPES: DeferredRegister<StructurePieceType> = DeferredRegister.create(HexMob.MODID, Registries.STRUCTURE_PIECE)
    val AKASHIC_LIBRARY: DeferredSupplier<StructurePieceType> = TYPES.register("akashic_library") {StructurePieceType { _, compoundTag -> AkashicLibraryPiece(compoundTag) }}
    val UR_CIRCLE_ARENA: DeferredSupplier<StructurePieceType> = TYPES.register("ur_circle_arena") { StructurePieceType { ctx, compoundTag -> UrCircleArenaPiece(ctx, compoundTag) } }
}