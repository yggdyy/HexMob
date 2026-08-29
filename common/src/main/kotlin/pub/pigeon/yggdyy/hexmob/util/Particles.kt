package pub.pigeon.yggdyy.hexmob.util

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

/**
 * 服务端+客户端都能用的粒子生成工具。
 *
 * 坑：Minecraft 的 Level.addParticle(...) 在**服务端是空操作**（ServerLevel 不覆写它，
 * 基类字节码直接 return）。所以服务端代码调用 addParticle 永远不产生粒子！
 * 正确做法是：
 * - 客户端：addParticle 会被 ClientLevel 覆写为本地实际生成（精确位置/速度）；
 * - 服务端：手动构造 ClientboundLevelParticlesPacket（count=0，精确单粒子）广播给半径内玩家。
 */
fun spawnParticle(
    level: Level, type: ParticleOptions,
    x: Double, y: Double, z: Double,
    vx: Double, vy: Double, vz: Double
) {
    if (level.isClientSide) {
        level.addParticle(type, x, y, z, vx, vy, vz)
    } else if (level is ServerLevel) {
        // count=0：客户端按精确 (x,y,z) + 速度生成单个粒子；maxSpeed=1.0 使速度不衰减
        val packet = ClientboundLevelParticlesPacket(
            type, false,
            x, y, z,
            vx.toFloat(), vy.toFloat(), vz.toFloat(),
            1.0F, 0
        )
        for (p in level.players()) {
            if (p.distanceToSqr(x, y, z) < 4096.0) { // 64 格内
                p.connection.send(packet)
            }
        }
    }
}
