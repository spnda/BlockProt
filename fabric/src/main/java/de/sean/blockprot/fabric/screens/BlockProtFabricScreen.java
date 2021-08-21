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

package de.sean.blockprot.fabric.screens;

import de.sean.blockprot.fabric.translation.TranslationIdentifier;
import de.sean.blockprot.screens.BlockProtScreen;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public abstract class BlockProtFabricScreen extends SimpleGui implements BlockProtScreen<Item, TranslationIdentifier> {
    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type                        the screen handler that the client should display
     * @param player                      the player to server this gui to
     * @param includePlayerInventorySlots if <code>true</code> the players inventory
     */
    public BlockProtFabricScreen(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean includePlayerInventorySlots) {
        super(type, player, includePlayerInventorySlots);

        this.setTitle(new LiteralText(this.getDefaultScreenName()));
    }

    @Override
    public void setBackButton() {
        setBackButton(getSize() - 1);
    }

    @Override
    public void setBackButton(int index) {
        setItemStack(index, Items.BLACK_STAINED_GLASS_PANE, TranslationIdentifier.SCREEN_BACK);
    }

    @Override
    public void setItemStack(int index, Item item) {
        setItemStack(index, item, "");
    }

    @Override
    public void setItemStack(int index, Item item, TranslationIdentifier identifier) {

    }

    @Override
    public void setItemStack(int index, Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.setCustomName(new LiteralText(name));
        setSlot(index, stack);
    }
}
