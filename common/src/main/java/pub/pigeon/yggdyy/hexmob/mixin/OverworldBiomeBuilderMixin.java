package pub.pigeon.yggdyy.hexmob.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * 把 hexmob:crystal_spikes（晶簇尖刺）群系加入主世界生成：
 * 注入 OverworldBiomeBuilder.addBiomes 末尾，放在温带内陆 + 高奇异度稀有带
 * （与常态群系错开的独立分布），地表由覆盖的 surface rule 铺深板岩。
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
    private static final ResourceKey<Biome> CRYSTAL_SPIKES =
        ResourceKey.create(Registries.BIOME, new ResourceLocation("hexmob", "crystal_spikes"));

    @Shadow
    private void addSurfaceBiome(
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
        Climate.Parameter temperature,
        Climate.Parameter humidity,
        Climate.Parameter continentalness,
        Climate.Parameter erosion,
        Climate.Parameter weirdness,
        float offset,
        ResourceKey<Biome> biome
    ) {
    }

    @Inject(method = "addBiomes", at = @At("RETURN"))
    private void hexmob$addCrystalSpikes(
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
        CallbackInfo ci
    ) {
        this.addSurfaceBiome(consumer,
            Climate.Parameter.span(0.4F, 0.9F),                 // 温度：温带（地表由 surface rule 变深板岩，不再是雪）
            Climate.Parameter.span(0.3F, 1.0F),                 // 湿度：湿润
            Climate.Parameter.span(-0.11F, 0.55F),              // 大陆度：内陆
            Climate.Parameter.span(-0.05F, 0.05F),              // 侵蚀：侵蚀2
            Climate.Parameter.span(0.9333333333333333F, 1.0F),  // 奇异度：高切片上升（与常态群系错开的稀有带）
            0.0F,
            CRYSTAL_SPIKES
        );
    }
}
