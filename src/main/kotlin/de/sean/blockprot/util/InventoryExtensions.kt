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
fun Inventory.setBackButton(index: Int) = setItemStack(
    index,
    Material.BLACK_STAINED_GLASS_PANE,
    TranslationKey.INVENTORIES__BACK
)

/**
 * Sets a ItemStack with the type [material] and the name as [key] at [index].
 */
fun Inventory.setItemStack(index: Int, material: Material, key: TranslationKey) = setItem(
    index,
    ItemUtil.getItemStack(
        1,
        material,
        Translator.get(key)
    )
)
