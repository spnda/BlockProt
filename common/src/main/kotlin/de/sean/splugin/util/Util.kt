package de.sean.splugin.util

object Util {
    /**
     * Concatenates a Array<String> into one long String
     * @param arr The Array to concatenate
     * @param begin The begin index inside of the given Array. Defaults to 0
     * @param end The end index inside of the given Array. Defaults to the size of the Array minus 1
     * @throws IllegalArgumentException if the `begin` is larger than `end` or if `end` is larger than the array's size
     */
    fun concatArrayRange(arr: Array<String>, begin: Int = 0, end: Int = arr.size - 1): String {
        if (begin < 0) throw java.lang.IllegalArgumentException("begin cannot be lower than 0")
        if (begin > end) throw IllegalArgumentException("begin cannot be larger than end")
        if (end > arr.size - 1) throw IllegalArgumentException("end index cannot be larger than the size of the array")
        val builder = StringBuilder()
        for (i in begin until end) {
            builder.append(arr[i]).append(" ")
        }
        return builder.toString()
    }
}
