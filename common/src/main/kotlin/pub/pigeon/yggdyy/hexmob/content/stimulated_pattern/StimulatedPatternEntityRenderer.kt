package pub.pigeon.yggdyy.hexmob.content.stimulated_pattern

import at.petrak.hexcasting.client.render.PatternColors
import at.petrak.hexcasting.client.render.PatternRenderer
import at.petrak.hexcasting.client.render.PatternSettings
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import pub.pigeon.yggdyy.hexmob.HexMob

class StimulatedPatternEntityRenderer(context: EntityRendererProvider.Context) : EntityRenderer<StimulatedPatternEntity>(
    context
) {
    override fun getTextureLocation(entity: StimulatedPatternEntity): ResourceLocation {
        return HexMob.id("null")
    }
    override fun render(
        entity: StimulatedPatternEntity,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight)
        val delta: Vec3 = Minecraft.getInstance().player?.eyePosition?.subtract(entity.position().add(0.0, 0.5, 0.0))?.multiply(1.0, 0.0, 1.0)?.normalize() ?: return
        poseStack.pushPose()
        poseStack.mulPose(Quaternionf().rotateTo(0F, 0F, 1F, delta.x.toFloat(), delta.y.toFloat(), delta.z.toFloat()))
        poseStack.translate(-0.5, 1.0, 0.0)
        poseStack.scale(1F, -1F, 1F)
        PatternRenderer.renderPattern(entity.getPattern(), poseStack, SETTINGS, COLOR, 0.0, 512)
        poseStack.popPose()
    }
    companion object {
        val SETTINGS = PatternSettings(
            "stimulated_pattern_entity",
            PatternSettings.PositionSettings.paddedSquare(0.25),
            PatternSettings.StrokeSettings.fromStroke(0.8 / 16),
            PatternSettings.ZappySettings.WOBBLY
        )
        val COLOR: PatternColors = PatternColors.glowyStroke(0xFF_EE82EE.toInt())
    }
}