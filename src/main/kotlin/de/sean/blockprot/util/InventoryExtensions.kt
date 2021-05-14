package de.sean.blockprot.util

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import org.bukkit.Material
import org.bukkit.inventory.Inventory

/**
 * Sets the back button to the last item in the inventory.
 */
fun Inventory.setBackButton() = setBackButton(size - 1)

/**
 * Sets the back button to the [index] in the inventory.
 */
fun Inventory.setBackButton(index: Int) {
    setItem(
        index,
        ItemUtil.getItemStack(
            1,
            Material.BLACK_STAINED_GLASS_PANE,
            Translator.get(TranslationKey.INVENTORIES__BACK)
        )
    )
}
