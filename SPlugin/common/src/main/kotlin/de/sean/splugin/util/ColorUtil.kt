package de.sean.splugin.util

import java.awt.Color

object ColorUtil {
    private val colors: Array<Color> = Color::class.java.fields.map { it.get(null) }.filterIsInstance<Color>().toTypedArray()

    /**
     * Returns a random color from the java.awt.Color class based
     * on a pseudorandom int from `MathUtil.randomInt`.
     */
    fun randomColor(): Color {
        return colors[MathUtil.randomInt(0, colors.size)]
    }
}