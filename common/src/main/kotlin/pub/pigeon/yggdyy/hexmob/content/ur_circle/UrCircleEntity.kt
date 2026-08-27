package pub.pigeon.yggdyy.hexmob.content.ur_circle

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexDamageTypes
import at.petrak.hexcasting.common.lib.HexParticles
import net.minecraft.sounds.SoundEvent
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.BossEvent
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.api.entity.FlickeringEntity
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import pub.pigeon.yggdyy.hexmob.content.IHMMultipartEntity
import pub.pigeon.yggdyy.hexmob.content.guard.CrystalGuardEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities.CubePart
import pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities.SlatePart
import pub.pigeon.yggdyy.hexmob.content.ur_circle.servant.UrCircleServant
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.AmethystTrapSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.BeamSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.CurseSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.EruptSerpentSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.ExplosionSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.HexMobBacklash
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.LightningSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.MishapSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.RecoverSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.RingSpinSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.SerpentSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.SummonServantSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.UrCircleMishap
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.UrCircleSkill
import pub.pigeon.yggdyy.hexmob.content.ur_circle.spells.UrCircleStatusTable
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities
import pub.pigeon.yggdyy.hexmob.registry.HexMobItems
import pub.pigeon.yggdyy.hexmob.util.rotateDA
import pub.pigeon.yggdyy.hexmob.util.spawnParticle
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

