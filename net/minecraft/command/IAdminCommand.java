/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.command;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public interface IAdminCommand {
    public void notifyOperators(ICommandSender var1, ICommand var2, int var3, String var4, Object ... var5);
}

