package de.sean.splugin.util

class Vector3f(x: Float, y: Float, z: Float) {
    private var x: Float = x
    private var y: Float = y
    private var z: Float = z

    companion object {
        fun fromDouble(x: Double, y: Double, z: Double): Vector3f
            = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun getX() = x
    fun getY() = y
    fun getZ() = z

    fun getXInt() = x.toInt()
    fun getYInt() = y.toInt()
    fun getZInt() = z.toInt()
}
