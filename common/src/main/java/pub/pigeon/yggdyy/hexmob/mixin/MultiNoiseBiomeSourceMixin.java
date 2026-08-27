package pub.pigeon.yggdyy.hexmob.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 【晶簇尖刺散落】自包含地让群系在正常主世界可靠出现。
 *
 * 为什么不靠往 OverworldBiomeBuilder 加参数点：原版气候网格被原版群系铺满，
 * 新点只能叠在已有格子上，RTree 按距离裁决 → 窄参数带几乎永不被选中（实测找不到）。
 * 1.20.1 的 offset 也不是目标点的一维（TargetPoint 只有 6 个值），所以"offset 变体"不成立。
 *
 * 正确做法（等价 TerraBlender 的位置散射，但完全自包含）：
 * 在 MultiNoiseBiomeSource.getNoiseBiome 里做后置过滤——当原版选中【黑森林/沼泽等湿润温带群系】、
 * 且该列气候的奇异度落在 CRYSTAL_WEIRDNESS 子带时，改判为 hexmob:crystal_spikes。
 * 纯按噪声、确定性（服务端/客户端一致）、可调频率、不影响任何其它群系。
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
    private static final ResourceKey<Biome> CRYSTAL_SPIKES =
        ResourceKey.create(Registries.BIOME, new ResourceLocation("hexmob", "crystal_spikes"));
    /** 奇异度子带：晶簇尖刺散落在宿主格子的这一小段里（调这两个值 = 调出现频率/分布）。 */
    private static final Climate.Parameter CRYSTAL_WEIRDNESS = Climate.Parameter.span(-0.8F, -0.45F);
    /** 宿主群系：黑森林/沼泽这类湿润温带群系，晶簇尖刺与它们"一起生成"。 */
    private static final java.util.Set<ResourceKey<Biome>> HOSTS = java.util.Set.of(
        Biomes.DARK_FOREST, Biomes.SWAMP, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.FLOWER_FOREST
    );
    private static Holder<Biome> crystalHolder;

    @Shadow
    private Climate.ParameterList<Holder<Biome>> parameters() {
        return null;
    }

    @Inject(
        method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void hexmob$scatterCrystalSpikes(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        Holder<Biome> biome = cir.getReturnValue();
        if (biome == null || !biome.is(HOSTS::contains)) {
            return;
        }
        if (CRYSTAL_WEIRDNESS.distance(sampler.sample(x, y, z).weirdness()) != 0) {
            return;
        }
        // 稀有化：按区块坐标确定性哈希，带内仅约 15% 的区块触发（整块变晶簇尖刺，保持块状大小而非缩小）
        if (!chunkPasses(x, z)) {
            return;
        }
        Holder<Biome> crystal = crystalHolder;
        if (crystal == null) {
            crystal = findCrystalHolder();
            if (crystal == null) {
                return; // 群系没在参数表里（非主世界/自定义世界）——不干预
            }
            crystalHolder = crystal;
        }
        cir.setReturnValue(crystal);
    }

    /** 稀有化判定：按区块坐标做确定性哈希（同区块结果一致 = 整块触发），固定盐值，约 15% 区块通过。 */
    private static boolean chunkPasses(int x, int z) {
        long hash = (long) (x >> 4) * 341873128712L + (long) (z >> 4) * 132897987541L;
        hash ^= hash >>> 13;
        hash *= 0x5bd1e995L;
        hash ^= hash >>> 15;
        return ((hash & 0x7FFFFFFFL) % 100) < 15;
    }

    /** 从主世界气候表里找 hexmob:crystal_spikes 的 Holder（由 OverworldBiomeBuilderMixin 注入的那一项）。 */
    private Holder<Biome> findCrystalHolder() {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> values = this.parameters().values();
        if (values == null) {
            return null;
        }
        for (Pair<Climate.ParameterPoint, Holder<Biome>> pair : values) {
            Holder<Biome> h = pair.getSecond();
            if (h.unwrapKey().map(k -> k.equals(CRYSTAL_SPIKES)).orElse(false)) {
                return h;
            }
        }
        return null;
    }
}
