/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.command;

import net.minecraft.command.CommandException;

public class CommandNotFoundException
extends CommandException {
    public CommandNotFoundException() {
        this("commands.generic.notFound", new Object[0]);
    }

    public CommandNotFoundException(String p_i1363_1_, Object ... p_i1363_2_) {
        super(p_i1363_1_, p_i1363_2_);
    }
}

