package pub.pigeon.yggdyy.hexmob.content

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

abstract class HMEntityPart(entityType: EntityType<*>, level: Level) : Entity(entityType, level) {
    abstract fun shouldRenderAsPart(): Boolean
    abstract fun shouldInteract(): Boolean
}