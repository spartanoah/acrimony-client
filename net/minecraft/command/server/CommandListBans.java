/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.command.server;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

public class CommandListBans
extends CommandBase {
    @Override
    public String getCommandName() {
        return "banlist";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return (MinecraftServer.getServer().getConfigurationManager().getBannedIPs().isLanServer() || MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().isLanServer()) && super.canCommandSenderUseCommand(sender);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.banlist.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1 && args[0].equalsIgnoreCase("ips")) {
            sender.addChatMessage(new ChatComponentTranslation("commands.banlist.ips", MinecraftServer.getServer().getConfigurationManager().getBannedIPs().getKeys().length));
            sender.addChatMessage(new ChatComponentText(CommandListBans.joinNiceString(MinecraftServer.getServer().getConfigurationManager().getBannedIPs().getKeys())));
        } else {
            sender.addChatMessage(new ChatComponentTranslation("commands.banlist.players", MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().getKeys().length));
            sender.addChatMessage(new ChatComponentText(CommandListBans.joinNiceString(MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().getKeys())));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? CommandListBans.getListOfStringsMatchingLastWord(args, "players", "ips") : null;
    }
}

