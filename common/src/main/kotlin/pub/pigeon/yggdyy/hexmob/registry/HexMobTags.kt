package pub.pigeon.yggdyy.hexmob.registry

import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import pub.pigeon.yggdyy.hexmob.HexMob

object HexMobTags {
    object BlockTags {
        private fun make(id: String): TagKey<Block> = TagKey.create(Registries.BLOCK, HexMob.id(id))
        val GEODE_WALL: TagKey<Block> = make("geode_wall")
        val AKASHIC_LIBRARY_WALL: TagKey<Block> = make("akashic_library_wall")
    }
}