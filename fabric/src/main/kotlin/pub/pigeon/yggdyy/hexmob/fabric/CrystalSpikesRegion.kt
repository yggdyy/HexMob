package pub.pigeon.yggdyy.hexmob.fabric

import com.mojang.datafixers.util.Pair
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Climate
import terrablender.api.Region
import terrablender.api.RegionType
import java.util.function.Consumer

/**
 * TerraBlender 存在时的 crystal_spikes 注册（软依赖）。
 *
 * 为什么需要它：TerraBlender 3.x 会重建主世界 MultiNoiseBiomeSource 的参数表，
 * 且其 getNoiseBiome 选择路径不再走我们散射 mixin 所注入的
 * MultiNoiseBiomeSource.getNoiseBiome(int,int,int,Sampler) → 散射在大包中完全不触发。
 * 有 TerraBlender 时改用它自己的 Region 机制把群系加进主世界。
 *
 * 分布（与用户商定的方案 A）：温带湿润气候 + 奇异度高切片 0.9333~1.0
 * （原版几乎没群系的窄带）→ 群系在窄带里成片出现，带本身窄所以总量仍稀少。
 * TerraBlender 走"参数点最近裁决"（RTree/findValuePositional），做不了散落的
 * 15% 区块哈希门控；没有 TB 的环境（dev/普通 Fabric）仍由 MultiNoiseBiomeSourceMixin 散射负责。
 */
class CrystalSpikesRegion : Region(
    ResourceLocation("hexmob", "crystal_spikes_region"),
    RegionType.OVERWORLD,
    2
) {
    override fun addBiomes(
        registry: Registry<Biome>,
        consumer: Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>>
    ) {
        // 参数顺序（TB Region.addBiome 原始参数版）：温度/湿度/大陆度/侵蚀/奇异度/深度/offset
        // 收窄版（2026-08-27 用户反馈软依赖 TB 时覆盖过大）：温度 0.5~0.65、湿度 0.55~0.8、
        // 大陆度 -0.11~0.1（不过海）、侵蚀 0±0.05、奇异度高切片 0.9333~1.0。
        // 参数框缩小后群系patch显著变小变稀；仍嫌大/嫌小可再调这几个 span。
        addBiome(
            consumer,
            Climate.Parameter.span(0.5f, 0.65f),
            Climate.Parameter.span(0.55f, 0.8f),
            Climate.Parameter.span(-0.11f, 0.1f),
            Climate.Parameter.span(-0.05f, 0.05f),
            Climate.Parameter.span(0.9333333333333333f, 1.0f),
            Climate.Parameter.span(-1.0f, 1.0f),
            0.0f,
            ResourceKey.create(Registries.BIOME, ResourceLocation("hexmob", "crystal_spikes"))
        )
    }
}
