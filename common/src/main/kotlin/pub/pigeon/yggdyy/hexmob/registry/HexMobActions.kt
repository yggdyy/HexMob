package pub.pigeon.yggdyy.hexmob.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.OpTransformStimulatedPatternSpell

object HexMobActions : HexMobRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {
    val TRANSFORM_STIMULATED_PATTERN: Entry<ActionRegistryEntry> = make("transform_stimulated_pattern", HexDir.SOUTH_EAST, "deaqqwwqqaed", OpTransformStimulatedPatternSpell())
    private fun make(name: String, startDir: HexDir, signature: String, action: Action) =
        make(name, startDir, signature) { action }
    private fun make(name: String, startDir: HexDir, signature: String, getAction: () -> Action) = register(name) {
        ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), getAction())
    }
}
