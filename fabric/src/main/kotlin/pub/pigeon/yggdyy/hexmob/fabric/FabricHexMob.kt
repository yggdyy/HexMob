package pub.pigeon.yggdyy.hexmob.fabric

import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.registry.HexMobActions
import pub.pigeon.yggdyy.hexmob.worldgen.CrystalSpikesHolder
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

object FabricHexMob : ModInitializer {
    override fun onInitialize() {
        HexMob.init()
        initRegistry(HexMobActions)
        // 群系 Holder 缓存：TerraBlender 会重建主世界参数表挤掉注入点，不能依赖参数表，
        // 改在服务器启动时从世界动态注册表（含 mod 数据包群系）直接解析 hexmob:crystal_spikes。
        // 单机（集成服务器）也会触发 SERVER_STARTING，生成时优先取这里缓存的 Holder。
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            val opt = server.registryAccess()
                .registry(Registries.BIOME)
                .flatMap { it.getHolder(ResourceKey.create(Registries.BIOME, ResourceLocation("hexmob", "crystal_spikes"))) }
            if (opt.isPresent) {
                CrystalSpikesHolder.set(opt.get())
                HexMob.LOGGER.info("[CrystalSpikes] holder cached from registry at server start")
            } else {
                HexMob.LOGGER.warn(
                    "[CrystalSpikes] holder NOT found at server start (biome registry present: {})",
                    server.registryAccess().registry(Registries.BIOME).isPresent
                )
            }
        }
    }
}
