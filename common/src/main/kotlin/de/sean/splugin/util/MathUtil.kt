package de.sean.splugin.util

import java.util.concurrent.ThreadLocalRandom

object MathUtil {
    /**
     * Returns a pseudorandom `int` value between the specified
     * min (inclusive) and the specified max (exclusive).
     *
     * @param min the least value returned
     * @param max the upper bound (exclusive)
     * @return a pseudorandom `int` value between the min
     * (inclusive) and the max (exclusive)
     * @throws IllegalArgumentException if `min` is greater than
     * or equal to `max`
     */
    @Throws(IllegalArgumentException::class)
    fun randomInt(min: Int, max: Int): Int {
        return ThreadLocalRandom.current().nextInt(min, max)
    }
}