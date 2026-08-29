package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.client.model.IllagerModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.IllagerRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob

/** 板岩兵渲染器：**原版卫道士模型/渲染器**（含挥斧攻击动画）+ 玩家贴图替换。
 * 武器渲染层（ItemInHandLayer）显式加上：实测不手动加则手持武器不渲染。 */
class GuardBruteRenderer(context: EntityRendererProvider.Context) :
    IllagerRenderer<GuardBrute>(
        context,
        IllagerModel(context.bakeLayer(ModelLayers.VINDICATOR)),
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