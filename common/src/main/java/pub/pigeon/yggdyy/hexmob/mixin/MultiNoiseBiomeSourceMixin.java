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
import pub.pigeon.yggdyy.hexmob.worldgen.CrystalSpikesHolder;

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
@Mixin(value = MultiNoiseBiomeSource.class, priority = 5000)
public abstract class MultiNoiseBiomeSourceMixin {
    private static final ResourceKey<Biome> CRYSTAL_SPIKES =
        ResourceKey.create(Registries.BIOME, new ResourceLocation("hexmob", "crystal_spikes"));
    /** 奇异度子带：晶簇尖刺散落在宿主格子的这一小段里（调这两个值 = 调出现频率/分布）。 */
    private static final Climate.Parameter CRYSTAL_WEIRDNESS = Climate.Parameter.span(-0.8F, -0.45F);
    /** 宿主群系：黑森林/沼泽这类湿润温带群系，晶簇尖刺与它们"一起生成"。 */
    private static final java.util.Set<ResourceKey<Biome>> HOSTS = java.util.Set.of(
        Biomes.DARK_FOREST, Biomes.SWAMP, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.FLOWER_FOREST
    );
    /** 诊断用一次性标记。 */
    private static boolean diagRun;
    private static boolean diagNotHost;
    private static boolean diagNoCrystal;
    private static boolean diagSet;
    private static boolean diagHead;

    static {
        // 用与 FabricHexMob 相同的 Log4j logger（pub.pigeon.yggdyy.hexmob.HexMob.LOGGER），
        // 确保诊断能进大包 latest.log。
        pub.pigeon.yggdyy.hexmob.HexMob.LOGGER.info("[MNB] MultiNoiseBiomeSourceMixin loaded");
    }

    private static void diag(String msg) {
        pub.pigeon.yggdyy.hexmob.HexMob.LOGGER.info("[MNB] " + msg);
    }

    @Inject(
        method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
        at = @At("HEAD")
    )
    private void hexmob$diagHead(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!diagHead) {
            diagHead = true;
            diag("getNoiseBiome(4-arg) CALLED, biomeSource class = " + this.getClass().getName());
        }
    }

    @Shadow
    private Climate.ParameterList<Holder<Biome>> parameters() {
        return null;
    }

    @Inject(
        // 全描述符精确命中 4 参 getNoiseBiome（name-only 会匹配到 getNoiseBiome(TargetPoint) 重载导致 dev 签名不匹配崩）。
        // ⚠ Loom refmap 无法映射这个重载名（类层级里 getNoiseBiome 有 3 个）+ Climate$ 嵌套类型，
        //   发布包（intermediary 命名）找不到目标会崩启动——由 scripts/patch-refmap.ps1 在装包时补 method_38109。
        method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void hexmob$scatterCrystalSpikes(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!diagRun) {
            diagRun = true;
            diag("scatter RETURN handler invoked; registryCached=" + (CrystalSpikesHolder.get() != null)
                + ", paramsHasCrystal=" + (findCrystalHolder() != null));
        }
        Holder<Biome> biome = cir.getReturnValue();
        if (biome == null || !biome.is(HOSTS::contains)) {
            if (!diagNotHost) {
                diagNotHost = true;
                diag("returned biome not a host: "
                    + (biome == null ? "null" : biome.unwrapKey().map(k -> k.location().toString()).orElse("no-key"))
                    + " (weird=" + sampler.sample(x, y, z).weirdness() + ")");
            }
            return;
        }
        if (CRYSTAL_WEIRDNESS.distance(sampler.sample(x, y, z).weirdness()) != 0) {
            return;
        }
        // 稀有化：按区块坐标确定性哈希，带内仅约 15% 的区块触发（整块变晶簇尖刺，保持块状大小而非缩小）
        if (!chunkPasses(x, z)) {
            return;
        }
        Holder<Biome> crystal = findCrystalHolder();
        if (crystal == null) {
            if (!diagNoCrystal) {
                diagNoCrystal = true;
                diag("host+weird+gate passed but NO crystal holder!");
            }
            return; // 群系不可用（非主世界/自定义世界）——不干预
        }
        cir.setReturnValue(crystal);
        // ⚠ priority=5000（高于 TerraBlender 的默认 1000）+ cancel：TerraBlender 也在 getNoiseBiome
        //   RETURN 上挂了 @Inject（会把它 region 的群系写回来/可能先改写宿主），必须在此停住后续 handler，
        //   确保晶簇尖刺判定必胜。非晶簇块我们早退不 cancel，TerraBlender 照常工作。
        cir.cancel();
        if (!diagSet) {
            diagSet = true;
            diag("crystal SET at chunk (" + (x >> 4) + "," + (z >> 4) + ")");
        }
    }

    /** 稀有化判定：按区块坐标做确定性哈希（同区块结果一致 = 整块触发），固定盐值，约 15% 区块通过。 */
    private static boolean chunkPasses(int x, int z) {
        long hash = (long) (x >> 4) * 341873128712L + (long) (z >> 4) * 132897987541L;
        hash ^= hash >>> 13;
        hash *= 0x5bd1e995L;
        hash ^= hash >>> 15;
        return ((hash & 0x7FFFFFFFL) % 100) < 15;
    }

    /**
     * 找 hexmob:crystal_spikes 的 Holder。
     * 首选 {@link CrystalSpikesHolder}（服务器启动时从世界动态注册表解析的缓存，
     * TerraBlender 会重建参数表、parameters() 里可能没有该项）；参数表扫描作为兜底
     * （覆盖 forge / 未触发缓存等路径，由 OverworldBiomeBuilderMixin 注入的那一项）。
     */
    private Holder<Biome> findCrystalHolder() {
        Holder<Biome> cached = CrystalSpikesHolder.get();
        if (cached != null) {
            return cached;
        }
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
