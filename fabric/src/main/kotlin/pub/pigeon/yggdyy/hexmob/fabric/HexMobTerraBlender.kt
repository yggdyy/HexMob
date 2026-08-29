package pub.pigeon.yggdyy.hexmob.fabric

import terrablender.api.Regions
import terrablender.api.TerraBlenderApi

/**
 * TerraBlender 软依赖入口：fabric.mod.json 声明 "terrablender" entrypoint，
 * 只有 TerraBlender 存在并初始化时才会被调用（Fabric 只对声明了该 entrypoint 类型
 * 且该类型被某 mod 主动 invoke 的类进行加载）。TB 不在时本类永不加载，无副作用。
 */
object HexMobTerraBlender : TerraBlenderApi {
    override fun onTerraBlenderInitialized() {
        Regions.register(CrystalSpikesRegion())
    }
}
