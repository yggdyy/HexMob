package pub.pigeon.yggdyy.hexmob.content.amethyst_silverfish

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer

class AmethystSilverfishRenderer(val context: EntityRendererProvider.Context): GeoEntityRenderer<AmethystSilverfishEntity>(context, Model()) {
    companion object {
        class Model: GeoModel<AmethystSilverfishEntity>() {
            override fun getModelResource(animatable: AmethystSilverfishEntity?): ResourceLocation = model
            override fun getTextureResource(animatable: AmethystSilverfishEntity?): ResourceLocation = texture
            override fun getAnimationResource(animatable: AmethystSilverfishEntity?): ResourceLocation = animations
            companion object {
                private val model: ResourceLocation = HexMob.id("geo/entity/amethyst_silverfish.geo.json")
                private val texture: ResourceLocation = HexMob.id("textures/entity/amethyst_silverfish.png")
                private val animations: ResourceLocation = HexMob.id("animations/entity/amethyst_silverfish.animation.json")
            }
        }
    }
}