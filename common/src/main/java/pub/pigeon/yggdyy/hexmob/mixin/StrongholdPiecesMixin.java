package pub.pigeon.yggdyy.hexmob.mixin;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pub.pigeon.yggdyy.hexmob.HexMob;
import pub.pigeon.yggdyy.hexmob.content.akashic_library.AkashicLibraryPiece;

import java.lang.reflect.Constructor;
import java.util.List;

@Mixin(StrongholdPieces.class)
public abstract class StrongholdPiecesMixin{
    @Shadow private static List<Object> currentPieces;
    @Inject(method = "resetPieces", at = @At("TAIL"))
    private static void addHexMobStrongholdPieces(CallbackInfo ci){
        try {
            var lastPieceWeight = currentPieces.get(currentPieces.size() - 1);
            Class<?> pieceWeightClass = lastPieceWeight.getClass();
            Constructor<?> pieceWeightConstructor = pieceWeightClass.getDeclaredConstructor(Class.class, int.class, int.class);
            pieceWeightConstructor.setAccessible(true);
            var akashic_library = pieceWeightConstructor.newInstance(AkashicLibraryPiece.class, 10, 1);
            currentPieces.add(akashic_library);
        } catch (Exception exception) {
            HexMob.LOGGER.warn("[Failed to add stronghold pieces]: " + exception);
        }
    }
    @Inject(method = "findAndCreatePieceFactory", at = @At("RETURN"), cancellable = true)
    private static void constructHexMobStrongholdPieces(Class<?> pieceClass, StructurePieceAccessor pieces, RandomSource random, int x, int y, int z, Direction direction, int genDepth, CallbackInfoReturnable<Object> cir) {
        if(pieceClass == AkashicLibraryPiece.class) {
            cir.setReturnValue(AkashicLibraryPiece.createAkashicLibraryPiece(pieces, random, x, y, z, direction, genDepth));
        }
    }
}
