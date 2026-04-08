package pub.pigeon.yggdyy.hexmob.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pub.pigeon.yggdyy.hexmob.content.HMEntityPart;
import pub.pigeon.yggdyy.hexmob.content.IHMMultipartEntity;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "getEntityOrPart", at = @At("RETURN"), cancellable = true)
    private void getHexMobEntityPart(int id, CallbackInfoReturnable<Entity> cir) {
        if(cir.getReturnValue() == null) {
            ServerLevel self = (ServerLevel) ((Object) this);
            IHMMultipartEntity.Companion.updateInstances();
            for(IHMMultipartEntity<?> _parent : IHMMultipartEntity.Companion.getInstances()) {
                if(_parent instanceof Entity parent && parent.level() == self) {
                    for(HMEntityPart part : _parent.getAllParts()) {
                        if(part.shouldInteract() && part.getId() == id) {
                            cir.setReturnValue(part);
                            return;
                        }
                    }
                }
            }
        }
    }
}
