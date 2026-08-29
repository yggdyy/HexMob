package pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities

import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCirclePart

open class CubePart(
    parentMob: UrCircleEntity,
    id: ResourceLocation,
    width: Float,
    height: Float,
    val modelId: ResourceLocation,
    val modelVariant: String
) : UrCirclePart(parentMob, id, width, height) {
    constructor(parentMob: UrCircleEntity, id: ResourceLocation, modelId: ResourceLocation, modelVariant: String):
        this(parentMob, id, 1F, 1F, modelId, modelVariant)
    constructor(parentMob: UrCircleEntity, modelId: ResourceLocation, modelVariant: String):
        this(parentMob, HexMob.id("cube"), modelId, modelVariant)
}
