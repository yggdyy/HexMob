@file:JvmName("HexMobAbstractions")

package pub.pigeon.yggdyy.hexmob

/*
 * Hex-action (and other game-content) registration is intentionally NOT wired
 * through @ExpectPlatform here anymore.
 *
 * The common @ExpectPlatform stub's body was not being replaced at runtime in
 * this Architectury/Kotlin setup (the transformer that swaps in the platform
 * implementation never ran), so calling it just hit `throw AssertionError()`.
 *
 * Instead each platform entrypoint calls its own local implementation directly:
 *   - Fabric: FabricHexMob -> initRegistry(HexMobActions)
 *          (pub.pigeon.yggdyy.hexmob.fabric.HexMobAbstractionsImpl)
 *   - Forge:  ForgeHexMob    -> initRegistry(HexMobActions)
 *          (pub.pigeon.yggdyy.hexmob.forge.HexMobAbstractionsImpl)
 */
