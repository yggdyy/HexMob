package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob

/** 斧头守卫渲染器：卫道士几何模型 + guard_brute.png（贴图可直接改），手持铁斧（不掉落）。 */
class GuardBruteRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<GuardBrute, GuardIllagerModel<GuardBrute>>(
        context,
        GuardIllagerModel(context.bakeLayer(ModelLayers.VINDICATOR)),
        0.5F
    ) {
    init {
        addLayer(ItemInHandLayer(this, context.itemInHandRenderer))
    }

    override fun getTextureLocation(entity: GuardBrute): ResourceLocation = TEXTURE

    companion object {
        val TEXTURE: ResourceLocation = HexMob.id("textures/entity/guard/guard_brute.png")
    }
}
