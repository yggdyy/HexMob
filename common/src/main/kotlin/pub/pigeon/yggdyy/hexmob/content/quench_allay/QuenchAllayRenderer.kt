package pub.pigeon.yggdyy.hexmob.content.quench_allay

import net.minecraft.client.renderer.entity.AllayRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.animal.allay.Allay
import pub.pigeon.yggdyy.hexmob.HexMob

/**
 * Renders the quenched allay like a vanilla allay, but periodically cycles the
 * texture (every ~2 s) through the mod-owned variants at
 * assets/hexmob/textures/entity/quench_allay/allay_{0,1,2}.png — edit those to
 * customise each skin.
 */
class QuenchAllayRenderer(context: EntityRendererProvider.Context) : AllayRenderer(context) {

    private val TEXTS: List<ResourceLocation> = listOf(
        HexMob.id("textures/entity/quench_allay/allay_0.png"),
        HexMob.id("textures/entity/quench_allay/allay_1.png"),
        HexMob.id("textures/entity/quench_allay/allay_2.png"),
    )

    override fun getTextureLocation(entity: Allay): ResourceLocation {
        val index = (entity.tickCount / 40) % TEXTS.size
        return TEXTS[index]
    }
}
