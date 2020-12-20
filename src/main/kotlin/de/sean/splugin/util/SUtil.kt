package de.sean.splugin.util

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.awt.Color
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.HashMap

object SUtil {
    val playerLastActivity = HashMap<UUID, Long>()
    val afkPlayers = HashMap<UUID, Boolean>()
    val colors = arrayOf(Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW)

    fun concatArrayRange(arr: Array<String>, begin: Int, end: Int): String {
        val builder = StringBuilder()
        for (i in begin until end) {
            builder.append(arr[i]).append(" ")
        }
        return builder.toString()
    }

    fun parseStringList(str: String): List<String> {
        val ret: MutableList<String> = ArrayList(listOf(*str.replace("^\\[|]$".toRegex(), "").split(",").toTypedArray()))
        ret.removeIf { obj: String -> obj.isEmpty() }
        return ret
    }

    /**
     * Returns a pseudorandom `int` value between the specified
     * origin (inclusive) and the specified bound (exclusive).
     *
     * @param min the least value returned
     * @param max the upper bound (exclusive)
     * @return a pseudorandom `int` value between the origin
     * (inclusive) and the bound (exclusive)
     * @throws IllegalArgumentException if `origin` is greater than
     * or equal to `bound`
     */
    @Throws(IllegalArgumentException::class)
    fun randomInt(min: Int, max: Int): Int {
        return ThreadLocalRandom.current().nextInt(min, max)
    }

    fun randomColor(): Color {
        return colors[randomInt(0, colors.size)]
    }

    fun getItemStack(num: Int, material: Material?, name: String?): ItemStack {
        return getItemStack(num, material, name, emptyList<String>())
    }

    fun getItemStack(num: Int, material: Material?, name: String?, lore: List<String?>?): ItemStack {
        val stack = ItemStack(material!!, num)
        val meta = stack.itemMeta
        meta!!.setDisplayName(name)
        meta.lore = lore
        stack.itemMeta = meta
        return stack
    }

    fun getPlayerSkull(player: OfflinePlayer): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD, 1)
        val meta = skull.itemMeta as SkullMeta?
        meta!!.owningPlayer = player
        meta.setDisplayName(player.name)
        skull.itemMeta = meta
        return skull
    }

    fun getPlayerSkull(player: Player): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD, 1)
        val meta = skull.itemMeta as SkullMeta?
        meta!!.owningPlayer = player
        meta.setDisplayName(player.displayName)
        skull.itemMeta = meta
        return skull
    }
}
