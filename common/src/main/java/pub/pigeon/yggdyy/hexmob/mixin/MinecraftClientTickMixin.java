package pub.pigeon.yggdyy.hexmob.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.pigeon.yggdyy.hexmob.client.HexMobGaslightingTracker;

@Mixin(Minecraft.class)
public abstract class MinecraftClientTickMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void hexmob$gaslightTick(CallbackInfo ci) {
        if (!Minecraft.getInstance().isPaused()) {
            HexMobGaslightingTracker.INSTANCE.postFrameCheckRendered();
        }
    }
}