package pub.pigeon.yggdyy.hexmob.content.iota_sheep

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.SheepFurModel
import net.minecraft.client.model.SheepModel
import net.minecraft.client.model.geom.EntityModelSet
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.SheepRenderer
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.animal.Sheep
import pub.pigeon.yggdyy.hexmob.HexMob

/**
 * Renders the iota-sheep with two custom layers that replace the vanilla
 * [net.minecraft.client.renderer.entity.layers.SheepFurLayer]:
 * - the wool is tinted with the stored iota's *exact* ARGB (white when none);
 * - a faint emissive glow pass in the same color makes the wool look soaked in
 *   magic.
 *
 * The layers bake the proper `SheepFurModel` (from ModelLayers.SHEEP_FUR) — the
 * wool texture's UVs are laid out for that model, NOT the base SheepModel.
 * Rendering `sheep_fur.png` onto the base model would leave the head UVs pointing
 * at an empty region, so the tan base head shows through (the "brown head" bug).
 *
 * Note: in 1.20.1 `SheepRenderer` has no overridable addLayers() hook (it adds
 * SheepFurLayer inside its own constructor), so the custom layers are added here
 * in the constructor instead, and the vanilla fur layer never is.
 */
class IotaSheepRenderer(context: EntityRendererProvider.Context) : SheepRenderer(context) {

    init {
        val modelSet = context.modelSet
        this.addLayer(IotaWoolLayer(this, modelSet))
        this.addLayer(IotaGlowLayer(this, modelSet))
    }

    /** Use the mod-owned base skin (editable), instead of the vanilla sheep.png. */
    override fun getTextureLocation(entity: Sheep): ResourceLocation = SHEEP_BASE

    private companion object Fur {
        /** Mod-owned copy of the vanilla wool texture, editable at
         *  assets/hexmob/textures/entity/iota_sheep/sheep_fur.png. */
        val SHEEP_FUR: ResourceLocation = HexMob.id("textures/entity/iota_sheep/sheep_fur.png")

        /** Mod-owned copy of the vanilla base skin, editable at
         *  assets/hexmob/textures/entity/iota_sheep/sheep.png. */
        val SHEEP_BASE: ResourceLocation = HexMob.id("textures/entity/iota_sheep/sheep.png")

        fun rgb(color: Int): Triple<Float, Float, Float> = Triple(
            (color shr 16 and 0xFF) / 255f,
            (color shr 8 and 0xFF) / 255f,
            (color and 0xFF) / 255f,
        )
    }

    /** ① Exact-ARGB wool tint (replaces the vanilla SheepFurLayer). */
    private class IotaWoolLayer(
        renderer: RenderLayerParent<Sheep, SheepModel<Sheep>>,
        modelSet: EntityModelSet,
    ) : RenderLayer<Sheep, SheepModel<Sheep>>(renderer) {

        private val furModel: SheepFurModel<Sheep> =
            SheepFurModel(modelSet.bakeLayer(ModelLayers.SHEEP_FUR))

        override fun render(
            poseStack: PoseStack,
            multiBufferSource: MultiBufferSource,
            packedLight: Int,
            entity: Sheep,
            limbSwing: Float, limbSwingAmount: Float, partialTick: Float,
            ageInTicks: Float, netHeadYaw: Float, headPitch: Float,
        ) {
            if (entity.isSheared || entity.isInvisible) return
            val sheep = entity as IotaSheepEntity
            val (r, g, b) = rgb(sheep.getIotaArgb())
            coloredCutoutModelCopyLayerRender(
                getParentModel(), furModel, SHEEP_FUR,
                poseStack, multiBufferSource, packedLight, entity,
                limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTick,
                r, g, b,
            )
        }
    }

    /** ③ Emissive glow pass in the iota color. */
    private class IotaGlowLayer(
        renderer: RenderLayerParent<Sheep, SheepModel<Sheep>>,
        modelSet: EntityModelSet,
    ) : RenderLayer<Sheep, SheepModel<Sheep>>(renderer) {

        private val furModel: SheepFurModel<Sheep> =
            SheepFurModel(modelSet.bakeLayer(ModelLayers.SHEEP_FUR))

        override fun render(
            poseStack: PoseStack,
            multiBufferSource: MultiBufferSource,
            packedLight: Int,
            entity: Sheep,
            limbSwing: Float, limbSwingAmount: Float, partialTick: Float,
            ageInTicks: Float, netHeadYaw: Float, headPitch: Float,
        ) {
            if (entity.isSheared || entity.isInvisible) return
            val sheep = entity as IotaSheepEntity
            val (r, g, b) = rgb(sheep.getIotaArgb())
            // copy the parent's pose onto the fur model, then draw it emissively
            getParentModel().copyPropertiesTo(furModel)
            furModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick)
            furModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch)
            furModel.renderToBuffer(
                poseStack,
                multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(SHEEP_FUR)),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r, g, b, 0.28f,
            )
        }
    }
}
