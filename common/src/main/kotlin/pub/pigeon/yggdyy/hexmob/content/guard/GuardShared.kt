package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import java.util.UUID

/**
 * 守卫标记接口：被大环召唤过的守卫（计入大环下属数量，见 UrCircleEntity.livingServants）。
 */
interface HexMobGuard {
    fun setSummoner(circle: UrCircleEntity)
    fun isSummonedBy(circle: UrCircleEntity): Boolean
}

/**
 * 守卫通用大脑（组合而非继承）：唤醒扫描缓存 + 召唤者标记。
 * 原版基类（AbstractIllager / IronGolem）没法加方法，守卫自己的基类又因单继承无法统一，
 * 所以把共用逻辑放进 [GuardCore]，由 [GuardDormant] 接口以缺省实现暴露出去。
 */
class GuardCore {
    private var masterAwakeCache = false
    private var masterAwakeCacheTick = -1
    var summonerUuid: UUID? = null

    /** 主人是否醒来：GUARD_AWAKE_RANGE 内是否有清醒（未沉睡）的大环。结果按 tick 缓存。 */
    fun isMasterAwake(entity: Entity): Boolean {
        if (entity.tickCount != masterAwakeCacheTick) {
            masterAwakeCacheTick = entity.tickCount
            masterAwakeCache = findMaster(entity) != null
        }
        return masterAwakeCache
    }

    /** 最近一个清醒的大环（索敌/仇恨目标来源）。 */
    fun findMaster(entity: Entity): UrCircleEntity? =
        entity.level().getEntitiesOfClass(UrCircleEntity::class.java, entity.boundingBox.inflate(GUARD_AWAKE_RANGE))
            .firstOrNull { it.isAwake() }

    companion object {
        const val GUARD_AWAKE_RANGE = 64.0
        const val TAG_SUMMONER = "hexmob:Summoner"
    }
}

/**
 * 守卫通用行为接口：以缺省实现把 [GuardCore] 的能力带给每个守卫，
 * 同时兼容 AbstractIllager 系与 IronGolem 系。
 */
interface GuardDormant : HexMobGuard {
    val guardCore: GuardCore

    fun isMasterAwake(): Boolean = guardCore.isMasterAwake(this as Entity)
    fun findMaster(): UrCircleEntity? = guardCore.findMaster(this as Entity)

    override fun setSummoner(circle: UrCircleEntity) {
        guardCore.summonerUuid = circle.uuid
    }

    override fun isSummonedBy(circle: UrCircleEntity): Boolean = guardCore.summonerUuid == circle.uuid

    fun saveGuardNbt(tag: CompoundTag) {
        guardCore.summonerUuid?.let { tag.putUUID(GuardCore.TAG_SUMMONER, it) }
    }

    fun loadGuardNbt(tag: CompoundTag) {
        if (tag.contains(GuardCore.TAG_SUMMONER)) guardCore.summonerUuid = tag.getUUID(GuardCore.TAG_SUMMONER)
    }

    /** 是否为守卫内部的"友军伤害"（板岩兵/弩手/傀儡互不打；弩箭经弹射物来源也识别）。 */
    fun isAllyAttack(source: DamageSource): Boolean {
        val direct = source.directEntity
        if (direct === this) return false
        if (direct is HexMobGuard) return true
        if (direct is Projectile && (direct.owner as? HexMobGuard)?.let { it !== this } == true) return true
        return false
    }

    /** 召唤时补发武器/装备：大环召唤走 [EntityType.create] 不经 finalizeSpawn，守卫默认裸手，需单独发装。 */
    fun equipOnSummon() {}
}

/** 主人未醒时的站桩闸门：占住最高优先级，取消目标、停下移动，让其他 AI 全都不跑。 */
class GuardIdleGateGoal(private val mob: Mob, private val awake: () -> Boolean) : Goal() {
    override fun canUse(): Boolean = !awake()
    override fun canContinueToUse(): Boolean = !awake()
    override fun tick() {
        mob.target = null
        mob.navigation.stop()
    }
}

/** 索敌（优先级 2）：主人醒着才盯玩家；不要求视线（隔地形也能锁）。 */
class GuardTargetGoal(
    private val mob: Mob,
    private val awake: () -> Boolean
) : NearestAttackableTargetGoal<Player>(mob, Player::class.java, false) {
    override fun canUse(): Boolean = awake() && super.canUse()
    override fun canContinueToUse(): Boolean = awake() && super.canContinueToUse()
}

/**
 * 索敌（优先级 1）：**就近支援大环的战斗**——优先打大环仇恨列表里离自己最近的敌人，
 * 仇恨列表空时才退回大环当前主目标（兜底）。
 */
class GuardMasterHateGoal(
    private val mob: PathfinderMob,
    private val awake: () -> Boolean,
    private val master: () -> UrCircleEntity?
) : TargetGoal(mob, false, false) {

    override fun canUse(): Boolean = awake() && pick() != null

    override fun canContinueToUse(): Boolean = awake() && super.canContinueToUse()

    override fun start() {
        mob.target = pick()
    }

    private fun pick(): LivingEntity? {
        val m = master() ?: return null
        // 优先：大环仇恨列表里离自己最近的（就近支援，不追远）
        val nearest = m.currentHated()
            .filter { it.isAlive && mob.distanceToSqr(it) <= HATE_RANGE_SQ }
            .minByOrNull { mob.distanceToSqr(it) }
        if (nearest != null) return nearest
        // 兜底：大环当前主目标（通常也在仇恨列表里，列表空时才走到这）
        val primary = m.target
        if (primary != null && primary.isAlive && mob.distanceToSqr(primary) <= HATE_RANGE_SQ) {
            return primary
        }
        return null
    }

    private companion object {
        val HATE_RANGE_SQ = UrCircleEntity.HATE_RANGE * UrCircleEntity.HATE_RANGE
    }
}

/** 索敌（优先级 3）：受伤反击（原版 HurtByTargetGoal），仅唤醒时生效。 */
class GuardHurtByTargetGoal(
    mob: PathfinderMob,
    private val awake: () -> Boolean
) : HurtByTargetGoal(mob) {
    override fun canUse(): Boolean =
        awake() && super.canUse() && mob.lastHurtByMob !is HexMobGuard

    override fun canContinueToUse(): Boolean = awake() && super.canContinueToUse()
}