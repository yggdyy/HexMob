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
import pub.pigeon.yggdyy.hexmob.HexMob

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

        // 晶刺守卫（弓箭/斧头/傀儡）：crystal_spikes 群系怪生成条目在群系 JSON 的
        // spawners.monster 里（data/hexmob/worldgen/biome/crystal_spikes.json）。
        // 这里再走 BiomeModifications 运行时注册一遍（iota 羊同款、已证可行），
        // 双保险确保带 TerraBlender/数据包群系也能进生成权表。
        // 日志：确认运行时是否真的匹配到该群系 + 放置规则是否注册。
        BiomeModifications.addProperties({ ctx ->
            val key = ctx.getKey()
            if (key.isPresent && key.get() == ResourceLocation("hexmob", "crystal_spikes")) {
                HexMob.LOGGER.info("[Spawn] crystal_spikes 匹配到 BiomeModifications，正在添加守卫生成条目")
                true
            } else {
                false
            }
        })
        { _, properties ->
            HexMob.LOGGER.info("[Spawn] 正在把守卫写进 crystal_spikes 生成权表")
            properties.getSpawnProperties().addSpawn(
                MobCategory.MONSTER,
                MobSpawnSettings.SpawnerData(HexMobEntities.GUARD_ARCHER.get(), 30, 1, 2),
            )
            properties.getSpawnProperties().addSpawn(
                MobCategory.MONSTER,
                MobSpawnSettings.SpawnerData(HexMobEntities.GUARD_BRUTE.get(), 30, 1, 2),
            )
            properties.getSpawnProperties().addSpawn(
                MobCategory.MONSTER,
                MobSpawnSettings.SpawnerData(HexMobEntities.GUARD_GOLEM.get(), 10, 1, 1),
            )
        }

        // 出生点规则：自然生成的先决条件。
        // 判定无条件通过 → 白天/夜晚/任意亮度/任意方块都刷（守卫不烧不惧光）。
        listOf(
            HexMobEntities.GUARD_ARCHER.get(),
            HexMobEntities.GUARD_BRUTE.get(),
            HexMobEntities.GUARD_GOLEM.get(),
        ).forEach { type ->
            SpawnPlacementsRegistry.register(
                { type },
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnPlacements.SpawnPredicate { _, _, _, _, _ -> true },
            )
        }
        HexMob.LOGGER.info("[Spawn] 守卫放置规则注册完成：archer/brute/golem")
    }
}
