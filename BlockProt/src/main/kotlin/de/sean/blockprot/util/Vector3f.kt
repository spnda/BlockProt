package de.sean.blockprot.util

class Vector3f constructor(private var x: Float, private var y: Float, private var z: Float) {
    companion object {
        fun fromDouble(x: Double, y: Double, z: Double): Vector3f
            = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun getXInt() = x.toInt()
    fun getYInt() = y.toInt()
    fun getZInt() = z.toInt()
}
