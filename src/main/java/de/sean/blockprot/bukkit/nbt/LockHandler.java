/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.sean.blockprot.bukkit.nbt;

import de.tr7zw.changeme.nbtapi.NBTCompound;

public abstract class LockHandler<T extends NBTCompound> {
    static final String OWNER_ATTRIBUTE = "splugin_owner";
    static final String LOCK_ATTRIBUTE = "splugin_lock";
    static final String REDSTONE_ATTRIBUTE = "splugin_lock_redstone";
    static final String LOCK_ON_PLACE_ATTRIBUTE = "splugin_lock_on_place";
    static final String DEFAULT_FRIENDS_ATTRIBUTE = "blockprot_default_friends";
    static final String ACCESS_FLAGS_ATTRIBUTE = "blockprot_access_flags";

    public static final String PERMISSION_LOCK = "blockprot.lock";
    public static final String PERMISSION_INFO = "blockprot.info";
    public static final String PERMISSION_ADMIN = "blockprot.admin";
    public static final String PERMISSION_BYPASS = "blockprot.bypass";

    public T container;

    protected LockHandler() { }
}
