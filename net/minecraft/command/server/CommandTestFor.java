/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.command.server;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandTestFor
extends CommandBase {
    @Override
    public String getCommandName() {
        return "testfor";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.testfor.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("commands.testfor.usage", new Object[0]);
        }
        Entity entity = CommandTestFor.func_175768_b(sender, args[0]);
        NBTTagCompound nbttagcompound = null;
        if (args.length >= 2) {
            try {
                nbttagcompound = JsonToNBT.getTagFromJson(CommandTestFor.buildString(args, 1));
            } catch (NBTException nbtexception) {
                throw new CommandException("commands.testfor.tagError", nbtexception.getMessage());
            }
        }
        if (nbttagcompound != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            entity.writeToNBT(nbttagcompound1);
            if (!NBTUtil.func_181123_a(nbttagcompound, nbttagcompound1, true)) {
                throw new CommandException("commands.testfor.failure", entity.getCommandSenderName());
            }
        }
        CommandTestFor.notifyOperators(sender, (ICommand)this, "commands.testfor.success", entity.getCommandSenderName());
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? CommandTestFor.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}

