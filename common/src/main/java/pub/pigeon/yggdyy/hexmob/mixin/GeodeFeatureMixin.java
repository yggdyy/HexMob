package pub.pigeon.yggdyy.hexmob.mixin;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig;
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntity;

@Mixin(GeodeFeature.class)
public class GeodeFeatureMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void summonStimulatedPattern(FeaturePlaceContext<GeodeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue() && context.random().nextDouble() <= HexMobServerConfig.getConfig().getStimulatedPatternSpawnRate()) {
            StimulatedPatternEntity.Companion.spawnInGeode(context);
        }
    }
}
