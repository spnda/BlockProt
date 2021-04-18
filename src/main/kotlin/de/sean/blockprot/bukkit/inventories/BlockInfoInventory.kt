package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.util.Strings
import org.bukkit.Bukkit

object BlockInfoInventory : BlockProtInventory {
    override val size = 9 * 3
    override val inventoryName = Strings.getString("inventories.block_info.name", "Block Info")
}
