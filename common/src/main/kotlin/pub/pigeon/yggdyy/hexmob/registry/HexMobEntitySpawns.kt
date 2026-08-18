package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.level.biome.BiomeModifications
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.SpawnPlacements
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.levelgen.Heightmap

/**
 * Natural spawning for the iota-sheep: a rare, grassland spawn (Option A).
 * Sheep-like biomes only, as a CREATURE entry with a low weight, and proper
 * ON_GROUND placement so natural spawning can succeed.
 */
object HexMobEntitySpawns {
    private val GRASSY_BIOMES: Set<ResourceLocation> = setOf(
        ResourceLocation("minecraft:plains"),
        ResourceLocation("minecraft:sunflower_plains"),
        ResourceLocation("minecraft:meadow"),
        ResourceLocation("minecraft:snowy_plains"),
        ResourceLocation("minecraft:windswept_hills"),
        ResourceLocation("minecraft:cherry_grove"),
    )

    private val OVERWORLD: TagKey<Biome> =
        TagKey.create(Registries.BIOME, ResourceLocation("minecraft:is_overworld"))

    fun init() {
        // Must be placed before it can spawn from biome spawn entries.
        SpawnPlacementsRegistry.register(
            { HexMobEntities.IOTA_SHEEP.get() },
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            SpawnPlacements.SpawnPredicate { type, world, spawnReason, pos, random ->
                Mob.checkMobSpawnRules(type, world, spawnReason, pos, random)
            },
        )

        BiomeModifications.addProperties({ ctx -> ctx.getKey().map { it in GRASSY_BIOMES }.orElse(false) })
        { _, properties ->
            properties.getSpawnProperties().addSpawn(
                MobCategory.CREATURE,
                MobSpawnSettings.SpawnerData(HexMobEntities.IOTA_SHEEP.get(), 8, 3, 5),
            )
        }
    }
}
