package pub.pigeon.yggdyy.hexmob.mixin;

import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pub.pigeon.yggdyy.hexmob.api.entity.FlickeringEntity;

import java.util.List;

/**
 * Makes EntityIota references to {@link FlickeringEntity}s (e.g. the Ur Circle)
 * unreliable: whenever a spell resolves an entity iota that points at such a
 * boss, the resolution periodically "flickers" and throws MishapBadEntity — so
 * the boss cannot be reliably driven, teleported or re-targeted through its
 * entity iota.
 *
 * <p>We deliberately hook the spell-facing resolution helper
 * ({@code ActionUtilsKt.getEntity}) instead of {@code EntityIota.getEntity()}
 * itself: every spell op resolves entities through the helper (with proper
 * mishap handling), while the iota's own getter is used by UI display and the
 * redstone impetus, which must keep seeing the entity.
 *
 * <p>Target is a mod-owned static method whose name never changes under SRG,
 * hence {@code remap = false}. The facade class of {@code ActionUtils.kt} is
 * named {@code OperatorUtils} via {@code @file:JvmName}.
 */
@Mixin(OperatorUtils.class)
public abstract class EntityIotaFlickerMixin {
    @Inject(method = "getEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void hexmob$flickerEntityIota(List<Iota> list, int idx, int argc,
        CallbackInfoReturnable<Entity> cir) {
        // let the original handle out-of-range indices (MishapNotEnoughArgs)
        if (idx < 0 || idx >= list.size()) {
            return;
        }
        Iota iota = list.get(idx);
        if (iota instanceof EntityIota entityIota) {
            Entity e = entityIota.getEntity();
            if (e instanceof FlickeringEntity flicker && flicker.isReferenceFlickering(e.level().getGameTime())) {
                // the boss's own mishap: text & effect are entity-defined
                throw flicker.createFlickeringMishap(e);
            }
        }
    }
}
