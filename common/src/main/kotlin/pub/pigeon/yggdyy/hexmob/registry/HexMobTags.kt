package pub.pigeon.yggdyy.hexmob.registry

import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Block
import pub.pigeon.yggdyy.hexmob.HexMob

object HexMobTags {
    object BlockTags {
        private fun make(id: String): TagKey<Block> = TagKey.create(Registries.BLOCK, HexMob.id(id))
        val GEODE_WALL: TagKey<Block> = make("geode_wall")
        val AKASHIC_LIBRARY_WALL: TagKey<Block> = make("akashic_library_wall")
    }

    object EntityTypeTags {
        private fun make(id: String): TagKey<EntityType<*>> = TagKey.create(Registries.ENTITY_TYPE, HexMob.id(id))

        /** "有智慧"的生物：大环（Ur Circle）的索敌目标——村民、流浪商人、玩家与灾厄村民。 */
        val WISE: TagKey<EntityType<*>> = make("wise")
    }
}