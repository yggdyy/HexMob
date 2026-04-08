package pub.pigeon.yggdyy.hexmob.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pub.pigeon.yggdyy.hexmob.content.HMEntityPart;
import pub.pigeon.yggdyy.hexmob.content.IHMMultipartEntity;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void addHexMobMultipartEntities1(Entity entity, AABB area, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
        Level self = (Level) ((Object) this);
        List<Entity> res = cir.getReturnValue();
        IHMMultipartEntity.Companion.updateInstances();
        IHMMultipartEntity.Companion.getInstances().stream().filter(_e -> _e instanceof Entity e && _e != entity && self == e.level()).forEach(_e -> {
            _e.getAllParts().stream().filter(predicate.and(e -> e instanceof HMEntityPart e1 && e1.shouldInteract() && e1.getBoundingBox().intersects(area))).forEach(res::add);
        });
        cir.setReturnValue(res);
    }
    @Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V", at = @At("RETURN"))
    private void addHexMobMultipartEntities2(EntityTypeTest<Entity, Entity> entityTypeTest, AABB bounds, Predicate<Entity> predicate, List<Entity> output, int maxResults, CallbackInfo ci) {
        Level self = (Level) ((Object) this);
        IHMMultipartEntity.Companion.updateInstances();
        IHMMultipartEntity.Companion.getInstances().stream().filter(_e -> _e instanceof Entity e && self == e.level()).forEach(_e -> {
            _e.getAllParts().forEach(e -> {
                if(!e.shouldInteract()) {
                    return;
                }
                Entity e1 = entityTypeTest.tryCast(e);
                if(e1 != null && e1.getBoundingBox().intersects(bounds) && predicate.test(e1)) {
                    if(output.size() < maxResults) {
                        output.add(e1);
                    }
                }
            });
        });
    }
}
