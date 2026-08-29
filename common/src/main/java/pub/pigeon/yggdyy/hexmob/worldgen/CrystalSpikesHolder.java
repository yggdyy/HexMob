package pub.pigeon.yggdyy.hexmob.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/**
 * hexmob:crystal_spikes 的 Holder 缓存。
 *
 * <p>为什么需要它：TerraBlender 3.x 会重建主世界 MultiNoiseBiomeSource 的参数表
 * （其 MixinMultiNoiseBiomeSource/MixinParameterList + IExtendedParameterList 把
 * OverworldBiomeBuilderMixin 注入的 crystal_spikes 参数点挤掉），导致运行时
 * {@code parameters().values()} 里找不到该群系 → MultiNoiseBiomeSourceMixin 的
 * findCrystalHolder() 返回 null → 散射静默不触发、群系永不生成（dev 无 TB 正常、
 * 大包有 TB 时 /locate 报"无法在合理距离内找到"）。
 *
 * <p>解法：服务器启动时（ServerLifecycleEvents.SERVER_STARTING）从世界动态注册表
 * （Registry&lt;Biome&gt;，含 mod 数据包群系）直接解析 Holder 缓存到此处，生成时优先用它。
 * 数据包群系不在 BuiltInRegistries.BIOME（那是内置群系注册表），必须走世界 RegistryAccess。
 */
public final class CrystalSpikesHolder {
    private static Holder<Biome> holder;

    private CrystalSpikesHolder() {
    }

    public static void set(Holder<Biome> h) {
        if (h != null) {
            holder = h;
        }
    }

    public static Holder<Biome> get() {
        return holder;
    }
}
