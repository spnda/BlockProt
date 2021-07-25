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

package de.sean.blockprot.fabric.mixin;

import de.sean.blockprot.fabric.ext.BlockEntityNbtExtension;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class MixinBlockEntity implements BlockEntityNbtExtension {
    @Unique
    @Final
    private static final String NBT_BASE_KEY = "blockprot_nbt";

    @Mutable
    @Unique
    private NbtCompound customNbt = new NbtCompound();

    /**
     * Write to the NBT just before returning it.
     */
    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeToNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        nbt.put(NBT_BASE_KEY, customNbt);
    }

    /**
     * Read from the nbt.
     */
    @Inject(method = "readNbt", at = @At("RETURN"))
    private void readFromNbt(NbtCompound tag, CallbackInfo ci) {
        customNbt = tag.getCompound(NBT_BASE_KEY);
    }

    @Override
    public boolean contains(String key) {
        return customNbt.contains(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return customNbt.getBoolean(key);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        customNbt.putBoolean(key, value);
    }

    @Override
    public String getString(String key) {
        return customNbt.getString(key);
    }

    @Override
    public void putString(String key, String value) {
        customNbt.putString(key, value);
    }

    @Override
    public NbtCompound getCompound(String key) {
        return customNbt.getCompound(key);
    }
}
