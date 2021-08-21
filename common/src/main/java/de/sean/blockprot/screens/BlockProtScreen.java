/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sean.blockprot.screens;

/**
 * Interface with base methods for any inventory screen.
 *
 * @param <I> The type of ItemStack.
 * @param <T> The translation key class.
 */
public interface BlockProtScreen<I, T> {
    /**
     * Get the default size of a single row. This defaults to 9, the width
     * of a chest, barrel and most other "menu" screens. Can be overridden
     * to have a container similar to a dispenser or hopper.
     *
     * @return By default the size of a single row, 9.
     */
    default int getRowSize() {
        return 9;
    }

    /**
     * Gets the complete size of the inventory, multiplying {@link #getRowSize()}
     * with {@link #getRows()}.
     * 
     * @return The complete size of the inventory.
     */
    default int getSize() {
        return getRowSize() * getRows();
    }

    /**
     * Get the size of this inventory, which should be a multiple of 9 with a maximum value of 6 *
     * 9.
     *
     * @return The size of this inventory.
     * @since 0.2.2
     */
    int getRows();

    /**
     * Get the default inventory name.
     *
     * @return The translated inventory name, or this class's simple name if the translation was not
     * found.
     */
    String getDefaultScreenName();

    /**
     * Sets the back button to the last item in the inventory.
     *
     * @since 0.2.3
     */
    void setBackButton();

    /**
     * Sets the back button to the [index] in the inventory.
     *
     * @param index The index of the back button inside this inventory.
     * @since 0.2.3
     */
    void setBackButton(int index);

    /**
     * Sets a ItemStack {@code item} at {@code index}.
     *
     * @param index The index of the item inside this inventory.
     * @param item  The item.
     * @since 0.2.3
     */
    void setItemStack(int index, I item);

    /**
     * Sets a ItemStack with the type [material] and the name translated by [key] at [index].
     *
     * @param index The index of the item inside this inventory.
     * @param item  The material of the item.
     * @param key   The translation key to use to get the translated name for this item.
     * @since 0.2.3
     */
    void setItemStack(int index, I item, T key);

    /**
     * Sets a ItemStack with the type {@code material} and the name as {@code text} at {@code index}.
     *
     * @param index The index of the item inside this inventory.
     * @param item  The material of the item.
     * @param text  The text or name of the item.
     * @since 0.4.0
     */
    void setItemStack(int index, I item, String text);
}
