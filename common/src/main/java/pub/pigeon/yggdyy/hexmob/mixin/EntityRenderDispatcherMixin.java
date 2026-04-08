package pub.pigeon.yggdyy.hexmob.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.pigeon.yggdyy.hexmob.content.IHMMultipartEntity;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "renderHitbox", at = @At("HEAD"))
    private static void renderHexMobMultipartEntityHitBox(PoseStack poseStack, VertexConsumer buffer, Entity entity, float partialTicks, CallbackInfo ci) {
        if(entity instanceof IHMMultipartEntity<?> parent) {
            double d = -Mth.lerp(partialTicks, entity.xOld, entity.getX());
            double e = -Mth.lerp(partialTicks, entity.yOld, entity.getY());
            double f = -Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            parent.getAllParts().forEach(part -> {
                if(part != null && part.shouldRenderAsPart()) {
                    double g = d + Mth.lerp(partialTicks, part.xOld, part.getX());
                    double h = e + Mth.lerp(partialTicks, part.yOld, part.getY());
                    double i = f + Mth.lerp(partialTicks, part.zOld, part.getZ());
                    poseStack.pushPose();
                    poseStack.translate(g, h, i);
                    LevelRenderer.renderLineBox(poseStack, buffer, part.getBoundingBox().move(-part.getX(), -part.getY(), -part.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
                    poseStack.popPose();
                }
            });
        }
    }
}
