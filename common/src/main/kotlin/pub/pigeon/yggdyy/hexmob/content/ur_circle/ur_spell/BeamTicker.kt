package pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.util.spawnParticle

/**
 * 【光束调度器】服务端每 tick 驱动 Core Beam 法术的持续光束：
 * 施法者→目标 的粒子线 + 周期伤害（照大环光炮：直击必中 + 命中点 AoE 半伤）。
 *
 * 挂载：fabric 端 ServerTickEvents.END_SERVER_TICK → [tickAll]（见 FabricHexMob）。
 * 目标死亡/施法者离开/倒计时结束即自动移除。
 */
object BeamTicker {
    private val beams = ArrayList<Beam>()

    private class Beam(
        val level: ServerLevel,
        val attacker: LivingEntity,
        val target: LivingEntity,
        var ticksLeft: Int,
    )

    /** 启动一束持续光束（durationTicks>0）。 */
    fun launch(level: ServerLevel, attacker: LivingEntity, target: LivingEntity, durationTicks: Int) {
        beams += Beam(level, attacker, target, durationTicks.coerceAtLeast(1))
    }

    fun tickAll(server: MinecraftServer) {
        val it = beams.iterator()
        while (it.hasNext()) {
            val b = it.next()
            if (b.level.server !== server || b.level.isClientSide) {
                it.remove()
                continue
            }
            if (!b.attacker.isAlive || b.attacker.level() !== b.level || !b.target.isAlive) {
                it.remove()
                continue
            }
            b.ticksLeft--
            if (b.ticksLeft <= 0) {
                it.remove()
                continue
            }
            tickBeam(b)
        }
    }

    private fun tickBeam(b: Beam) {
        val level = b.level
        val origin = b.attacker.getEyePosition()
        val end = b.target.position().add(0.0, b.target.bbHeight / 2.0, 0.0)

        // 沿 施法者→目标 的连线铺粒子（光束外观，跟随目标）
        val delta = end.subtract(origin)
        val len = delta.length()
        if (len > 0.01) {
            val dir = delta.scale(1.0 / len)
            val steps = (len * 2).toInt().coerceIn(6, 32)
            for (i in 0 until steps) {
                val along = origin.add(dir.scale(len * i / steps))
                spawnParticle(level, ParticleTypes.ENCHANT, along.x, along.y, along.z, 0.0, 0.0, 0.0)
                spawnParticle(level, ParticleTypes.ELECTRIC_SPARK, along.x, along.y, along.z, 0.0, 0.0, 0.0)
            }
        }

        // 周期伤害（直击必中 + AoE）
        if (b.ticksLeft % DAMAGE_INTERVAL == 0) {
            val source = level.damageSources().mobAttack(b.attacker)
            b.target.hurt(source, beamDamageFor(b.target))
            val aoe = AABB(end, end).inflate(AOE_RADIUS)
            for (other in level.getEntitiesOfClass(LivingEntity::class.java, aoe)) {
                if (other === b.target || other is Enemy) continue
                other.hurt(source, beamDamageFor(other) * 0.5F)
            }
        }
    }

    private fun beamDamageFor(victim: LivingEntity): Float =
        if (victim.getMaxHealth() > PERCENT_HEALTH_THRESHOLD) victim.getMaxHealth() * PERCENT_DAMAGE
        else FIXED_DAMAGE

    private const val DAMAGE_INTERVAL = 2
    const val AOE_RADIUS = 2.5
    const val FIXED_DAMAGE = 8.0F
    const val PERCENT_HEALTH_THRESHOLD = 100.0F
    const val PERCENT_DAMAGE = 0.2F
}