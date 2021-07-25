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

package de.sean.blockprot.fabric.translation;

import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

public enum TranslationIdentifier {
    SCREEN_LOCK("screen.blockprot.block_lock"),
    SCREEN_FRIENDS_PERMISSION_READ("screen.blockprot.friends.permission.read"),
    SCREEN_FRIENDS_PERMISSION_WRITE("screen.blockprot.friends.permission.write"),
    SCREEN_BACK("screen.blockprot.back"),
    MESSAGES_UNLOCKED("messages.blockprot.unlocked"),
    MESSAGES_LOCKED("messages.blockprot.locked"),
    MESSAGES_PERMISSION_GRANTED("messages.blockprot.permission_granted"),
    MESSAGES_NO_PERMISSION("messages.blockprot.no_permission");

    private final String identifier;

    TranslationIdentifier(@NotNull final String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public TranslatableText asTranslatableText() {
        return new TranslatableText(this.identifier);
    }
}
