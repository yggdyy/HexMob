package pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities

import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCirclePart

open class CubePart(parentMob: UrCircleEntity, id: ResourceLocation, val modelId: ResourceLocation, val modelVariant: String): UrCirclePart(parentMob, id, 1F, 1F) {
    constructor(parentMob: UrCircleEntity, modelId: ResourceLocation, modelVariant: String): this(parentMob, HexMob.id("cube"), modelId, modelVariant)
}