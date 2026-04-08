package pub.pigeon.yggdyy.hexmob.util

import net.minecraft.world.phys.Vec3
import kotlin.math.*

fun Vec3.rotateDA(deg: Float, axis: Vec3): Vec3 {
    val y: Vec3 = axis.cross(this).scale(1.0 / axis.length())
    val o: Vec3 = axis.scale(axis.dot(this) / axis.lengthSqr())
    val x: Vec3 = y.cross(axis).scale(1.0 / axis.length())
    val rad: Double = deg / 180F * PI
    return o.add(y.scale(sin(rad))).add(x.scale(cos(rad)))
}