package pub.pigeon.yggdyy.hexmob.content.crying_amethyst

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import pub.pigeon.yggdyy.hexmob.HexMob
import java.util.function.Function
import java.util.function.Supplier

class CryingAmethystRenderer(val context: EntityRendererProvider.Context) : EntityRenderer<CryingAmethystEntity>(context) {
    override fun getTextureLocation(entity: CryingAmethystEntity): ResourceLocation {
        return HexMob.id("null")
    }
    override fun render(
        entity: CryingAmethystEntity,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight)
        poseStack.pushPose()
        poseStack.translate(-0.5, 0.0, -0.5)
        poseStack.scale(entity.bbWidth, entity.bbHeight, entity.bbWidth)
        renderModel(poseStack, buffer, baseSupplier.get(), packedLight)
        poseStack.translate(0.0, 1.0, 0.0)
        renderModel(poseStack, buffer, budSupplier.apply(entity), packedLight)
        poseStack.popPose()
    }
    companion object {
        private fun renderModel(poseStack: PoseStack, buffer: MultiBufferSource, model: BakedModel, light: Int) {
            poseStack.pushPose()
            Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), Blocks.AIR.defaultBlockState(), model, 1F, 1F, 1F, light, OverlayTexture.NO_OVERLAY)
            poseStack.popPose()
        }
        private fun getBakedModel(rl: ModelResourceLocation) = Minecraft.getInstance().modelManager.getModel(rl)
        private val baseSupplier: Supplier<BakedModel> = Supplier{ getBakedModel(ModelResourceLocation("minecraft", "budding_amethyst", "")) }
        private val buds: List<ModelResourceLocation> = listOf(
            ModelResourceLocation("minecraft", "small_amethyst_bud", "facing=up,waterlogged=false"),
            ModelResourceLocation("minecraft", "medium_amethyst_bud", "facing=up,waterlogged=false"),
            ModelResourceLocation("minecraft", "large_amethyst_bud", "facing=up,waterlogged=false"),
            ModelResourceLocation("minecraft", "amethyst_cluster", "facing=up,waterlogged=false"),
        )
        private val budSupplier: Function<CryingAmethystEntity, BakedModel> = Function { entity ->
            val idx: Int = (entity.health / (entity.maxHealth + 0.1F) * 4F).toInt()
            getBakedModel(buds[idx])
        }
    }
}