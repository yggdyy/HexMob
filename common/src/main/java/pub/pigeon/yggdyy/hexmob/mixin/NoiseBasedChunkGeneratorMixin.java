package pub.pigeon.yggdyy.hexmob.mixin;

import at.petrak.hexcasting.common.lib.HexBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 用 mixin 给主世界地表规则"包一层"，而不是覆盖整个 overworld.json 数据包：
 * 在 NoiseBasedChunkGenerator.buildSurface 把地表规则传给 SurfaceSystem 之前，
 * 把原规则包成 sequence([晶簇尖刺→板岩块, 原规则])。
 *
 * 原规则原样保留、只有 hexmob:crystal_spikes 群系被替换成板岩块地表；
 * 与任何其它 mod 的数据包/地表 mod 都不冲突（不碰 data/minecraft/worldgen/noise_settings）。
 * 注：用 HexBlocks.SLATE_BLOCK（板岩块）时须懒加载——它是 Hex Casting 注册的，mixin 类加载时可能未初始化。
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    private static final ResourceKey<Biome> CRYSTAL_SPIKES =
        ResourceKey.create(Registries.BIOME, new ResourceLocation("hexmob", "crystal_spikes"));

    private static SurfaceRules.RuleSource crystalSpikesSurface() {
        SurfaceRules.RuleSource surface = CRYSTAL_SPIKES_SURFACE;
        if (surface == null) {
            surface = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(CRYSTAL_SPIKES),
                SurfaceRules.sequence(
                    SurfaceRules.state(HexBlocks.SLATE_BLOCK.defaultBlockState()),
                    SurfaceRules.state(HexBlocks.SLATE_BLOCK.defaultBlockState())
                )
            );
            CRYSTAL_SPIKES_SURFACE = surface;
        }
        return surface;
    }

    private static SurfaceRules.RuleSource CRYSTAL_SPIKES_SURFACE;

    @ModifyArg(
        method = "buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/blending/Blender;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/SurfaceSystem;buildSurface(Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/core/Registry;ZLnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;)V"
        ),
        index = 7
    )
    private SurfaceRules.RuleSource hexmob$addCrystalSpikesSurface(SurfaceRules.RuleSource originalRule) {
        return SurfaceRules.sequence(crystalSpikesSurface(), originalRule);
    }
}
