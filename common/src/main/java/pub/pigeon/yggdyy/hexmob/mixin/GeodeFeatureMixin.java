package pub.pigeon.yggdyy.hexmob.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig;
import pub.pigeon.yggdyy.hexmob.content.crying_amethyst.CryingAmethystEntity;
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntity;
import pub.pigeon.yggdyy.hexmob.registry.HexMobTags;

@Mixin(GeodeFeature.class)
public class GeodeFeatureMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void summonStimulatedPattern(FeaturePlaceContext<GeodeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            for(int i = -12; i <= 12; i += 4) {
                for(int j = -12; j <= 12; j += 4) {
                    for(int k = -12; k <= 12; k += 4) {
                        BlockPos now = context.origin().offset(i, j, k);
                        ServerLevelAccessor level = context.level();
                        if(level.getBlockState(now).isAir()) {
                            var tmp = now;
                            while(!level.isOutsideBuildHeight(tmp) && level.getBlockState(tmp).isAir()) tmp = tmp.below();
                            if(!level.getBlockState(tmp).is(HexMobTags.BlockTags.INSTANCE.getGEODE_WALL())) {
                                continue;
                            }
                            tmp = now;
                            while(!level.isOutsideBuildHeight(tmp) && level.getBlockState(tmp).isAir()) tmp = tmp.above();
                            if(!level.getBlockState(tmp).is(HexMobTags.BlockTags.INSTANCE.getGEODE_WALL())) {
                                continue;
                            }
                            if (context.random().nextDouble() <= HexMobServerConfig.getConfig().getStimulatedPatternSpawnRate()) {
                                StimulatedPatternEntity.Companion.spawnInGeode(context, now);
                            }
                            if (context.random().nextDouble() <= HexMobServerConfig.getConfig().getCryingAmethystSpawnRate()) {
                                CryingAmethystEntity.Companion.spawnInGeode(context, now);
                            }
                            return;
                        }
                    }
                }
            }

        }
    }
}
