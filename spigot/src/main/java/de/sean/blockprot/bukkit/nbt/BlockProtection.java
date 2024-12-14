package de.sean.blockprot.bukkit.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BlockProtection {
    private BlockProtection() {}

    public static void modify(@NotNull final BlockState block, final Consumer<BlockNBTHandler> consumer) {
        NBT.modify(block, nbt -> {
            consumer.accept(new BlockNBTHandler(nbt));
        });
    }
}
