package de.sean.splugin.util

import java.awt.Color

object ColorUtil {
    private val colors = arrayOf(Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW)

    /**
     * Returns a random color from the java.awt.Color class based
     * on a pseudorandom int from `MathUtil.randomInt`.
     */
    fun randomColor(): Color {
        return colors[MathUtil.randomInt(0, colors.size)]
    }
}