/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.command;

import net.minecraft.command.CommandException;

public class NumberInvalidException
extends CommandException {
    public NumberInvalidException() {
        this("commands.generic.num.invalid", new Object[0]);
    }

    public NumberInvalidException(String message, Object ... replacements) {
        super(message, replacements);
    }
}

