/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.command;

import net.minecraft.command.CommandException;

public class PlayerNotFoundException
extends CommandException {
    public PlayerNotFoundException() {
        this("commands.generic.player.notFound", new Object[0]);
    }

    public PlayerNotFoundException(String message, Object ... replacements) {
        super(message, replacements);
    }
}

