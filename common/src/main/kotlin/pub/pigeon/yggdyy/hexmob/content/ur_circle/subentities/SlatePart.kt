package pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities

import at.petrak.hexcasting.api.casting.math.HexPattern
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCirclePart

open class SlatePart(parentMob: UrCircleEntity, id: ResourceLocation, val pattern: HexPattern): UrCirclePart(parentMob, id, 1F, 1F) {
    constructor(parentMob: UrCircleEntity, pattern: HexPattern): this(parentMob, HexMob.id("slate"), pattern)
}