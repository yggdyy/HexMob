package pub.pigeon.yggdyy.hexmob.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import pub.pigeon.yggdyy.hexmob.content.quench_allay.action.OpAllayCasting
import pub.pigeon.yggdyy.hexmob.content.quench_allay.action.OpAllayTarget
import pub.pigeon.yggdyy.hexmob.content.quench_allay.action.OpQuenchAllayCreate
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.OpTransformStimulatedPatternSpell
import pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell.AmethystTrapSpell
import pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell.BeamSpell
import pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell.SerpentSpell
import pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell.SlateProjectileSpell

object HexMobActions : HexMobRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {
    val TRANSFORM_STIMULATED_PATTERN: Entry<ActionRegistryEntry> = make("transform_stimulated_pattern", HexDir.SOUTH_EAST, "deaqqwwqqaed", OpTransformStimulatedPatternSpell())

    val QUENCH_ALLAY_CAST = make("quench_allay/cast", HexDir.EAST,"qqaqwwawwqaqq", OpAllayCasting)

    val QUENCH_ALLAY_MOVE = make("quench_allay/move", HexDir.NORTH_EAST,"qaawawaaq", OpAllayTarget)

    val QUENCH_ALLAY_CREATE = make("quench_allay", HexDir.EAST,"qqqqadedqqqdqqqqadedqqqdqqqqwaqaeqq", OpQuenchAllayCreate)

    // ===== 大环玩家法术（副手持大环核心才能施放） =====
    // TODO: 下列 4 条法术的 signature（笔顺）留空待用户填写。
    // 填法参考上方：笔顺字母串 + 起始方向（HexDir），字母表见 at.petrak.hexcasting.api.casting.math.HexDirection
    // （a/e/w/q 表示线条方向，q 还表示转折点）。空笔顺可注册但无实际图案，填好前请勿在游戏内画它。

    val UR_AMETHYST_TRAP = make("ur_amethyst_trap", HexDir.WEST, "qwqwqqwqwaqew", AmethystTrapSpell())

    val UR_BEAM = make("ur_beam", HexDir.EAST, "wqwqwqwqwqwaeqqqaqw", BeamSpell())

    val UR_SERPENT = make("ur_serpent", HexDir.SOUTH_EAST, "dewdedweaaewqaqwaq", SerpentSpell())

    val UR_SLATE_PROJECTILE = make("ur_slate_projectile", HexDir.WEST, "qwqwqaeqedwew", SlateProjectileSpell())

    private fun make(name: String, startDir: HexDir, signature: String, action: Action) =
        make(name, startDir, signature) { action }
    private fun make(name: String, startDir: HexDir, signature: String, getAction: () -> Action) = register(name) {
        ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), getAction())
    }
}
