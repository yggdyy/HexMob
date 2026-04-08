package pub.pigeon.yggdyy.hexmob.content.ur_circle

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation

abstract class PartRenderer(val id: ResourceLocation) {
    abstract fun render(part: UrCirclePart, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, partialTick: Float, context: EntityRendererProvider.Context)
}