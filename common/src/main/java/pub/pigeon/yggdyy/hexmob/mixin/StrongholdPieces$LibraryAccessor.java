package pub.pigeon.yggdyy.hexmob.mixin;

import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StrongholdPieces.Library.class)
public interface StrongholdPieces$LibraryAccessor {
    @Accessor
    boolean isIsTall();
}
