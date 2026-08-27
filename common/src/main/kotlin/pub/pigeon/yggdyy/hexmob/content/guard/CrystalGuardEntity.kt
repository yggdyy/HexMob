package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import java.util.UUID

/**
 * 晶刺守卫基类：在新群系 crystal_spikes 里自然生成的人形敌对生物。
 *
 * 三种外观：弓箭守卫（像掠夺者）、斧头守卫（像卫道士）、傀儡守卫（像铁傀儡）。
 * 行为：默认站桩不动（主人未醒时 GuardIdleGateGoal 占住最高优先级，
 * 取消防御/停下移动，其余攻击/索敌 AI 全部不跑）；附近任一清醒的大环
 * （GUARD_AWAKE_RANGE 内）出现后，才开始移动与攻击玩家。
 * 基类用 Monster（Monster 是 PathfinderMob 子类）：
 * RangedCrossbowAttackGoal 要求 T extends Monster & RangedAttackMob & CrossbowAttackMob。
 */
abstract class CrystalGuardEntity(type: EntityType<out CrystalGuardEntity>, level: Level) :
    Monster(type, level), Enemy {

    private var masterAwakeCache = false
    private var masterAwakeCacheTick = -1

    /** 被大环召唤时的召唤者 UUID（用于计入召唤预算；自然生成/刷怪蛋为 null）。 */
    private var summonerUuid: UUID? = null

    /** 标记为大环召唤的守卫（计入该大环的下属数量）。 */
    fun setSummoner(circle: UrCircleEntity) {
        summonerUuid = circle.uuid
    }

    /** 是否为指定大环召唤的下属。 */
    fun isSummonedBy(circle: UrCircleEntity): Boolean = summonerUuid == circle.uuid

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        if (summonerUuid != null) tag.putUUID("hexmob:Summoner", summonerUuid!!)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        if (tag.contains("hexmob:Summoner")) summonerUuid = tag.getUUID("hexmob:Summoner")
    }

    /** 主人是否醒来：GUARD_AWAKE_RANGE 内是否有清醒（未沉睡）的大环。结果按 tick 缓存。 */
    fun isMasterAwake(): Boolean {
        if (tickCount != masterAwakeCacheTick) {
            masterAwakeCacheTick = tickCount
            val prev = masterAwakeCache
            masterAwakeCache = level()
                .getEntitiesOfClass(UrCircleEntity::class.java, boundingBox.inflate(GUARD_AWAKE_RANGE))
                .any { it.isAwake() }
            if (masterAwakeCache != prev) {
                HexMob.LOGGER.info("[Guard] {} 主人状态 -> {}", type.description.string, masterAwakeCache)
            }
        }
        return masterAwakeCache
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, GuardIdleGateGoal(this))
        // 醒着时巡逻游荡（沉睡时被 Gate 压住不跑），没玩家时也能看到守卫在动
        goalSelector.addGoal(7, RandomStrollGoal(this, 0.8))
        targetSelector.addGoal(1, GuardTargetGoal(this))
    }

    companion object {
        /** 守卫判定"主人醒来"的半径。 */
        const val GUARD_AWAKE_RANGE = 64.0
    }
}

/** 主人未醒时的站桩闸门：占住最高优先级，取消防御、停下移动，让其他 AI 全都不跑。 */
class GuardIdleGateGoal(private val guard: CrystalGuardEntity) : Goal() {
    override fun canUse(): Boolean = !guard.isMasterAwake()
    override fun canContinueToUse(): Boolean = !guard.isMasterAwake()
    override fun tick() {
        guard.target = null
        guard.navigation.stop()
    }
}

/** 索敌：主人醒着才盯玩家；不要求视线（隔地形也能锁），配合 32 格 follow range。 */
class GuardTargetGoal(guard: CrystalGuardEntity) : NearestAttackableTargetGoal<Player>(guard, Player::class.java, false) {
    override fun canUse(): Boolean = (mob as CrystalGuardEntity).isMasterAwake() && super.canUse()
    override fun canContinueToUse(): Boolean = (mob as CrystalGuardEntity).isMasterAwake() && super.canContinueToUse()
}
