/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.command;

import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ViaVersionCommand {
    public void registerSubCommand(ViaSubCommand var1);

    public boolean hasSubCommand(String var1);

    public @Nullable ViaSubCommand getSubCommand(String var1);

    public boolean onCommand(ViaCommandSender var1, String[] var2);

    public List<String> onTabComplete(ViaCommandSender var1, String[] var2);

    public void showHelp(ViaCommandSender var1);
}