class UrCircleEntity(entityType: EntityType<out Mob>, level: Level) : Mob(entityType, level), Enemy,
    IHMMultipartEntity<UrCirclePart>, FlickeringEntity {
    val equator: MutableList<UrCirclePart> = mutableListOf(
        CubePart(this, HexAPI.modLoc("impetus/empty"), "energized=false,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/look"), "energized=false,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/redstone"), "energized=false,facing=south,powered=false"),
        CubePart(this, HexAPI.modLoc("impetus/rightclick"), "energized=false,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/empty"), "energized=true,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/look"), "energized=true,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/redstone"), "energized=true,facing=south,powered=false"),
        CubePart(this, HexAPI.modLoc("impetus/rightclick"), "energized=true,facing=south"),
        CubePart(this, HexAPI.modLoc("directrix/empty"), "energized=false,facing=south"),
        CubePart(this, HexAPI.modLoc("directrix/boolean"), "energized=false,facing=south,state=false"),
        CubePart(this, HexAPI.modLoc("directrix/redstone"), "energized=false,facing=south,powered=false"),
        CubePart(this, HexAPI.modLoc("directrix/empty"), "energized=true,facing=south"),
        CubePart(this, HexAPI.modLoc("directrix/boolean"), "energized=true,facing=south,state=false"),
        CubePart(this, HexAPI.modLoc("directrix/redstone"), "energized=true,facing=south,powered=false"),
    )
    val ecliptic: MutableList<UrCirclePart> = mutableListOf(
        SlatePart(this, HexPattern.fromAnglesUnchecked("eqawwwwqqaw", HexDir.SOUTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("e", HexDir.EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("waqw", HexDir.SOUTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("wedw", HexDir.NORTH_WEST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("qsq", HexDir.NORTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("aawqqeee", HexDir.NORTH_WEST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("qqaeqwaeswqwq", HexDir.NORTH_WEST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("qaq", HexDir.NORTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("wawwawwewwqsq", HexDir.WEST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("qaqwqaaswa", HexDir.NORTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("wqadaqw", HexDir.NORTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("eqqwqwqeda", HexDir.SOUTH_EAST)),
    )
    /** 核心（地球）方块部件：模型指向 hexmob:block/ur_circle_core（隐藏技术方块提供模型，贴图 ur_circle_core.png）。 */
    val earth: CubePart = CubePart(this, HexMob.id("cube"), 2F, 2F, HexMob.id("ur_circle_core"), "")
    var equatorRadius: Vec3
        get() = Vec3(entityData.get(EQUATOR_RADIUS))
        set(value) = entityData.set(EQUATOR_RADIUS, value.toVector3f())
    var equatorNormal: Vec3
        get() = Vec3(entityData.get(EQUATOR_NORMAL))
        set(value) = entityData.set(EQUATOR_NORMAL, value.toVector3f())
    var equatorRotation: Float
        get() = entityData.get(EQUATOR_ROTATION)
        set(value) = entityData.set(EQUATOR_ROTATION, value)
    var eclipticRadius: Vec3
        get() = Vec3(entityData.get(ECLIPTIC_RADIUS))
        set(value) = entityData.set(ECLIPTIC_RADIUS, value.toVector3f())
    var eclipticNormal: Vec3
        get() = Vec3(entityData.get(ECLIPTIC_NORMAL))
        set(value) = entityData.set(ECLIPTIC_NORMAL, value.toVector3f())
    var eclipticRotation: Float
        get() = entityData.get(ECLIPTIC_ROTATION)
        set(value) = entityData.set(ECLIPTIC_ROTATION, value)
    var earthRadius: Vec3
        get() = Vec3(entityData.get(EARTH_RADIUS))
        set(value) = entityData.set(EARTH_RADIUS, value.toVector3f())
    var earthNormal: Vec3
        get() = Vec3(entityData.get(EARTH_NORMAL))
        set(value) = entityData.set(EARTH_NORMAL, value.toVector3f())
    var earthRotation: Float
        get() = entityData.get(EARTH_ROTATION)
        set(value) = entityData.set(EARTH_ROTATION, value)
    // 战斗状态机（默认巡航；后续招式状态由冲撞/光束驱动），同步到客户端供渲染使用
    var circleState: CircleState
        get() = CircleState.byId(entityData.get(STATE))
        set(value) = entityData.set(STATE, value.ordinal)
    var stateTicks: Int
        get() = entityData.get(STATE_TICKS)
        set(value) = entityData.set(STATE_TICKS, value)
    /** 出生点：无目标时巡航回这里。仅服务器端。 */
    var homePos: Vec3? = null
    /** 碰撞攻击冷却表：UUID -> 剩余 tick（同一受害者被轮盘碾过后的喘息时间）。 */
    private val contactCooldowns: MutableMap<UUID, Int> = HashMap()
    /** 石板弹发射冷却与轮换指针。 */
    private var fireCooldown = 0
    private var slateIndex = 0
    /** 冲撞：冷却计时与锁定的冲刺方向。 */
    private var chargeCooldown = 0
    private var chargeDir = Vec3.ZERO
    /** 贴地破坏冷却：防止轮盘每 tick 刷坑刷音效。 */
    private var groundCraterCooldown = 0
    /** 巡航空闲自动补下属的间隔计时。 */
    private var servantSummonCooldown = 0
    /** Boss 血条（仅服务器端）：凋灵式，随距离加入/移除玩家。 */
    private var bossEvent: ServerBossEvent? = null
    /** 沉睡标记（仅服务器端，随 NBT 持久化）：结构召唤的大环初始沉睡——不索敌/不动/不发技能/不显示血条/无敌，
     *  玩家进入 [WAKE_RANGE] 格才苏醒。一旦苏醒不再沉睡。守卫怪据此判断"主人是否醒来"。 */
    private var dormant = false
    /** 结构召唤自查次数：实体化后的前若干 tick 内查一次 ur_circle_arena。 */
    private var arenaCheckAttempts = 0
    /** 是否已被玩家唤醒过（唤醒后不再被结构自查重新入睡）。 */
    private var wokenOnce = false
    /** setHealth 免疫用的内部写血标记：仅在自身 hurt 流程/读档时置真。 */
    private var internalHealthWrite = false
    // ---- 限伤 / 受伤额外行为（服务端瞬态） ----
    /** DPS 窗口限伤：当前窗口已累计伤害。 */
    private var dpsWindow = 0.0F
    /** DPS 窗口起点（tickCount）。 */
    private var dpsWindowStart = 0
    /** 过载累计（窗口内实际受伤害）。 */
    private var overloadDamage = 0.0F
    /** 过载窗口起点（tickCount）。 */
    private var overloadWindowStart = 0
    /** 过载反击冷却。 */
    private var overloadCooldown = 0
    /** 蓄力技能列表：吟唱冷却好时在"满足 canUse 的技能"里按权重随机抽一个进入 CHANNELING
     *  （见 [pickWeightedSkill]；权重见各技能 weight）。 */
    val skills: MutableList<UrCircleSkill> = mutableListOf(
        SummonServantSkill(),
        ExplosionSkill(),
        CurseSkill(),
        MishapSkill(),
        AmethystTrapSkill(),
        LightningSkill(),
        RecoverSkill(),
        BeamSkill(),
        SerpentSkill(),
        RingSpinSkill(),
        EruptSerpentSkill()
    )
    /** 当前正在吟唱的技能。 */
    private var currentSkill: UrCircleSkill? = null
    /** 当前正在释放光束的技能（BEAM 状态）。 */
    var beamSkill: UrCircleSkill? = null
    /** 吟唱音效脉冲计时。 */
    private var channelPulseTimer = 0
    /** 吟唱粒子脉冲计时（每几 tick 一波）。 */
    private var channelParticleTimer = 0
    /** 技能冷却：释放后一段时间不再进入吟唱。 */
    private var skillCooldown = 0
    /** 反向过度施法：玩家施法积累的反噬值（服务端瞬态，不存档）。 */
    private var backlash = 0
    /** 反噬节流：上次积累的 tick，防止同一施法被多次触发。 */
    private var lastBacklashTick = 0
    /** 仇恨实体列表（服务端瞬态）：大环可同时对多个实体保持仇恨；
     *  自爆/紫水晶/闪电等技能同时作用于列表内所有目标（多目标攻击）。
     *  受击来源与索敌目标都会加入。 */
    private val hatedTargets: MutableList<LivingEntity> = mutableListOf()
    init {
        noPhysics = true
        noCulling = true
        IHMMultipartEntity.instances.add(this)
    }

    // 索敌：周期锁定附近"有智慧"的生物（hexmob:wise tag），见 UrCircleTargetGoal。
    // 移动：自定义飞行控制器（恶魂式悬浮）+ 巡航 Goal，行为走 Mob 标准 AI 管线。
    override fun registerGoals() {
        moveControl = UrCircleMoveControl(this)
        targetSelector.addGoal(1, UrCircleTargetGoal(this))
        goalSelector.addGoal(1, UrCircleCruiseGoal(this))
    }
    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(EQUATOR_RADIUS, Vector3f(4F, 0F, 0F))
        entityData.define(EQUATOR_NORMAL, Vector3f(0F, 0.917F, -0.399F))
        entityData.define(EQUATOR_ROTATION, 0F)
        entityData.define(ECLIPTIC_RADIUS, Vector3f(6F, 0F, 0F))
        entityData.define(ECLIPTIC_NORMAL, Vector3f(0F, 0.917F, 0.399F))
        entityData.define(ECLIPTIC_ROTATION, 0F)
        entityData.define(EARTH_RADIUS, Vector3f(0.1F, 0F, 0F))
        entityData.define(EARTH_NORMAL, Vector3f(0F, 1F, 0F))
        entityData.define(EARTH_ROTATION, 0F)
        entityData.define(STATE, CircleState.CRUISE.ordinal)
        entityData.define(STATE_TICKS, 0)
        entityData.define(TUMBLING, false)
        entityData.define(RING_SPINNING, false)
    }
    override fun aiStep() {
        super.aiStep()
        // 先更新部件位置，让战斗逻辑（碰撞/发射）使用当 tick 的石板位置
        updatePartsPos()
        if (!level().isClientSide) {
            if (homePos == null) homePos = position()
            checkArenaDormancy()
            if (dormant) {
                // 沉睡：不索敌/不动/不发技能/不显示血条；玩家靠近 24 格唤醒
                tryWakeFromDormancy()
                updateBossBar()
                return
            }
            stateTicks += 1
            // 维护仇恨列表：清理死亡/超距/敌对的，无 target 时自动锁最优目标（先玩家、再剩余血量最高）
            hatedTargets.removeAll { !it.isAlive || it.isRemoved || it is Enemy || distanceToSqr(it) > HATE_RANGE * HATE_RANGE }
            if (target == null) {
                target = preferredTargets(currentHated()).firstOrNull()
            }
            combatBrain()
            // 死亡演出期间只播演出，不碰撞/不发射/不啃地/不播常态环境声
            if (circleState != CircleState.DYING) {
                idleBehavior()
                hurtContacts()
                tryFireSlate()
                groundContact()
                // 常态环境声：法术环吟唱（约每 5 秒一次）
                if (tickCount % AMBIENT_SOUND_INTERVAL == 0) {
                    level().playSound(null, blockPosition(), HexSounds.CASTING_AMBIANCE, SoundSource.HOSTILE, 1.2F, 1.0F)
                }
            }
            updateBossBar()
        }
        updateShape()
    }
    /**
     * 怒气/怒意因子（基础，未阻尼）：
     * - 索敌时 ×2.5；
     * - 生命越低越大：满血 1.0 → 半血 1.75 → 1/4 血 2.125。
     * 最高约 5.3，仅作为各行为因子的"原始怒气"。
     */
    fun currentAnger(): Float {
        val t = target
        val tf = if (t != null && t.isAlive) 2.5F else 1.0F
        val hpRatio = (health / maxHealth).coerceIn(0.0F, 1.0F)
        return tf * (1.0F + (1.0F - hpRatio) * 1.5F)
    }
    /** 旋转速度因子：随怒气增强最猛——索敌/低血时转得明显更快。 */
    fun rotationSpeedFactor(): Float = 1.0F + (currentAnger() - 1.0F) * 1.5F
    /** 死亡演出的转速倍率（纯 stateTicks 函数，客户端同公式推导）：先加快后减慢——0..SPIN_UP 1→3 倍，SPIN_UP..SPIN_DOWN_END 3→0，之后停。 */
    fun deathSpinFactor(): Float {
        val t = stateTicks.toFloat()
        return when {
            t <= SPIN_UP_TICKS -> 1.0F + (t / SPIN_UP_TICKS) * 2.0F
            t <= SPIN_DOWN_END -> {
                val p = (t - SPIN_UP_TICKS) / (SPIN_DOWN_END - SPIN_UP_TICKS).toFloat()
                3.0F * (1.0F - p)
            }
            else -> 0.0F
        }
    }

    /** 环刃风暴的吟唱进度（0..1）：RING_SPINNING 同步标志 + stateTicks 确定性推导（客户端同公式）。 */
    fun ringSpinProgress(): Float {
        if (!entityData.get(RING_SPINNING)) return 0.0F
        return (stateTicks.toFloat() / RingSpinSkill.CHANNEL_TICKS).coerceIn(0.0F, 1.0F)
    }

    /** 环刃风暴的部件/半径放大倍率：1 → RING_SPIN_MAX_SCALE（渲染与命中判定共用）。 */
    fun ringSpinScale(): Float = 1.0F + ringSpinProgress() * (RING_SPIN_MAX_SCALE - 1.0F)
    /** 总放大倍率 = 服务端配置的体积倍率 × 环刃风暴倍率（渲染/半径/碰撞共用，两端同值：客户端读 S2C 同步的配置）。 */
    fun totalScale(): Float = HexMobServerConfig.config.urCircleScale * ringSpinScale()

    /** 环外圈半径（格）：外圈石板到中心的距离 × 总放大倍率——飞行回避/部件不穿墙用。 */
    fun ringOuterRadius(): Double = (eclipticRadius.length() * totalScale()).toDouble()
    /** 环内圈半径（格）：内圈促动石到中心的距离 × 总放大倍率——飞行回避也采内圈。 */
    fun ringInnerRadius(): Double = (equatorRadius.length() * totalScale()).toDouble()

    /**
     * 部件回避：返回一个"环整体不穿墙"的飞行方向（避免任何部件卡进方块，不只是核心）。
     * 优先级：目标方向 → 抬升爬高 → 水平左右偏转（含微抬） → 反向退后 → 直接爬升；
     * 当前已卡住时"全力爬升 / 反向退后"优先脱困。
     * 每个候选做"半程 + 终点"两点探测，降低路径中途穿入；**全堵返回 null**（调用方减速悬停，不硬撞）。
     */
    fun safeFlightDir(desired: Vec3, lookAhead: Double): Vec3? {
        val rOuter = ringOuterRadius() + RING_CLEAR_MARGIN
        val rInner = ringInnerRadius() + RING_CLEAR_MARGIN
        val origin = position().add(0.0, bbHeight / 2.0, 0.0)
        val currentlyClear = ringClear(level(), origin, rOuter, rInner)
        val candidates = ArrayList<Vec3>()
        if (!currentlyClear) {
            // 已卡住：全力爬升 / 反向退后 优先脱困
            candidates.add(Vec3(0.0, 1.0, 0.0))
            candidates.add(desired.scale(-1.0).normalize())
            candidates.add(Vec3(desired.x, desired.y + 0.5, desired.z).normalize())
        }
        candidates.add(desired.normalize())
        candidates.add(Vec3(desired.x, desired.y + 0.35, desired.z).normalize())
        candidates.add(desired.yRot(0.6F))
        candidates.add(desired.yRot(-0.6F))
        candidates.add(Vec3(desired.yRot(0.6F).x, desired.y + 0.2, desired.yRot(0.6F).z).normalize())
        candidates.add(Vec3(desired.yRot(-0.6F).x, desired.y + 0.2, desired.yRot(-0.6F).z).normalize())
        candidates.add(desired.scale(-1.0).normalize())
        candidates.add(Vec3(0.0, 1.0, 0.0))
        for (c in candidates) {
            val mid = origin.add(c.scale(lookAhead * 0.5))
            val end = origin.add(c.scale(lookAhead))
            if (ringClear(level(), mid, rOuter, rInner) && ringClear(level(), end, rOuter, rInner)) return c
        }
        return null // 全堵：调用方减速悬停，不硬撞穿模
    }

    /** 环盘采样（外圈 [RING_CLEAR_SAMPLES] 点 + 内圈 [RING_INNER_SAMPLES] 点 + 中心，垂直覆盖随半径缩放）：
     *  全部不与方块碰撞才返回 true。黄道面是倾斜的（±半径×sin23°），垂直覆盖 = 外圈半径 × [RING_THICKNESS_RATIO]。 */
    private fun ringClear(level: Level, pos: Vec3, rOuter: Double, rInner: Double): Boolean {
        val thick = (rOuter * RING_THICKNESS_RATIO).toInt().coerceIn(MIN_RING_THICKNESS, MAX_RING_THICKNESS)
        for (k in 0 until RING_CLEAR_SAMPLES) {
            val a = Math.PI * 2.0 * k / RING_CLEAR_SAMPLES
            val sx = pos.x + cos(a) * rOuter
            val sz = pos.z + sin(a) * rOuter
            for (dy in -thick..thick) {
                val py = pos.y + dy
                if (!level.noCollision(AABB(sx - 0.05, py - 0.05, sz - 0.05, sx + 0.05, py + 0.05, sz + 0.05))) return false
            }
        }
        for (k in 0 until RING_INNER_SAMPLES) {
            val a = Math.PI * 2.0 * k / RING_INNER_SAMPLES
            val sx = pos.x + cos(a) * rInner
            val sz = pos.z + sin(a) * rInner
            for (dy in -thick..thick) {
                val py = pos.y + dy
                if (!level.noCollision(AABB(sx - 0.05, py - 0.05, sz - 0.05, sx + 0.05, py + 0.05, sz + 0.05))) return false
            }
        }
        return level.noCollision(AABB(pos.x - 0.5, pos.y - 0.5, pos.z - 0.5, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5))
    }
    /** 移动速度因子：强烈阻尼，移速基本保持稳定。 */
    fun moveSpeedFactor(): Float = 1.0F + (currentAnger() - 1.0F) * 0.3F
    /** 射速因子：中等阻尼，射速可以稍快但不夸张。 */
    fun fireRateFactor(): Float = 1.0F + (currentAnger() - 1.0F) * 0.6F
    fun updateShape() {
        if(!level().isClientSide) {
            if (dormant) return // 沉睡：停转（环静止悬浮）
            val base = rotationSpeedFactor()
            // 蓄力吟唱：转速随时间线性衰减，完全停下的瞬间释放技能
            val rot = if (isProtected()) {
                // 保护状态（血量>90% 且有下属）：停转
                0.0F
            } else if (circleState == CircleState.CHANNELING && currentSkill is RingSpinSkill) {
                // 环刃风暴：转速不衰减反而飙升（1→3 倍），环像绞肉机一样转起来
                base * (1.0F + ringSpinProgress() * 2.0F)
            } else if (circleState == CircleState.CHANNELING) {
                val duration = currentSkill?.channelTicks ?: 1
                val progress = (stateTicks.toFloat() / duration).coerceIn(0.0F, 1.0F)
                base * (1.0F - progress)
            } else if (circleState == CircleState.DYING) {
                // 死亡演出：转速先加快后减慢（1→3→0），同时赤道/黄道面各自绕半径轴翻滚
                base * deathSpinFactor()
            } else {
                base
            }
            equatorRotation += -1 * rot
            eclipticRotation += 1 * rot
            // 可打断技能吟唱 / 死亡演出时置真：赤道/黄道面绕各自半径轴旋转（updatePartsPos 读取）
            val tumbling = circleState == CircleState.CHANNELING && currentSkill?.channelInterruptible == true
                || circleState == CircleState.DYING
            entityData.set(TUMBLING, tumbling)
            // 环刃风暴：置同步标志（客户端据此推导部件/半径放大渲染）
            val ringSpinning = circleState == CircleState.CHANNELING && currentSkill is RingSpinSkill
            entityData.set(RING_SPINNING, ringSpinning)
        }
    }
    fun updatePartsPos() {
        val origin: Vec3 = position().add(0.0, bbHeight / 2.0, 0.0)
        earth.changeState(origin, if(target != null) target!!.position().subtract(origin).normalize() else Vec3(0.0, 0.0, 1.0))
        // 可打断技能吟唱 / 死亡演出：赤道/黄道面各自绕自己的半径轴旋转（翻滚动画）
        val tumbling = entityData.get(TUMBLING)
        val spinMul = if (circleState == CircleState.DYING) deathSpinFactor() else 1.0F
        val tumbleDeg = if (tumbling) stateTicks.toFloat() * CHANNEL_TUMBLE_SPEED * spinMul else 0.0F
        val eqN = if (tumbling) equatorNormal.rotateDA(tumbleDeg, equatorRadius.normalize()) else equatorNormal
        val ecN = if (tumbling) eclipticNormal.rotateDA(tumbleDeg, eclipticRadius.normalize()) else eclipticNormal
        // 环刃风暴：赤道/黄道半径随膨胀倍率扩展（部件位置外推）
        val ringScale = totalScale()
        for(i in 0..<equator.size) {
            val deg: Float = equatorRotation + (i / equator.size.toFloat() * 360F)
            val delta: Vec3 = equatorRadius.rotateDA(deg, eqN).scale(ringScale.toDouble())
            equator[i].changeState(origin.add(delta), eqN.cross(delta).normalize())
        }
        for(i in 0..<ecliptic.size) {
            val deg: Float = eclipticRotation + (i / ecliptic.size.toFloat() * 360F)
            val delta: Vec3 = eclipticRadius.rotateDA(deg, ecN).scale(ringScale.toDouble())
            ecliptic[i].changeState(origin.add(delta), delta.normalize())
        }
    }
    /** 碰撞攻击：部件命中范围内的非敌对生物（玩家/村民/猪灵等）受到伤害+击退，每受害者带冷却。 */
    private fun hurtContacts() {
        if (level().isClientSide) return
        if (isProtected()) return // 保护状态：停手
        if (circleState == CircleState.CHANNELING && currentSkill is RingSpinSkill) return // 环刃风暴的碰撞伤害由技能自身结算
        // 冷却衰减
        val it = contactCooldowns.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            val next = e.value - 1
            if (next <= 0) it.remove() else e.setValue(next)
        }
        val origin = position().add(0.0, bbHeight / 2.0, 0.0)
        for (part in getAllParts()) {
            val p = part.posNow
            if (p == Vec3.ZERO) continue // 首 tick 部件还没就位
            val box = AABB(p, p).inflate(1.1 * totalScale())
            for (victim in level().getEntitiesOfClass(LivingEntity::class.java, box)) {
                if (victim === this || victim is Enemy) continue
                if (contactCooldowns.containsKey(victim.uuid)) continue
                if (victim.hurt(level().damageSources().mobAttack(this), if (circleState == CircleState.CHARGING) CONTACT_DAMAGE * 2 else CONTACT_DAMAGE)) {
                    val kb = victim.position().subtract(origin)
                    victim.knockback(0.8, kb.x, kb.z)
                    contactCooldowns[victim.uuid] = CONTACT_COOLDOWN
                }
            }
        }
    }
    /** 石板弹：周期从黄道石板（连续多块齐射）朝目标发射，弹体携带各自石板图案。仅巡航状态发射。 */
    private fun tryFireSlate() {
        if (isProtected()) return // 保护状态：停手
        if (circleState != CircleState.CRUISE) return
        val t = target
        if (t == null || !t.isAlive) return
        if (fireCooldown > 0) {
            fireCooldown--
            return
        }
        // 射速：怒意增强但阻尼（射速可以快一点，但别太夸张）
        fireCooldown = (FIRE_INTERVAL / fireRateFactor()).toInt().coerceAtLeast(MIN_FIRE_INTERVAL)
        val volley = (1 + ((currentAnger() - 1.0F) * 1.5F).toInt()).coerceIn(1, MAX_VOLLEY)
        // 齐射音效：法术弹射声（偏大）
        level().playSound(null, blockPosition(), HexSounds.CAST_NORMAL, SoundSource.HOSTILE, 1.8F, 1.0F)
        for (n in 0 until volley) {
            val part = ecliptic[(slateIndex + n) % ecliptic.size] as? SlatePart ?: continue
            val from = part.posNow
            // 发射口紫色魔法粒子
            for (k in 0 until 4) {
                spawnParticle(
                    level(),
                    ParticleTypes.AMBIENT_ENTITY_EFFECT,
                    from.x, from.y, from.z,
                    (random.nextDouble() - 0.5) * 0.1,
                    random.nextDouble() * 0.05,
                    (random.nextDouble() - 0.5) * 0.1
                )
            }
            val projectile = SlateProjectile(HexMobEntities.SLATE_PROJECTILE.get(), level())
            projectile.setPos(from.x, from.y, from.z)
            projectile.owner = this
            projectile.pattern = part.pattern
            val aim = t.position().add(0.0, t.bbHeight / 2.0, 0.0).subtract(from)
            projectile.shoot(aim.x, aim.y, aim.z, SLATE_SPEED, 1.0F)
            level().addFreshEntity(projectile)
        }
        slateIndex += volley
    }
    /** 大环贴地：noPhysics 不会自然落地，手动检测脚底 1 格是否为实心地面；
     *  接触时在接触点炸出 3×3×3 的坑 + 爆炸音效（带冷却防刷屏）。 */
    private fun groundContact() {
        if (groundCraterCooldown > 0) {
            groundCraterCooldown--
            return
        }
        val feet = BlockPos.containing(position().x, y - 1.0, position().z)
        val state = level().getBlockState(feet)
        if (state.isAir || !state.fluidState.isEmpty) return
        if (state.getDestroySpeed(level(), feet) < 0.0F) return // 不炸基岩
        craterAround(level(), feet, this)
        groundCraterCooldown = GROUND_CRATER_COOLDOWN
    }
    /** 是否清醒（未沉睡）：守卫怪据此判断主人是否醒来。 */
    fun isAwake(): Boolean = !dormant

    /** 设置沉睡（结构召唤的大环初始沉睡；一旦唤醒不会自动再睡）。 */
    fun setDormant(v: Boolean) { dormant = v }

    /** 沉睡唤醒：附近 [WAKE_RANGE] 格内有玩家就醒来（苏醒演出：一声低吟）。 */
    private fun tryWakeFromDormancy() {
        val serverLevel = level() as? ServerLevel ?: return
        val nearest = serverLevel.getNearestPlayer(this, WAKE_RANGE)
        if (nearest != null) {
            wokenOnce = true
            dormant = false
            HexMob.LOGGER.info("[UrCircle] woke up: player {} within {} blocks", nearest.name.string, WAKE_RANGE.toInt())
            level().playSound(null, blockPosition(), HexSounds.CASTING_AMBIANCE, SoundSource.HOSTILE, 1.0F, 0.5F)
        }
    }

    /**
     * 结构召唤自查：ur_circle_arena 模板里的实体在 chunk 生成期会被序列化成 NBT 暂存
     * （ProtoChunk.addEntity(Entity) → save → pending），直到 chunk 转正才真正实体化，
     * 所以没法在 postProcess 里直接给实体打沉睡标记。改为实体化后的前若干 tick 自查：
     * 若自己处于 ur_circle_arena 结构范围内 → 初始沉睡。被玩家唤醒后不再复查。
     */
    private fun checkArenaDormancy() {
        if (dormant || wokenOnce) return
        if (arenaCheckAttempts++ >= ARENA_CHECK_MAX_TICKS) return
        val serverLevel = level() as? ServerLevel ?: return
        val structure = serverLevel.registryAccess()
            .registryOrThrow(Registries.STRUCTURE)
            .get(HexMob.id("ur_circle_arena")) ?: return
        val start = serverLevel.structureManager().getStructureAt(blockPosition(), structure)
        if (start.isValid()) {
            setDormant(true)
        }
    }

    /** Boss 血条（凋灵式）：进入 128 格的玩家看到紫色血条；活着时屏幕天色变暗 + Boss 音乐。 */
    private fun updateBossBar() {
        val serverLevel = level() as? ServerLevel ?: return
        if (bossEvent == null) {
            bossEvent = ServerBossEvent(type.description, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10).apply {
                setDarkenScreen(true)
                setPlayBossMusic(true)
            }
        }
        val be = bossEvent ?: return
        be.name = type.description
        be.progress = health / maxHealth
        if (dormant || !isAlive) {
            be.removeAllPlayers()
            return
        }
        for (player in serverLevel.players()) {
            if (player.distanceToSqr(this) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
                be.addPlayer(player)
            } else {
                be.removePlayer(player)
            }
        }
    }
    override fun remove(reason: Entity.RemovalReason) {
        bossEvent?.removeAllPlayers()
        super.remove(reason)
    }
    override fun die(source: DamageSource) {
        // 死亡演出（参考末影龙）：不立刻 super.die/移除，进入 DYING 状态由 combatBrain 分段驱动，
        // 演出末尾（核心毁灭后）才生成掉落并 remove()。期间不置 dead，保持实体数据同步让客户端播演出。
        if (circleState == CircleState.DYING) return
        circleState = CircleState.DYING
        stateTicks = 0
        currentSkill = null
        beamSkill = null
        bossEvent?.removeAllPlayers()
        bossEvent = null
        // 下属立即退场（不触 5 点反噬，避免死亡时连环扣血）
        for (s in level().getEntitiesOfClass(UrCircleServant::class.java, getBoundingBox().inflate(48.0))) {
            if (s.owner === this) s.remove(Entity.RemovalReason.KILLED)
        }
        // 清空仇恨，防止演出期间再索敌
        hatedTargets.clear()
        target = null
    }

    /** 死亡演出期间接管 vanilla 的死亡计时：血量归 0 后 vanilla 会在 deathTime>=20 时自动移除实体，
     *  这会掐断演出。这里置空，移除完全由 DYING 序列在演出末尾 finishDeath() 统一执行（参考末影龙）。 */
    override fun tickDeath() {
        // 演出由 runDeathSequence（stateTicks）驱动，这里不再递增 deathTime / 广播死亡事件 / 移除
    }
    /**
     * 大环状态机：
     * - 冲撞：CRUISE(概率触发) → WINDUP(前摇) → CHARGING(直线冲刺) → STAGGER(僵直) → CRUISE；
     * - 蓄力技能：CRUISE(满足 canUse 的技能) → CHANNELING(不移动、转速逐渐停止、脉冲粒子/音效)
     *   → 完全停下放 CAST_THOTH + cast() → CRUISE（带冷却）。
     * 大脑在 moveControl 之后运行，冲刺速度可覆盖其残留巡航速度。
     */
    private fun combatBrain() {
        // 死亡演出：接管状态机，逐段播放（加速→减速→渐熄→依次消失→核心毁灭→掉落）
        if (circleState == CircleState.DYING) {
            runDeathSequence()
            return
        }
        // 保护状态（血量>90% 且仍有下属存活）：停手回巡航，等玩家清完下属
        if (isProtected()) {
            circleState = CircleState.CRUISE
            stateTicks = 0
            currentSkill = null
            beamSkill = null
            skillCooldown = SKILL_COOLDOWN
            setDeltaMovement(deltaMovement.scale(0.85))
            return
        }
        when (circleState) {
            CircleState.CRUISE -> {
                if (chargeCooldown > 0) chargeCooldown--
                val t = target
                if (t != null && t.isAlive && chargeCooldown <= 0 && stateTicks > 40 && random.nextFloat() < 0.01F) {
                    circleState = CircleState.WINDUP
                    stateTicks = 0
                    return
                }
                // 蓄力技能：冷却好且存在满足 canUse 的技能 → 进入吟唱（按权重随机抽）
                if (skillCooldown > 0) skillCooldown--
                if (skillCooldown <= 0) {
                    val skill = pickWeightedSkill()
                    if (skill != null) {
                        currentSkill = skill
                        channelPulseTimer = 0
                        circleState = CircleState.CHANNELING
                        stateTicks = 0
                        level().playSound(null, blockPosition(), skill.channelStartSound(this), SoundSource.HOSTILE, skill.channelStartVolume(this), 1.0F)
                        spawnChannelParticles(skill) // 吟唱开始瞬间就来一波，立即有反馈
                    }
                }
            }
            CircleState.CHANNELING -> {
                val skill = currentSkill
                if (skill == null) {
                    // 保险：无技能就回巡航
                    circleState = CircleState.CRUISE
                    stateTicks = 0
                    return
                }
                // 蓄力：不移动，速度衰减
                setDeltaMovement(deltaMovement.scale(0.9))
                // 技能每 tick 钩子（如光炮吟唱自发光）
                skill.onChannelTick(this)
                // 可标记目标的技能：吟唱期间给目标挂发光（标记即将被打者）
                if (skill.channelMarksTarget(this)) {
                    target?.addEffect(MobEffectInstance(MobEffects.GLOWING, 12, 0, false, false))
                }
                // 粒子：每 4 tick 一波持续向圆心汇聚（保证肉眼可见）
                channelParticleTimer++
                if (channelParticleTimer >= CHANNEL_PARTICLE_INTERVAL) {
                    channelParticleTimer = 0
                    spawnChannelParticles(skill)
                }
                // 音效脉冲：每 channelPulseInterval tick 一次（间隔/类型/音量由技能决定）
                channelPulseTimer++
                if (channelPulseTimer >= skill.channelPulseInterval(this)) {
                    channelPulseTimer = 0
                    level().playSound(null, blockPosition(), skill.channelPulseSound(this), SoundSource.HOSTILE, skill.channelPulseVolume(this), 1.0F)
                }
                // 完全停下：释放音效 + 释放技能（cast 可能切入 BEAM 状态）
                if (stateTicks >= skill.channelTicks) {
                    level().playSound(null, blockPosition(), skill.releaseSound(this), SoundSource.HOSTILE, skill.releaseVolume(this), 1.0F)
                    skill.cast(this)
                    currentSkill = null
                    skillCooldown = SKILL_COOLDOWN
                    if (circleState != CircleState.BEAM) {
                        circleState = CircleState.CRUISE
                    }
                    stateTicks = 0
                }
            }
            CircleState.WINDUP -> {
                // 前摇：停住 + 紫色粒子示警
                setDeltaMovement(deltaMovement.scale(0.8))
                for (k in 0 until 3) {
                    val part = equator[random.nextInt(equator.size)]
                    spawnParticle(level(), ParticleTypes.END_ROD, part.posNow.x, part.posNow.y, part.posNow.z, 0.0, 0.08, 0.0)
                }
                if (stateTicks >= WINDUP_TICKS) {
                    val origin = position().add(0.0, bbHeight / 2.0, 0.0)
                    val t = target
                    chargeDir = if (t != null) t.position().add(0.0, t.bbHeight / 2.0, 0.0).subtract(origin).normalize() else Vec3(0.0, 0.0, 1.0)
                    circleState = CircleState.CHARGING
                    stateTicks = 0
                }
            }
            CircleState.CHARGING -> {
                // 直线高速冲刺（覆盖 moveControl 残留速度）
                setDeltaMovement(chargeDir.scale(CHARGE_SPEED))
                if (stateTicks >= CHARGE_TICKS) {
                    circleState = CircleState.STAGGER
                    stateTicks = 0
                }
            }
            CircleState.STAGGER -> {
                // 僵直：减速停住，恢复期结束后回巡航并进入冷却
                setDeltaMovement(deltaMovement.scale(0.8))
                if (stateTicks >= STAGGER_TICKS) {
                    circleState = CircleState.CRUISE
                    stateTicks = 0
                    chargeCooldown = CHARGE_COOLDOWN
                }
            }
            CircleState.BEAM -> {
                // 光束持续状态（第 4 步光炮）：cast() 调用 beginBeam 切入，每 tick 驱动技能
                val skill = beamSkill
                if (skill == null) {
                    circleState = CircleState.CRUISE
                    stateTicks = 0
                } else {
                    skill.onBeamTick(this)
                    if (stateTicks >= skill.beamTicks(this)) {
                        skill.onBeamEnd(this)
                        beamSkill = null
                        circleState = CircleState.CRUISE
                        stateTicks = 0
                    }
                }
            }
            // 死亡演出已在函数顶部 runDeathSequence() 接管，这里不会走到
            CircleState.DYING -> {}
        }
    }
    // ---- 死亡演出（DYING，参考末影龙） ----

    /** 每个部件"熄灭"的 tick（按全局序号 0..25：先内圈促动石 0..13，再外圈石板 14..25）。 */
    fun deathExtinguishTick(globalIndex: Int): Int = EXTINGUISH_START + globalIndex * EXTINGUISH_STAGGER

    /** 每个部件"消失"的 tick：全部熄灭后才开始依次消失。 */
    fun deathVanishTick(globalIndex: Int): Int = VANISH_START + globalIndex * VANISH_STAGGER

    /** 全局序号 → 部件（0..13 内圈促动石，14..25 外圈石板；核心 earth 单独处理）。 */
    private fun partAt(globalIndex: Int): UrCirclePart? =
        when {
            globalIndex < equator.size -> equator[globalIndex]
            globalIndex < equator.size + ecliptic.size -> ecliptic[globalIndex - equator.size]
            else -> null
        }

    /** DYING 状态逐 tick：停住 → 逐部件消失播方块破坏粒子/音效 → 核心毁灭 → 掉落移除。 */
    private fun runDeathSequence() {
        setDeltaMovement(deltaMovement.scale(0.85)) // 停住
        val t = stateTicks
        for (i in 0 until TOTAL_PARTS) {
            if (t == deathVanishTick(i)) {
                val part = partAt(i) ?: continue
                spawnPartBreak(part, isSlate = i >= equator.size)
            }
        }
        if (t == CORE_BREAK_TICK) {
            spawnCoreBreak()
        }
        if (t >= DEATH_END_TICK) {
            finishDeath()
        }
    }

    /** 部件消失：在部件位置炸开方块破坏粒子 + 破坏音效（服务端只发一次）。 */
    private fun spawnPartBreak(part: UrCirclePart, isSlate: Boolean) {
        val level = level()
        val pos = part.posNow
        if (pos == Vec3.ZERO) return
        val particle = if (isSlate) SLATE_BREAK else STONE_BREAK
        for (k in 0 until 20) {
            spawnParticle(
                level, particle,
                pos.x, pos.y, pos.z,
                (random.nextDouble() - 0.5) * 0.5, random.nextDouble() * 0.5, (random.nextDouble() - 0.5) * 0.5
            )
        }
        level.playSound(null, blockPosition(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.0F, 1.2F)
    }

    /** 核心毁灭（终幕）：紫水晶破坏音效 + 紫水晶破坏粒子。 */
    private fun spawnCoreBreak() {
        val level = level()
        var pos = earth.posNow
        if (pos == Vec3.ZERO) pos = position().add(0.0, bbHeight / 2.0, 0.0)
        for (k in 0 until 40) {
            spawnParticle(
                level, AMETHYST_BREAK,
                pos.x + (random.nextDouble() - 0.5) * 2.0,
                pos.y + (random.nextDouble() - 0.5) * 2.0,
                pos.z + (random.nextDouble() - 0.5) * 2.0,
                (random.nextDouble() - 0.5) * 0.4, random.nextDouble() * 0.4, (random.nextDouble() - 0.5) * 0.4
            )
        }
        level.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 2.0F, 1.0F)
    }

    /** 演出终幕：生成掉落物（大环核心物品）+ 经验球，然后移除实体。 */
    private fun finishDeath() {
        if (level().isClientSide) return
        val level = level() as ServerLevel
        val dropPos = position().add(0.0, bbHeight / 2.0, 0.0)
        // 战利品：必定掉落 1 个大环核心
        val item = ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, ItemStack(HexMobItems.UR_CIRCLE_CORE.get()))
        item.setDeltaMovement((random.nextDouble() - 0.5) * 0.3, random.nextDouble() * 0.3, (random.nextDouble() - 0.5) * 0.3)
        level.addFreshEntity(item)
        // 经验球（Boss 奖励）
        ExperienceOrb.award(level, dropPos, getExperienceReward())
        remove(Entity.RemovalReason.KILLED)
    }

    /** 在"满足 canUse 的技能"里按权重随机抽一个（权重越高越常被选中）；没有可用技能返回 null。 */
    private fun pickWeightedSkill(): UrCircleSkill? {
        val usable = skills.filter { it.canUse(this) }
        if (usable.isEmpty()) return null
        val total = usable.sumOf { it.weight }
        if (total <= 0) return usable.random()
        var r = random.nextInt(total)
        for (s in usable) {
            r -= s.weight
            if (r < 0) return s
        }
        return usable.last()
    }
    /** 蓄力粒子：位置/类型/速度/数量全部由技能定义，越接近完成越密集。
     *  客户端与服务端都会调用：客户端本地 addParticle；服务端 spawnParticle 发包广播。 */
    private fun spawnChannelParticles(skill: UrCircleSkill) {
        val progress = (stateTicks.toFloat() / skill.channelTicks).coerceIn(0.0F, 1.0F)
        val count = skill.channelParticlesPerPulse(this) + (progress * 16).toInt()
        val type = skill.channelParticleType(this)
        for (k in 0 until count) {
            val pos = skill.channelParticlePos(this)
            val vel = skill.channelParticleVelocity(this, pos)
            spawnParticle(level(), type, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z)
        }
    }
    override fun addAdditionalSaveData(nbt: CompoundTag) {
        super.addAdditionalSaveData(nbt)
        nbt.putBoolean("hexmob:Dormant", dormant)
    }
    override fun readAdditionalSaveData(compound: CompoundTag) {
        dormant = compound.getBoolean("hexmob:Dormant")
        // 读档恢复血量走 setHealth，需放行
        internalHealthWrite = true
        super.readAdditionalSaveData(compound)
        internalHealthWrite = false
    }
    override fun getAllParts(): List<UrCirclePart> {
        return buildList {
            addAll(equator)
            addAll(ecliptic)
            add(earth)
        }
    }
    override fun shouldRecord(): Boolean = isAlive
    override fun addEffect(effectInstance: MobEffectInstance, entity: Entity?): Boolean =
        // 只放行发光效果（光炮吟唱自发光用），其余状态免疫
        if (effectInstance.effect == MobEffects.GLOWING) super.addEffect(effectInstance, entity) else false
    override fun canRide(vehicle: Entity): Boolean = false
    override fun canChangeDimensions(): Boolean = false
    override fun isPickable(): Boolean = false
    override fun getExperienceReward() = Enemy.XP_REWARD_BOSS
    override fun isNoGravity(): Boolean = true
    /** Boss 持久化：vanilla 敌对生物会因"玩家太远/超时"被 checkDespawn 自然移除，这里禁掉（Boss 不该自然消失）。 */
    override fun isPersistenceRequired(): Boolean = true
    /** 大环不会着火：忽略一切点火（火焰伤害仍走 90% 减免）。 */
    override fun setRemainingFireTicks(ticks: Int) {}

    /** 大环的"拒绝引用"事故：3 秒失明 + 自然文案。 */
    override fun createFlickeringMishap(entity: net.minecraft.world.entity.Entity): Mishap = UrCircleFlickerMishap(this)
    /** 被打立刻还击：把（直接/间接）攻击者设为当前目标——凋灵式 HurtByTarget 行为；
     *  吟唱（CHANNELING）/光束（BEAM）期间受伤委托给当前技能控制。
     *  整个服务端受击流程包裹 internalHealthWrite，让随后的扣血 setHealth 得以通过（见 setHealth 免疫规则）。
     *
     *  限伤管线（顺序：免疫/减免 → 软上限 → DPS 窗口 → 技能减伤倍率）：
     *  - 免疫：环境/cheese 伤害（摔落、挤压、溺水、冰冻、仙人掌、虚空）与爆炸直接无视；
     *  - 火焰减免 [FIRE_DAMAGE_MULTIPLIER]（90%）；
     *  - 软上限：单发 > [SOFT_HIT_CAP] 的部分只按 [DAMAGE_OVERFLOW_MULTIPLIER]（20%）结算；
     *  - DPS 窗口：每 [DPS_WINDOW_TICKS] tick 累计伤害上限 [DPS_CAP_PER_WINDOW]，超出的不计。
     *  实际受伤害还会喂给过载计数器（受伤额外行为，见 [accumulateOverload]）。 */
    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (!level().isClientSide) {
            if (dormant) return false // 沉睡：无敌（唤醒只靠玩家接近，不靠攻击）
            if (circleState == CircleState.DYING) return false // 死亡演出：不再受伤
            val attacker = source.entity
            if (attacker === this) return false // 自身来源的伤害（如自爆）直接免疫，不结算
            if (attacker is LivingEntity && attacker !== this && attacker.isAlive) {
                target = attacker
                addToHated(attacker)
            }
            if (source.`is`(HexDamageTypes.OVERCAST)) return false // 忽略过度施法（反向施法）伤害
            if (isImmuneDamage(source)) return false // 环境/爆炸免疫
            if (isProtected()) return false // 保护状态（血>90% 且有下属）：不受任何伤害
        }
        // 火焰减免 90%
        var dmg = if (isFireDamage(source)) amount * FIRE_DAMAGE_MULTIPLIER else amount
        // 软上限：单发 > SOFT_HIT_CAP 的部分只吃 20%
        if (dmg > SOFT_HIT_CAP) {
            dmg = SOFT_HIT_CAP + (dmg - SOFT_HIT_CAP) * DAMAGE_OVERFLOW_MULTIPLIER
        }
        // DPS 窗口：每 DPS_WINDOW_TICKS 累计上限 DPS_CAP_PER_WINDOW（服务端结算；客户端只看软上限）
        if (!level().isClientSide) {
            val now = tickCount
            if (now - dpsWindowStart >= DPS_WINDOW_TICKS) {
                dpsWindowStart = now
                dpsWindow = 0.0F
            }
            val room = DPS_CAP_PER_WINDOW - dpsWindow
            val allowed = dmg.coerceAtMost(room)
            if (allowed <= 0.0F) return false // 本窗口已满：本次伤害不计
            dpsWindow += allowed
            dmg = allowed
        }
        internalHealthWrite = true
        return try {
            val active = when (circleState) {
                CircleState.CHANNELING -> currentSkill
                CircleState.BEAM -> beamSkill
                else -> null
            }
            val result = if (active != null && !level().isClientSide) {
                active.onChannelHurt(this, source, dmg)
            } else {
                super.hurt(source, dmg)
            }
            // 受伤额外行为：实际受伤害喂给过载计数器
            if (result && !level().isClientSide && dmg > 0.0F) {
                accumulateOverload(dmg)
            }
            result
        } finally {
            internalHealthWrite = false
        }
    }

    /** 完全免疫的伤害类型：环境/cheese 伤害 + 爆炸（TNT/苦力怕/玩家爆破/火球）。 */
    private fun isImmuneDamage(source: DamageSource): Boolean =
        source.`is`(DamageTypes.FALL) || source.`is`(DamageTypes.FLY_INTO_WALL) ||
            source.`is`(DamageTypes.IN_WALL) || source.`is`(DamageTypes.DROWN) ||
            source.`is`(DamageTypes.DRY_OUT) || source.`is`(DamageTypes.FREEZE) ||
            source.`is`(DamageTypes.CACTUS) || source.`is`(DamageTypes.FELL_OUT_OF_WORLD) ||
            source.`is`(DamageTypes.EXPLOSION) || source.`is`(DamageTypes.PLAYER_EXPLOSION) ||
            source.`is`(DamageTypes.FIREBALL) || source.`is`(DamageTypes.UNATTRIBUTED_FIREBALL)

    /** 火焰系伤害：减免 [FIRE_DAMAGE_MULTIPLIER]（90%）。 */
    private fun isFireDamage(source: DamageSource): Boolean =
        source.`is`(DamageTypes.LAVA) || source.`is`(DamageTypes.IN_FIRE) ||
            source.`is`(DamageTypes.ON_FIRE) || source.`is`(DamageTypes.HOT_FLOOR)

    /** 受伤额外行为——过载反击：短窗口内实际受伤害累计超阈值，触发冲击波（伤害+强击退），把站桩玩家弹开。 */
    private fun accumulateOverload(amount: Float) {
        val now = tickCount
        if (now - overloadWindowStart >= OVERLOAD_WINDOW_TICKS) {
            overloadWindowStart = now
            overloadDamage = 0.0F
        }
        overloadDamage += amount
        if (overloadCooldown > 0) overloadCooldown--
        if (overloadDamage >= OVERLOAD_THRESHOLD && overloadCooldown <= 0) {
            triggerOverload()
            overloadDamage = 0.0F
            overloadCooldown = OVERLOAD_COOLDOWN
        }
    }

    /** 过载冲击波：以核心为中心扩张，伤害并击退半径 [OVERLOAD_RADIUS] 内非敌对实体。 */
    private fun triggerOverload() {
        val level = level()
        if (level.isClientSide) return
        val origin = beamOrigin()
        val box = AABB(origin, origin).inflate(OVERLOAD_RADIUS)
        for (victim in level.getEntitiesOfClass(LivingEntity::class.java, box)) {
            if (victim === this || victim is Enemy) continue
            victim.hurt(level.damageSources().mobAttack(this), OVERLOAD_DAMAGE)
            val kb = victim.position().subtract(origin)
            victim.knockback(OVERLOAD_KNOCKBACK, kb.x, kb.z)
        }
        // 冲击波粒子（辐射状 END_ROD）+ 音效
        for (k in 0 until 40) {
            val ang = random.nextDouble() * Math.PI * 2.0
            val r = random.nextDouble() * OVERLOAD_RADIUS
            spawnParticle(level, ParticleTypes.LARGE_SMOKE,
                origin.x + cos(ang) * r, origin.y + (random.nextDouble() - 0.5) * 3.0, origin.z + sin(ang) * r,
                0.0, 0.1, 0.0)
        }
        level.playSound(null, blockPosition(), HexSounds.CAST_FAILURE, SoundSource.HOSTILE, 1.8F, 1.4F)
    }

    /** 免疫击退：任何来源的击退都忽略。 */
    override fun knockback(strength: Double, x: Double, z: Double) {}

    /** 免疫外部 setHealth：只允许自身伤害流程（internalHealthWrite）或出生/读档窗口（tickCount<=1）写血；
     *  其他来源直接 setHealth（回血/改血/锁血）一律忽略。
     *  注意：1.20.1 LivingEntity 构造时 health 字段初始为 1.0F（非 0），所以出生判断用 tickCount 而不是 health==0。 */
    override fun setHealth(health: Float) {
        if (internalHealthWrite || tickCount <= 1) {
            super.setHealth(health)
        }
    }

    /** 受击音效：暂用石板被破坏的音效（改这里即可换）。 */
    override fun getHurtSound(source: DamageSource): SoundEvent =
        HexBlocks.SLATE.defaultBlockState().getSoundType().getBreakSound()

    /** 吟唱被技能允许时实际施加伤害（直接走父类 hurt，避免再次进入 CHANNELING 控制分支造成死循环）。 */
    fun applyChannelDamage(source: DamageSource, amount: Float): Boolean = super.hurt(source, amount)

    /** 打断当前吟唱/光束：回到巡航并进入技能冷却。 */
    fun interruptChannel() {
        when (circleState) {
            CircleState.CHANNELING -> { currentSkill = null }
            CircleState.BEAM -> { beamSkill = null }
            else -> return
        }
        circleState = CircleState.CRUISE
        stateTicks = 0
        skillCooldown = SKILL_COOLDOWN
    }

    /** 进入光束状态（cast() 调用）：由 beamSkill 的 onBeamTick 驱动后续。 */
    fun beginBeam(skill: UrCircleSkill) {
        beamSkill = skill
        circleState = CircleState.BEAM
        stateTicks = 0
    }

    /** 光束起点：核心（earth）位置，未就位时退回大环中心。 */
    fun beamOrigin(): Vec3 {
        val e = earth.posNow
        return if (e == Vec3.ZERO) position().add(0.0, bbHeight / 2.0, 0.0) else e
    }

    /** 存活下属数量（64 格内、本大环召唤、还活着的仆从：恼鬼下属 + 被召唤的守卫）。仅服务端。 */
    fun livingServants(): Int {
        if (level().isClientSide) return 0
        val box = AABB(blockPosition()).inflate(64.0)
        val vex = level().getEntitiesOfClass(UrCircleServant::class.java, box) {
            it.isAlive && it.getOwner() === this
        }.size
        val guards = level().getEntitiesOfClass(CrystalGuardEntity::class.java, box) {
            it.isAlive && it.isSummonedBy(this)
        }.size
        return vex + guards
    }

    /** 保护状态：血量 >90% 且仍有下属存活 → 大环停转、不动、不受伤害。（死亡演出期间不适用） */
    fun isProtected(): Boolean =
        circleState != CircleState.DYING && health / maxHealth > PROTECT_HEALTH_RATIO && livingServants() > 0

    /** 巡航空闲行为：无目标时——血量 <90% 直接开始回复；满血（≥90%）自动一只只补下属到上限，
     *  配合 [isProtected] 让大环停转+无敌。仅巡航、无存活目标时触发。 */
    private fun idleBehavior() {
        if (circleState != CircleState.CRUISE) return
        if (servantSummonCooldown > 0) servantSummonCooldown--
        val t = target
        if (t != null && t.isAlive) return // 有目标：进入战斗，不回复不补员
        val hpRatio = health / maxHealth
        if (hpRatio < PROTECT_HEALTH_RATIO) {
            // 无目标且血量 <90%：直接开始回复（回血到 90% 停）
            healSelf(IDLE_REGEN)
            if (tickCount % 10 == 0) {
                val origin = position().add(0.0, bbHeight / 2.0, 0.0)
                for (k in 0 until 3) {
                    spawnParticle(level(), ParticleTypes.HAPPY_VILLAGER,
                        origin.x + (random.nextDouble() - 0.5) * 5.0,
                        origin.y + (random.nextDouble() - 0.5) * 5.0,
                        origin.z + (random.nextDouble() - 0.5) * 5.0,
                        0.0, 0.3, 0.0)
                }
            }
            return
        }
        // 满血（≥90%）且无目标：一只只补下属到上限，第一只出来即触发 isProtected 停转+无敌
        if (livingServants() < SummonServantSkill.MAX_SERVANTS && servantSummonCooldown <= 0) {
            summonServantOne()
            servantSummonCooldown = SERVANT_SUMMON_INTERVAL
        }
    }

    /** 召唤一个下属（出现在大环周围随机角度，类型随机：恼鬼/弓箭/斧头/傀儡守卫）。 */
    fun summonServantOne() {
        summonPets(1)
    }

    /** 批量召唤 `count` 只仆从：恼鬼 40% / 弓箭守卫 20% / 斧头守卫 20% / 傀儡守卫 20%。 */
    fun summonPets(count: Int) {
        val origin = position().add(0.0, bbHeight / 2.0, 0.0)
        for (i in 0 until count) summonPetOne(origin)
    }

    private fun summonPetOne(origin: Vec3) {
        val level = level()
        if (level.isClientSide) return
        val ang = random.nextDouble() * Math.PI * 2.0
        val pos = Vec3(origin.x + cos(ang) * 2.0, origin.y + 1.0, origin.z + sin(ang) * 2.0)
        val roll = random.nextFloat()
        when {
            roll < 0.4F -> {
                val s = UrCircleServant(HexMobEntities.UR_CIRCLE_SERVANT.get(), level)
                s.setPos(pos.x, pos.y, pos.z)
                s.setOwner(this)
                level.addFreshEntity(s)
            }
            roll < 0.6F -> spawnGuardPet(HexMobEntities.GUARD_ARCHER.get(), pos)
            roll < 0.8F -> spawnGuardPet(HexMobEntities.GUARD_BRUTE.get(), pos)
            else -> spawnGuardPet(HexMobEntities.GUARD_GOLEM.get(), pos)
        }
    }

    private fun spawnGuardPet(type: EntityType<out CrystalGuardEntity>, pos: Vec3) {
        val level = level()
        val guard = type.create(level) ?: return
        guard.setPos(pos.x, pos.y, pos.z)
        guard.setSummoner(this)
        level.addFreshEntity(guard)
    }

    // ---- 仇恨实体列表（多目标） ----

    /** 加入仇恨列表：非敌对、存活、在 [HATE_RANGE] 内、尚未记录。 */
    fun addToHated(entity: LivingEntity) {
        if (level().isClientSide) return
        if (entity === this || entity is Enemy || !entity.isAlive) return
        if (entity !in hatedTargets && distanceToSqr(entity) <= HATE_RANGE * HATE_RANGE) {
            hatedTargets.add(entity)
        }
    }

    /** 当前有效仇恨目标（存活、未移除、非敌对、在 [HATE_RANGE] 内）。 */
    fun currentHated(): List<LivingEntity> =
        hatedTargets.filter { it.isAlive && !it.isRemoved && it !is Enemy && distanceToSqr(it) <= HATE_RANGE * HATE_RANGE }

    /**
     * 目标优先级排序（多目标时）：**最优先攻击玩家**，其次攻击**剩余生命值最高**的实体。
     * 返回排好序的列表（第一个是最优先目标）。
     */
    fun preferredTargets(entities: Collection<LivingEntity>): List<LivingEntity> =
        entities.sortedWith(
            compareByDescending<LivingEntity> { it is Player }
                .thenByDescending { it.health }
        )

    /** 下属死亡惩罚：每击败一个下属，大环受 [SERVANT_DEATH_DAMAGE] 直伤（绕过保护，直接内写血量）。 */
    fun onServantKilled() {
        if (level().isClientSide || !isAlive || circleState == CircleState.DYING) return
        internalHealthWrite = true
        try {
            setHealth(health - SERVANT_DEATH_DAMAGE)
        } finally {
            internalHealthWrite = false
        }
        if (health <= 0.0F) {
            die(level().damageSources().generic())
        }
    }

    /** 恢复自身状态：大环自身技能回血用，直接内写血量（受 setHealth 钳制到 maxHealth）。 */
    fun healSelf(amount: Float) {
        if (level().isClientSide || !isAlive || circleState == CircleState.DYING) return
        internalHealthWrite = true
        try {
            setHealth(health + amount)
        } finally {
            internalHealthWrite = false
        }
    }

    /** 反向过度施法：玩家在附近施法（每次 CastingEnvironment 创建）调此积累反噬值。
     *  达到 [BACKLASH_THRESHOLD] 立刻报复（免吟唱），然后清空重算。 */
    fun accumulateBacklash(player: ServerPlayer) {
        if (level().isClientSide) return
        if (isProtected()) return // 保护期间（下属挡着）：不额外报复
        if (tickCount - lastBacklashTick < BACKLASH_TICK_INTERVAL) return // 节流：约每秒最多一次
        lastBacklashTick = tickCount
        backlash += 1
        if (backlash >= BACKLASH_THRESHOLD) {
            backlash = 0
            retaliate(player)
        }
    }

    /** 报复：大环粒子示警 + 音效，然后丢事故（60%）或直接上 debuff（40%）给施法者。 */
    private fun retaliate(player: ServerPlayer) {
        val origin = position().add(0.0, bbHeight / 2.0, 0.0)
        for (k in 0 until 20) {
            spawnParticle(
                level(), ParticleTypes.PORTAL,
                origin.x + (random.nextDouble() - 0.5) * 8.0,
                origin.y + (random.nextDouble() - 0.5) * 4.0,
                origin.z + (random.nextDouble() - 0.5) * 8.0,
                0.0, 0.0, 0.0
            )
        }
        level().playSound(null, blockPosition(), HexSounds.CAST_FAILURE, SoundSource.HOSTILE, 1.6F, 1.0F)
        if (random.nextFloat() < 0.6F) {
            UrCircleMishap.throwAt(player, UrCircleStatusTable.randomMishap(this, random))
        } else {
            val debuff = UrCircleStatusTable.randomDebuff(random)
            player.addEffect(MobEffectInstance(debuff.effect, debuff.duration, debuff.amplifier))
            player.sendSystemMessage(
                Component.translatableWithFallback("hexmob.backlash.message", "大环的过度施法反噬向你袭来！")
            )
        }
    }

    override fun recreateFromPacket(packet: ClientboundAddEntityPacket) {
        super.recreateFromPacket(packet)
        val parts: List<UrCirclePart> = getAllParts()
        for(i in parts.indices) {
            parts[i].setId(packet.id + i + 1)
        }
    }
    companion object {
        val EQUATOR_RADIUS: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val EQUATOR_NORMAL: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val EQUATOR_ROTATION: EntityDataAccessor<Float> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.FLOAT)
        val ECLIPTIC_RADIUS: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val ECLIPTIC_NORMAL: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val ECLIPTIC_ROTATION: EntityDataAccessor<Float> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.FLOAT)
        val EARTH_RADIUS: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val EARTH_NORMAL: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val EARTH_ROTATION: EntityDataAccessor<Float> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.FLOAT)
        val STATE: EntityDataAccessor<Int> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.INT)
        val STATE_TICKS: EntityDataAccessor<Int> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.INT)
        /** 吟唱"可打断技能"时置真：驱动赤道/黄道面绕各自半径轴旋转的动画。 */
        val TUMBLING: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.BOOLEAN)
        val RING_SPINNING: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.BOOLEAN)
        const val CONTACT_DAMAGE = 6.0F
        const val CONTACT_COOLDOWN = 20
        const val FIRE_INTERVAL = 50
        const val SLATE_SPEED = 1.0F
        const val MIN_FIRE_INTERVAL = 12
        const val MAX_VOLLEY = 3
        const val WINDUP_TICKS = 20
        const val CHARGE_TICKS = 12
        const val STAGGER_TICKS = 30
        const val CHARGE_SPEED = 1.2
        const val CHARGE_COOLDOWN = 100
        const val GROUND_CRATER_COOLDOWN = 40
        const val AMBIENT_SOUND_INTERVAL = 100
        const val BOSS_BAR_RANGE = 128.0
        /** 沉睡唤醒半径（格）：玩家进入此范围大环苏醒。 */
        const val WAKE_RANGE = 24.0
        /** 结构自查窗口：实体化后最多查这么多 tick（约 3 秒），找不到就认为不是结构召唤。 */
        const val ARENA_CHECK_MAX_TICKS = 60
        const val SKILL_COOLDOWN = 100
        const val CHANNEL_PARTICLE_INTERVAL = 4
        const val PROTECT_HEALTH_RATIO = 0.9F
        /** 巡航空闲自动回血：无目标且血量 <90% 时每 tick 回复量。 */
        const val IDLE_REGEN = 1.0F
        /** 巡航空闲自动补下属：满血无目标时每只的间隔（tick）。 */
        const val SERVANT_SUMMON_INTERVAL = 40
        /** 反向过度施法：玩家距大环多近施法才积累反噬（格）。 */
        const val BACKLASH_RANGE = 32.0
        /** 反向过度施法：积累多少点反噬触发一次报复。 */
        const val BACKLASH_THRESHOLD = 5
        /** 反向过度施法：两次积累的最小间隔（tick，防同一施法被多次计数）。 */
        const val BACKLASH_TICK_INTERVAL = 2
        /** 仇恨实体列表有效距离（格），超出即遗忘（比索敌扫描范围更大，锁过的目标记得更久）。 */
        const val HATE_RANGE = 128.0
        /** 下属死亡对大环的反噬直伤（每击败一个下属扣这么多血）。 */
        const val SERVANT_DEATH_DAMAGE = 5.0F
        /** 可打断技能吟唱时，赤道/黄道面绕各自半径轴旋转的角速度（度/tick）。 */
        const val CHANNEL_TUMBLE_SPEED = 5.0F
        // ---- 限伤（软上限 + DPS 窗口）----
        /** 单发软上限：单次伤害超过此值的部分只按 [DAMAGE_OVERFLOW_MULTIPLIER] 结算。 */
        const val SOFT_HIT_CAP = 35.0F
        /** 单发超出部分的结算比例（20%）。 */
        const val DAMAGE_OVERFLOW_MULTIPLIER = 0.2F
        /** DPS 窗口时长（tick，1 秒=20）。 */
        const val DPS_WINDOW_TICKS = 20
        /** 每窗口累计伤害上限（每秒约 70 点）。 */
        const val DPS_CAP_PER_WINDOW = 70.0F
        // ---- 伤害免疫/减免 ----
        /** 火焰系伤害减免倍率（90% 减免 → 0.1）。 */
        const val FIRE_DAMAGE_MULTIPLIER = 0.1F
        // ---- 受伤额外行为：过载反击 ----
        /** 过载窗口时长（tick）。 */
        const val OVERLOAD_WINDOW_TICKS = 20
        /** 过载阈值：窗口内实际受伤害累计达此值触发冲击波。 */
        const val OVERLOAD_THRESHOLD = 40.0F
        /** 过载冲击波伤害。 */
        const val OVERLOAD_DAMAGE = 10.0F
        /** 过载冲击波半径（格）。 */
        const val OVERLOAD_RADIUS = 8.0
        /** 过载冲击波击退强度。 */
        const val OVERLOAD_KNOCKBACK = 2.0
        /** 过载反击冷却（tick）。 */
        const val OVERLOAD_COOLDOWN = 100
        // ---- 死亡演出时序（参考末影龙）----
        /** 死亡演出总部件数（内圈促动石 14 + 外圈石板 12；核心单独处理）。 */
        const val TOTAL_PARTS = 26
        /** 转速加速段（tick）：0..SPIN_UP 从 1 倍加到 3 倍。 */
        const val SPIN_UP_TICKS = 30
        /** 转速减速段终点（tick）：此后转速为 0（3→0 缓停）。 */
        const val SPIN_DOWN_END = 90
        /** 部件开始逐个熄灭的 tick。 */
        const val EXTINGUISH_START = 40
        /** 每两个部件熄灭的间隔（tick）。 */
        const val EXTINGUISH_STAGGER = 2
        /** 部件开始逐个消失的 tick（全部熄灭之后）。 */
        const val VANISH_START = 100
        /** 每两个部件消失的间隔（tick）。 */
        const val VANISH_STAGGER = 3
        /** 核心毁灭的 tick。 */
        const val CORE_BREAK_TICK = 185
        /** 演出结束（掉落+移除）的 tick。 */
        const val DEATH_END_TICK = 195
        /** 部件消失粒子：石板用石板方块破坏粒子。 */
        val SLATE_BREAK = BlockParticleOption(ParticleTypes.BLOCK, HexBlocks.SLATE.defaultBlockState())
        /** 部件消失粒子：促动石用石头破坏粒子。 */
        val STONE_BREAK = BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState())
        /** 核心毁灭粒子：紫水晶破坏粒子。 */
        val AMETHYST_BREAK = BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState())
        /** 环刃风暴的部件/半径最大放大倍率（满吟唱时）。 */
        const val RING_SPIN_MAX_SCALE = 6.0F
        /** 飞行部件回避：外圈采样点数。 */
        const val RING_CLEAR_SAMPLES = 12
        /** 飞行部件回避：内圈（促动石）采样点数。 */
        const val RING_INNER_SAMPLES = 8
        /** 飞行部件回避：垂直覆盖 = 外圈半径 × 此比例（黄道面倾斜 ±半径×sin23°≈0.4×半径）。 */
        const val RING_THICKNESS_RATIO = 0.5
        /** 飞行部件回避：垂直覆盖下限（格）。 */
        const val MIN_RING_THICKNESS = 2
        /** 飞行部件回避：垂直覆盖上限（格，防体积放大后采样爆炸）。 */
        const val MAX_RING_THICKNESS = 10
        /** 飞行部件回避：外圈半径外加的安全边距（格，抵消部件半宽）。 */
        const val RING_CLEAR_MARGIN = 0.8
        fun registerAttributes(): AttributeSupplier.Builder = createMobAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.ARMOR, 20.0).add(Attributes.ARMOR_TOUGHNESS, 10.0)
    }
}