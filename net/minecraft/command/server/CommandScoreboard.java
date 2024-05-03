/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.command.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class CommandScoreboard
extends CommandBase {
    @Override
    public String getCommandName() {
        return "scoreboard";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.scoreboard.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!this.func_175780_b(sender, args)) {
            if (args.length < 1) {
                throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
            }
            if (args[0].equalsIgnoreCase("objectives")) {
                if (args.length == 1) {
                    throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
                }
                if (args[1].equalsIgnoreCase("list")) {
                    this.listObjectives(sender);
                } else if (args[1].equalsIgnoreCase("add")) {
                    if (args.length < 4) {
                        throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
                    }
                    this.addObjective(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("remove")) {
                    if (args.length != 3) {
                        throw new WrongUsageException("commands.scoreboard.objectives.remove.usage", new Object[0]);
                    }
                    this.removeObjective(sender, args[2]);
                } else {
                    if (!args[1].equalsIgnoreCase("setdisplay")) {
                        throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
                    }
                    if (args.length != 3 && args.length != 4) {
                        throw new WrongUsageException("commands.scoreboard.objectives.setdisplay.usage", new Object[0]);
                    }
                    this.setObjectiveDisplay(sender, args, 2);
                }
            } else if (args[0].equalsIgnoreCase("players")) {
                if (args.length == 1) {
                    throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
                }
                if (args[1].equalsIgnoreCase("list")) {
                    if (args.length > 3) {
                        throw new WrongUsageException("commands.scoreboard.players.list.usage", new Object[0]);
                    }
                    this.listPlayers(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("add")) {
                    if (args.length < 5) {
                        throw new WrongUsageException("commands.scoreboard.players.add.usage", new Object[0]);
                    }
                    this.setPlayer(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("remove")) {
                    if (args.length < 5) {
                        throw new WrongUsageException("commands.scoreboard.players.remove.usage", new Object[0]);
                    }
                    this.setPlayer(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("set")) {
                    if (args.length < 5) {
                        throw new WrongUsageException("commands.scoreboard.players.set.usage", new Object[0]);
                    }
                    this.setPlayer(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("reset")) {
                    if (args.length != 3 && args.length != 4) {
                        throw new WrongUsageException("commands.scoreboard.players.reset.usage", new Object[0]);
                    }
                    this.resetPlayers(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("enable")) {
                    if (args.length != 4) {
                        throw new WrongUsageException("commands.scoreboard.players.enable.usage", new Object[0]);
                    }
                    this.func_175779_n(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("test")) {
                    if (args.length != 5 && args.length != 6) {
                        throw new WrongUsageException("commands.scoreboard.players.test.usage", new Object[0]);
                    }
                    this.func_175781_o(sender, args, 2);
                } else {
                    if (!args[1].equalsIgnoreCase("operation")) {
                        throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
                    }
                    if (args.length != 7) {
                        throw new WrongUsageException("commands.scoreboard.players.operation.usage", new Object[0]);
                    }
                    this.func_175778_p(sender, args, 2);
                }
            } else {
                if (!args[0].equalsIgnoreCase("teams")) {
                    throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
                }
                if (args.length == 1) {
                    throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
                }
                if (args[1].equalsIgnoreCase("list")) {
                    if (args.length > 3) {
                        throw new WrongUsageException("commands.scoreboard.teams.list.usage", new Object[0]);
                    }
                    this.listTeams(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("add")) {
                    if (args.length < 3) {
                        throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
                    }
                    this.addTeam(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("remove")) {
                    if (args.length != 3) {
                        throw new WrongUsageException("commands.scoreboard.teams.remove.usage", new Object[0]);
                    }
                    this.removeTeam(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("empty")) {
                    if (args.length != 3) {
                        throw new WrongUsageException("commands.scoreboard.teams.empty.usage", new Object[0]);
                    }
                    this.emptyTeam(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("join")) {
                    if (!(args.length >= 4 || args.length == 3 && sender instanceof EntityPlayer)) {
                        throw new WrongUsageException("commands.scoreboard.teams.join.usage", new Object[0]);
                    }
                    this.joinTeam(sender, args, 2);
                } else if (args[1].equalsIgnoreCase("leave")) {
                    if (args.length < 3 && !(sender instanceof EntityPlayer)) {
                        throw new WrongUsageException("commands.scoreboard.teams.leave.usage", new Object[0]);
                    }
                    this.leaveTeam(sender, args, 2);
                } else {
                    if (!args[1].equalsIgnoreCase("option")) {
                        throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
                    }
                    if (args.length != 4 && args.length != 5) {
                        throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
                    }
                    this.setTeamOption(sender, args, 2);
                }
            }
        }
    }

    private boolean func_175780_b(ICommandSender p_175780_1_, String[] p_175780_2_) throws CommandException {
        int i = -1;
        for (int j = 0; j < p_175780_2_.length; ++j) {
            if (!this.isUsernameIndex(p_175780_2_, j) || !"*".equals(p_175780_2_[j])) continue;
            if (i >= 0) {
                throw new CommandException("commands.scoreboard.noMultiWildcard", new Object[0]);
            }
            i = j;
        }
        if (i < 0) {
            return false;
        }
        ArrayList<String> list1 = Lists.newArrayList(this.getScoreboard().getObjectiveNames());
        String s = p_175780_2_[i];
        ArrayList<String> list = Lists.newArrayList();
        Iterator iterator = list1.iterator();
        while (iterator.hasNext()) {
            String s1;
            p_175780_2_[i] = s1 = (String)iterator.next();
            try {
                this.processCommand(p_175780_1_, p_175780_2_);
                list.add(s1);
            } catch (CommandException commandexception) {
                ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(commandexception.getMessage(), commandexception.getErrorObjects());
                chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
                p_175780_1_.addChatMessage(chatcomponenttranslation);
            }
        }
        p_175780_2_[i] = s;
        p_175780_1_.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());
        if (list.size() == 0) {
            throw new WrongUsageException("commands.scoreboard.allMatchesFailed", new Object[0]);
        }
        return true;
    }

    protected Scoreboard getScoreboard() {
        return MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
    }

    protected ScoreObjective getObjective(String name, boolean edit) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        ScoreObjective scoreobjective = scoreboard.getObjective(name);
        if (scoreobjective == null) {
            throw new CommandException("commands.scoreboard.objectiveNotFound", name);
        }
        if (edit && scoreobjective.getCriteria().isReadOnly()) {
            throw new CommandException("commands.scoreboard.objectiveReadOnly", name);
        }
        return scoreobjective;
    }

    protected ScorePlayerTeam getTeam(String name) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        ScorePlayerTeam scoreplayerteam = scoreboard.getTeam(name);
        if (scoreplayerteam == null) {
            throw new CommandException("commands.scoreboard.teamNotFound", name);
        }
        return scoreplayerteam;
    }

    protected void addObjective(ICommandSender sender, String[] args, int index) throws CommandException {
        String s = args[index++];
        String s1 = args[index++];
        Scoreboard scoreboard = this.getScoreboard();
        IScoreObjectiveCriteria iscoreobjectivecriteria = IScoreObjectiveCriteria.INSTANCES.get(s1);
        if (iscoreobjectivecriteria == null) {
            throw new WrongUsageException("commands.scoreboard.objectives.add.wrongType", s1);
        }
        if (scoreboard.getObjective(s) != null) {
            throw new CommandException("commands.scoreboard.objectives.add.alreadyExists", s);
        }
        if (s.length() > 16) {
            throw new SyntaxErrorException("commands.scoreboard.objectives.add.tooLong", s, 16);
        }
        if (s.length() == 0) {
            throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
        }
        if (args.length > index) {
            String s2 = CommandScoreboard.getChatComponentFromNthArg(sender, args, index).getUnformattedText();
            if (s2.length() > 32) {
                throw new SyntaxErrorException("commands.scoreboard.objectives.add.displayTooLong", s2, 32);
            }
            if (s2.length() > 0) {
                scoreboard.addScoreObjective(s, iscoreobjectivecriteria).setDisplayName(s2);
            } else {
                scoreboard.addScoreObjective(s, iscoreobjectivecriteria);
            }
        } else {
            scoreboard.addScoreObjective(s, iscoreobjectivecriteria);
        }
        CommandScoreboard.notifyOperators(sender, (ICommand)this, "commands.scoreboard.objectives.add.success", s);
    }

    protected void addTeam(ICommandSender sender, String[] args, int index) throws CommandException {
        String s = args[index++];
        Scoreboard scoreboard = this.getScoreboard();
        if (scoreboard.getTeam(s) != null) {
            throw new CommandException("commands.scoreboard.teams.add.alreadyExists", s);
        }
        if (s.length() > 16) {
            throw new SyntaxErrorException("commands.scoreboard.teams.add.tooLong", s, 16);
        }
        if (s.length() == 0) {
            throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
        }
        if (args.length > index) {
            String s1 = CommandScoreboard.getChatComponentFromNthArg(sender, args, index).getUnformattedText();
            if (s1.length() > 32) {
                throw new SyntaxErrorException("commands.scoreboard.teams.add.displayTooLong", s1, 32);
            }
            if (s1.length() > 0) {
                scoreboard.createTeam(s).setTeamName(s1);
            } else {
                scoreboard.createTeam(s);
            }
        } else {
            scoreboard.createTeam(s);
        }
        CommandScoreboard.notifyOperators(sender, (ICommand)this, "commands.scoreboard.teams.add.success", s);
    }

    protected void setTeamOption(ICommandSender sender, String[] args, int index) throws CommandException {
        ScorePlayerTeam scoreplayerteam;
        if ((scoreplayerteam = this.getTeam(args[index++])) != null) {
            String s;
            if (!((s = args[index++].toLowerCase()).equalsIgnoreCase("color") || s.equalsIgnoreCase("friendlyfire") || s.equalsIgnoreCase("seeFriendlyInvisibles") || s.equalsIgnoreCase("nametagVisibility") || s.equalsIgnoreCase("deathMessageVisibility"))) {
                throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
            }
            if (args.length == 4) {
                if (s.equalsIgnoreCase("color")) {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", s, CommandScoreboard.joinNiceStringFromCollection(EnumChatFormatting.getValidValues(true, false)));
                }
                if (!s.equalsIgnoreCase("friendlyfire") && !s.equalsIgnoreCase("seeFriendlyInvisibles")) {
                    if (!s.equalsIgnoreCase("nametagVisibility") && !s.equalsIgnoreCase("deathMessageVisibility")) {
                        throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
                    }
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", s, CommandScoreboard.joinNiceString(Team.EnumVisible.func_178825_a()));
                }
                throw new WrongUsageException("commands.scoreboard.teams.option.noValue", s, CommandScoreboard.joinNiceStringFromCollection(Arrays.asList("true", "false")));
            }
            String s1 = args[index];
            if (s.equalsIgnoreCase("color")) {
                EnumChatFormatting enumchatformatting = EnumChatFormatting.getValueByName(s1);
                if (enumchatformatting == null || enumchatformatting.isFancyStyling()) {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", s, CommandScoreboard.joinNiceStringFromCollection(EnumChatFormatting.getValidValues(true, false)));
                }
                scoreplayerteam.setChatFormat(enumchatformatting);
                scoreplayerteam.setNamePrefix(enumchatformatting.toString());
                scoreplayerteam.setNameSuffix(EnumChatFormatting.RESET.toString());
            } else if (s.equalsIgnoreCase("friendlyfire")) {
                if (!s1.equalsIgnoreCase("true") && !s1.equalsIgnoreCase("false")) {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", s, CommandScoreboard.joinNiceStringFromCollection(Arrays.asList("true", "false")));
                }
                scoreplayerteam.setAllowFriendlyFire(s1.equalsIgnoreCase("true"));
            } else if (s.equalsIgnoreCase("seeFriendlyInvisibles")) {
                if (!s1.equalsIgnoreCase("true") && !s1.equalsIgnoreCase("false")) {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", s, CommandScoreboard.joinNiceStringFromCollection(Arrays.asList("true", "false")));
                }
                scoreplayerteam.setSeeFriendlyInvisiblesEnabled(s1.equalsIgnoreCase("true"));
            } else if (s.equalsIgnoreCase("nametagVisibility")) {
                Team.EnumVisible team$enumvisible = Team.EnumVisible.func_178824_a(s1);
                if (team$enumvisible == null) {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", s, CommandScoreboard.joinNiceString(Team.EnumVisible.func_178825_a()));
                }
                scoreplayerteam.setNameTagVisibility(team$enumvisible);
            } else if (s.equalsIgnoreCase("deathMessageVisibility")) {
                Team.EnumVisible team$enumvisible1 = Team.EnumVisible.func_178824_a(s1);
                if (team$enumvisible1 == null) {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", s, CommandScoreboard.joinNiceString(Team.EnumVisible.func_178825_a()));
                }
                scoreplayerteam.setDeathMessageVisibility(team$enumvisible1);
            }
            CommandScoreboard.notifyOperators(sender, (ICommand)this, "commands.scoreboard.teams.option.success", s, scoreplayerteam.getRegisteredName(), s1);
        }
    }

    protected void removeTeam(ICommandSender p_147194_1_, String[] p_147194_2_, int p_147194_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        ScorePlayerTeam scoreplayerteam = this.getTeam(p_147194_2_[p_147194_3_]);
        if (scoreplayerteam != null) {
            scoreboard.removeTeam(scoreplayerteam);
            CommandScoreboard.notifyOperators(p_147194_1_, (ICommand)this, "commands.scoreboard.teams.remove.success", scoreplayerteam.getRegisteredName());
        }
    }

    protected void listTeams(ICommandSender p_147186_1_, String[] p_147186_2_, int p_147186_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        if (p_147186_2_.length > p_147186_3_) {
            ScorePlayerTeam scoreplayerteam = this.getTeam(p_147186_2_[p_147186_3_]);
            if (scoreplayerteam == null) {
                return;
            }
            Collection<String> collection = scoreplayerteam.getMembershipCollection();
            p_147186_1_.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection.size());
            if (collection.size() <= 0) {
                throw new CommandException("commands.scoreboard.teams.list.player.empty", scoreplayerteam.getRegisteredName());
            }
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.teams.list.player.count", collection.size(), scoreplayerteam.getRegisteredName());
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147186_1_.addChatMessage(chatcomponenttranslation);
            p_147186_1_.addChatMessage(new ChatComponentText(CommandScoreboard.joinNiceString(collection.toArray())));
        } else {
            Collection<ScorePlayerTeam> collection1 = scoreboard.getTeams();
            p_147186_1_.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection1.size());
            if (collection1.size() <= 0) {
                throw new CommandException("commands.scoreboard.teams.list.empty", new Object[0]);
            }
            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.scoreboard.teams.list.count", collection1.size());
            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147186_1_.addChatMessage(chatcomponenttranslation1);
            for (ScorePlayerTeam scoreplayerteam1 : collection1) {
                p_147186_1_.addChatMessage(new ChatComponentTranslation("commands.scoreboard.teams.list.entry", scoreplayerteam1.getRegisteredName(), scoreplayerteam1.getTeamName(), scoreplayerteam1.getMembershipCollection().size()));
            }
        }
    }

    protected void joinTeam(ICommandSender p_147190_1_, String[] p_147190_2_, int p_147190_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        String s = p_147190_2_[p_147190_3_++];
        HashSet<String> set = Sets.newHashSet();
        HashSet<String> set1 = Sets.newHashSet();
        if (p_147190_1_ instanceof EntityPlayer && p_147190_3_ == p_147190_2_.length) {
            String s4 = CommandScoreboard.getCommandSenderAsPlayer(p_147190_1_).getCommandSenderName();
            if (scoreboard.addPlayerToTeam(s4, s)) {
                set.add(s4);
            } else {
                set1.add(s4);
            }
        } else {
            while (p_147190_3_ < p_147190_2_.length) {
                String s1;
                if ((s1 = p_147190_2_[p_147190_3_++]).startsWith("@")) {
                    for (Entity entity : CommandScoreboard.func_175763_c(p_147190_1_, s1)) {
                        String s3 = CommandScoreboard.getEntityName(p_147190_1_, entity.getUniqueID().toString());
                        if (scoreboard.addPlayerToTeam(s3, s)) {
                            set.add(s3);
                            continue;
                        }
                        set1.add(s3);
                    }
                    continue;
                }
                String s2 = CommandScoreboard.getEntityName(p_147190_1_, s1);
                if (scoreboard.addPlayerToTeam(s2, s)) {
                    set.add(s2);
                    continue;
                }
                set1.add(s2);
            }
        }
        if (!set.isEmpty()) {
            p_147190_1_.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, set.size());
            CommandScoreboard.notifyOperators(p_147190_1_, (ICommand)this, "commands.scoreboard.teams.join.success", set.size(), s, CommandScoreboard.joinNiceString(set.toArray(new String[set.size()])));
        }
        if (!set1.isEmpty()) {
            throw new CommandException("commands.scoreboard.teams.join.failure", set1.size(), s, CommandScoreboard.joinNiceString(set1.toArray(new String[set1.size()])));
        }
    }

    protected void leaveTeam(ICommandSender p_147199_1_, String[] p_147199_2_, int p_147199_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        HashSet<String> set = Sets.newHashSet();
        HashSet<String> set1 = Sets.newHashSet();
        if (p_147199_1_ instanceof EntityPlayer && p_147199_3_ == p_147199_2_.length) {
            String s3 = CommandScoreboard.getCommandSenderAsPlayer(p_147199_1_).getCommandSenderName();
            if (scoreboard.removePlayerFromTeams(s3)) {
                set.add(s3);
            } else {
                set1.add(s3);
            }
        } else {
            while (p_147199_3_ < p_147199_2_.length) {
                String s;
                if ((s = p_147199_2_[p_147199_3_++]).startsWith("@")) {
                    for (Entity entity : CommandScoreboard.func_175763_c(p_147199_1_, s)) {
                        String s2 = CommandScoreboard.getEntityName(p_147199_1_, entity.getUniqueID().toString());
                        if (scoreboard.removePlayerFromTeams(s2)) {
                            set.add(s2);
                            continue;
                        }
                        set1.add(s2);
                    }
                    continue;
                }
                String s1 = CommandScoreboard.getEntityName(p_147199_1_, s);
                if (scoreboard.removePlayerFromTeams(s1)) {
                    set.add(s1);
                    continue;
                }
                set1.add(s1);
            }
        }
        if (!set.isEmpty()) {
            p_147199_1_.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, set.size());
            CommandScoreboard.notifyOperators(p_147199_1_, (ICommand)this, "commands.scoreboard.teams.leave.success", set.size(), CommandScoreboard.joinNiceString(set.toArray(new String[set.size()])));
        }
        if (!set1.isEmpty()) {
            throw new CommandException("commands.scoreboard.teams.leave.failure", set1.size(), CommandScoreboard.joinNiceString(set1.toArray(new String[set1.size()])));
        }
    }

    protected void emptyTeam(ICommandSender p_147188_1_, String[] p_147188_2_, int p_147188_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        ScorePlayerTeam scoreplayerteam = this.getTeam(p_147188_2_[p_147188_3_]);
        if (scoreplayerteam != null) {
            ArrayList<String> collection = Lists.newArrayList(scoreplayerteam.getMembershipCollection());
            p_147188_1_.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, collection.size());
            if (collection.isEmpty()) {
                throw new CommandException("commands.scoreboard.teams.empty.alreadyEmpty", scoreplayerteam.getRegisteredName());
            }
            for (String s : collection) {
                scoreboard.removePlayerFromTeam(s, scoreplayerteam);
            }
            CommandScoreboard.notifyOperators(p_147188_1_, (ICommand)this, "commands.scoreboard.teams.empty.success", collection.size(), scoreplayerteam.getRegisteredName());
        }
    }

    protected void removeObjective(ICommandSender p_147191_1_, String p_147191_2_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        ScoreObjective scoreobjective = this.getObjective(p_147191_2_, false);
        scoreboard.removeObjective(scoreobjective);
        CommandScoreboard.notifyOperators(p_147191_1_, (ICommand)this, "commands.scoreboard.objectives.remove.success", p_147191_2_);
    }

    protected void listObjectives(ICommandSender p_147196_1_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        Collection<ScoreObjective> collection = scoreboard.getScoreObjectives();
        if (collection.size() <= 0) {
            throw new CommandException("commands.scoreboard.objectives.list.empty", new Object[0]);
        }
        ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.objectives.list.count", collection.size());
        chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
        p_147196_1_.addChatMessage(chatcomponenttranslation);
        for (ScoreObjective scoreobjective : collection) {
            p_147196_1_.addChatMessage(new ChatComponentTranslation("commands.scoreboard.objectives.list.entry", scoreobjective.getName(), scoreobjective.getDisplayName(), scoreobjective.getCriteria().getName()));
        }
    }

    protected void setObjectiveDisplay(ICommandSender p_147198_1_, String[] p_147198_2_, int p_147198_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        String s = p_147198_2_[p_147198_3_++];
        int i = Scoreboard.getObjectiveDisplaySlotNumber(s);
        ScoreObjective scoreobjective = null;
        if (p_147198_2_.length == 4) {
            scoreobjective = this.getObjective(p_147198_2_[p_147198_3_], false);
        }
        if (i < 0) {
            throw new CommandException("commands.scoreboard.objectives.setdisplay.invalidSlot", s);
        }
        scoreboard.setObjectiveInDisplaySlot(i, scoreobjective);
        if (scoreobjective != null) {
            CommandScoreboard.notifyOperators(p_147198_1_, (ICommand)this, "commands.scoreboard.objectives.setdisplay.successSet", Scoreboard.getObjectiveDisplaySlot(i), scoreobjective.getName());
        } else {
            CommandScoreboard.notifyOperators(p_147198_1_, (ICommand)this, "commands.scoreboard.objectives.setdisplay.successCleared", Scoreboard.getObjectiveDisplaySlot(i));
        }
    }

    protected void listPlayers(ICommandSender p_147195_1_, String[] p_147195_2_, int p_147195_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        if (p_147195_2_.length > p_147195_3_) {
            String s = CommandScoreboard.getEntityName(p_147195_1_, p_147195_2_[p_147195_3_]);
            Map<ScoreObjective, Score> map = scoreboard.getObjectivesForEntity(s);
            p_147195_1_.setCommandStat(CommandResultStats.Type.QUERY_RESULT, map.size());
            if (map.size() <= 0) {
                throw new CommandException("commands.scoreboard.players.list.player.empty", s);
            }
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.players.list.player.count", map.size(), s);
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147195_1_.addChatMessage(chatcomponenttranslation);
            for (Score score : map.values()) {
                p_147195_1_.addChatMessage(new ChatComponentTranslation("commands.scoreboard.players.list.player.entry", score.getScorePoints(), score.getObjective().getDisplayName(), score.getObjective().getName()));
            }
        } else {
            Collection<String> collection = scoreboard.getObjectiveNames();
            p_147195_1_.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection.size());
            if (collection.size() <= 0) {
                throw new CommandException("commands.scoreboard.players.list.empty", new Object[0]);
            }
            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.scoreboard.players.list.count", collection.size());
            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147195_1_.addChatMessage(chatcomponenttranslation1);
            p_147195_1_.addChatMessage(new ChatComponentText(CommandScoreboard.joinNiceString(collection.toArray())));
        }
    }

    protected void setPlayer(ICommandSender p_147197_1_, String[] p_147197_2_, int p_147197_3_) throws CommandException {
        int j;
        String s1;
        String s = p_147197_2_[p_147197_3_ - 1];
        int i = p_147197_3_;
        if ((s1 = CommandScoreboard.getEntityName(p_147197_1_, p_147197_2_[p_147197_3_++])).length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", s1, 40);
        }
        ScoreObjective scoreobjective = this.getObjective(p_147197_2_[p_147197_3_++], true);
        int n = j = s.equalsIgnoreCase("set") ? CommandScoreboard.parseInt(p_147197_2_[p_147197_3_++]) : CommandScoreboard.parseInt(p_147197_2_[p_147197_3_++], 0);
        if (p_147197_2_.length > p_147197_3_) {
            Entity entity = CommandScoreboard.func_175768_b(p_147197_1_, p_147197_2_[i]);
            try {
                NBTTagCompound nbttagcompound = JsonToNBT.getTagFromJson(CommandScoreboard.buildString(p_147197_2_, p_147197_3_));
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                entity.writeToNBT(nbttagcompound1);
                if (!NBTUtil.func_181123_a(nbttagcompound, nbttagcompound1, true)) {
                    throw new CommandException("commands.scoreboard.players.set.tagMismatch", s1);
                }
            } catch (NBTException nbtexception) {
                throw new CommandException("commands.scoreboard.players.set.tagError", nbtexception.getMessage());
            }
        }
        Scoreboard scoreboard = this.getScoreboard();
        Score score = scoreboard.getValueFromObjective(s1, scoreobjective);
        if (s.equalsIgnoreCase("set")) {
            score.setScorePoints(j);
        } else if (s.equalsIgnoreCase("add")) {
            score.increseScore(j);
        } else {
            score.decreaseScore(j);
        }
        CommandScoreboard.notifyOperators(p_147197_1_, (ICommand)this, "commands.scoreboard.players.set.success", scoreobjective.getName(), s1, score.getScorePoints());
    }

    protected void resetPlayers(ICommandSender p_147187_1_, String[] p_147187_2_, int p_147187_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        String s = CommandScoreboard.getEntityName(p_147187_1_, p_147187_2_[p_147187_3_++]);
        if (p_147187_2_.length > p_147187_3_) {
            ScoreObjective scoreobjective = this.getObjective(p_147187_2_[p_147187_3_++], false);
            scoreboard.removeObjectiveFromEntity(s, scoreobjective);
            CommandScoreboard.notifyOperators(p_147187_1_, (ICommand)this, "commands.scoreboard.players.resetscore.success", scoreobjective.getName(), s);
        } else {
            scoreboard.removeObjectiveFromEntity(s, null);
            CommandScoreboard.notifyOperators(p_147187_1_, (ICommand)this, "commands.scoreboard.players.reset.success", s);
        }
    }

    protected void func_175779_n(ICommandSender p_175779_1_, String[] p_175779_2_, int p_175779_3_) throws CommandException {
        String s;
        Scoreboard scoreboard = this.getScoreboard();
        if ((s = CommandScoreboard.getPlayerName(p_175779_1_, p_175779_2_[p_175779_3_++])).length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", s, 40);
        }
        ScoreObjective scoreobjective = this.getObjective(p_175779_2_[p_175779_3_], false);
        if (scoreobjective.getCriteria() != IScoreObjectiveCriteria.TRIGGER) {
            throw new CommandException("commands.scoreboard.players.enable.noTrigger", scoreobjective.getName());
        }
        Score score = scoreboard.getValueFromObjective(s, scoreobjective);
        score.setLocked(false);
        CommandScoreboard.notifyOperators(p_175779_1_, (ICommand)this, "commands.scoreboard.players.enable.success", scoreobjective.getName(), s);
    }

    protected void func_175781_o(ICommandSender p_175781_1_, String[] p_175781_2_, int p_175781_3_) throws CommandException {
        ScoreObjective scoreobjective;
        String s;
        Scoreboard scoreboard = this.getScoreboard();
        if ((s = CommandScoreboard.getEntityName(p_175781_1_, p_175781_2_[p_175781_3_++])).length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", s, 40);
        }
        if (!scoreboard.entityHasObjective(s, scoreobjective = this.getObjective(p_175781_2_[p_175781_3_++], false))) {
            throw new CommandException("commands.scoreboard.players.test.notFound", scoreobjective.getName(), s);
        }
        int i = p_175781_2_[p_175781_3_].equals("*") ? Integer.MIN_VALUE : CommandScoreboard.parseInt(p_175781_2_[p_175781_3_]);
        int j = ++p_175781_3_ < p_175781_2_.length && !p_175781_2_[p_175781_3_].equals("*") ? CommandScoreboard.parseInt(p_175781_2_[p_175781_3_], i) : Integer.MAX_VALUE;
        Score score = scoreboard.getValueFromObjective(s, scoreobjective);
        if (score.getScorePoints() < i || score.getScorePoints() > j) {
            throw new CommandException("commands.scoreboard.players.test.failed", score.getScorePoints(), i, j);
        }
        CommandScoreboard.notifyOperators(p_175781_1_, (ICommand)this, "commands.scoreboard.players.test.success", score.getScorePoints(), i, j);
    }

    protected void func_175778_p(ICommandSender p_175778_1_, String[] p_175778_2_, int p_175778_3_) throws CommandException {
        Scoreboard scoreboard = this.getScoreboard();
        String s = CommandScoreboard.getEntityName(p_175778_1_, p_175778_2_[p_175778_3_++]);
        ScoreObjective scoreobjective = this.getObjective(p_175778_2_[p_175778_3_++], true);
        String s1 = p_175778_2_[p_175778_3_++];
        String s2 = CommandScoreboard.getEntityName(p_175778_1_, p_175778_2_[p_175778_3_++]);
        ScoreObjective scoreobjective1 = this.getObjective(p_175778_2_[p_175778_3_], false);
        if (s.length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", s, 40);
        }
        if (s2.length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", s2, 40);
        }
        Score score = scoreboard.getValueFromObjective(s, scoreobjective);
        if (!scoreboard.entityHasObjective(s2, scoreobjective1)) {
            throw new CommandException("commands.scoreboard.players.operation.notFound", scoreobjective1.getName(), s2);
        }
        Score score1 = scoreboard.getValueFromObjective(s2, scoreobjective1);
        if (s1.equals("+=")) {
            score.setScorePoints(score.getScorePoints() + score1.getScorePoints());
        } else if (s1.equals("-=")) {
            score.setScorePoints(score.getScorePoints() - score1.getScorePoints());
        } else if (s1.equals("*=")) {
            score.setScorePoints(score.getScorePoints() * score1.getScorePoints());
        } else if (s1.equals("/=")) {
            if (score1.getScorePoints() != 0) {
                score.setScorePoints(score.getScorePoints() / score1.getScorePoints());
            }
        } else if (s1.equals("%=")) {
            if (score1.getScorePoints() != 0) {
                score.setScorePoints(score.getScorePoints() % score1.getScorePoints());
            }
        } else if (s1.equals("=")) {
            score.setScorePoints(score1.getScorePoints());
        } else if (s1.equals("<")) {
            score.setScorePoints(Math.min(score.getScorePoints(), score1.getScorePoints()));
        } else if (s1.equals(">")) {
            score.setScorePoints(Math.max(score.getScorePoints(), score1.getScorePoints()));
        } else {
            if (!s1.equals("><")) {
                throw new CommandException("commands.scoreboard.players.operation.invalidOperation", s1);
            }
            int i = score.getScorePoints();
            score.setScorePoints(score1.getScorePoints());
            score1.setScorePoints(i);
        }
        CommandScoreboard.notifyOperators(p_175778_1_, (ICommand)this, "commands.scoreboard.players.operation.success", new Object[0]);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return CommandScoreboard.getListOfStringsMatchingLastWord(args, "objectives", "players", "teams");
        }
        if (args[0].equalsIgnoreCase("objectives")) {
            if (args.length == 2) {
                return CommandScoreboard.getListOfStringsMatchingLastWord(args, "list", "add", "remove", "setdisplay");
            }
            if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 4) {
                    Set<String> set = IScoreObjectiveCriteria.INSTANCES.keySet();
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, set);
                }
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (args.length == 3) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.func_147184_a(false));
                }
            } else if (args[1].equalsIgnoreCase("setdisplay")) {
                if (args.length == 3) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, Scoreboard.getDisplaySlotStrings());
                }
                if (args.length == 4) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.func_147184_a(false));
                }
            }
        } else if (args[0].equalsIgnoreCase("players")) {
            if (args.length == 2) {
                return CommandScoreboard.getListOfStringsMatchingLastWord(args, "set", "add", "remove", "reset", "list", "enable", "test", "operation");
            }
            if (!(args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("reset"))) {
                if (args[1].equalsIgnoreCase("enable")) {
                    if (args.length == 3) {
                        return CommandScoreboard.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                    }
                    if (args.length == 4) {
                        return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.func_175782_e());
                    }
                } else if (!args[1].equalsIgnoreCase("list") && !args[1].equalsIgnoreCase("test")) {
                    if (args[1].equalsIgnoreCase("operation")) {
                        if (args.length == 3) {
                            return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.getScoreboard().getObjectiveNames());
                        }
                        if (args.length == 4) {
                            return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.func_147184_a(true));
                        }
                        if (args.length == 5) {
                            return CommandScoreboard.getListOfStringsMatchingLastWord(args, "+=", "-=", "*=", "/=", "%=", "=", "<", ">", "><");
                        }
                        if (args.length == 6) {
                            return CommandScoreboard.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                        }
                        if (args.length == 7) {
                            return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.func_147184_a(false));
                        }
                    }
                } else {
                    if (args.length == 3) {
                        return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.getScoreboard().getObjectiveNames());
                    }
                    if (args.length == 4 && args[1].equalsIgnoreCase("test")) {
                        return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.func_147184_a(false));
                    }
                }
            } else {
                if (args.length == 3) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                }
                if (args.length == 4) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.func_147184_a(true));
                }
            }
        } else if (args[0].equalsIgnoreCase("teams")) {
            if (args.length == 2) {
                return CommandScoreboard.getListOfStringsMatchingLastWord(args, "add", "remove", "join", "leave", "empty", "list", "option");
            }
            if (args[1].equalsIgnoreCase("join")) {
                if (args.length == 3) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.getScoreboard().getTeamNames());
                }
                if (args.length >= 4) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                }
            } else {
                if (args[1].equalsIgnoreCase("leave")) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                }
                if (!(args[1].equalsIgnoreCase("empty") || args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("remove"))) {
                    if (args[1].equalsIgnoreCase("option")) {
                        if (args.length == 3) {
                            return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.getScoreboard().getTeamNames());
                        }
                        if (args.length == 4) {
                            return CommandScoreboard.getListOfStringsMatchingLastWord(args, "color", "friendlyfire", "seeFriendlyInvisibles", "nametagVisibility", "deathMessageVisibility");
                        }
                        if (args.length == 5) {
                            if (args[3].equalsIgnoreCase("color")) {
                                return CommandScoreboard.getListOfStringsMatchingLastWord(args, EnumChatFormatting.getValidValues(true, false));
                            }
                            if (args[3].equalsIgnoreCase("nametagVisibility") || args[3].equalsIgnoreCase("deathMessageVisibility")) {
                                return CommandScoreboard.getListOfStringsMatchingLastWord(args, Team.EnumVisible.func_178825_a());
                            }
                            if (args[3].equalsIgnoreCase("friendlyfire") || args[3].equalsIgnoreCase("seeFriendlyInvisibles")) {
                                return CommandScoreboard.getListOfStringsMatchingLastWord(args, "true", "false");
                            }
                        }
                    }
                } else if (args.length == 3) {
                    return CommandScoreboard.getListOfStringsMatchingLastWord(args, this.getScoreboard().getTeamNames());
                }
            }
        }
        return null;
    }

    protected List<String> func_147184_a(boolean p_147184_1_) {
        Collection<ScoreObjective> collection = this.getScoreboard().getScoreObjectives();
        ArrayList<String> list = Lists.newArrayList();
        for (ScoreObjective scoreobjective : collection) {
            if (p_147184_1_ && scoreobjective.getCriteria().isReadOnly()) continue;
            list.add(scoreobjective.getName());
        }
        return list;
    }

    protected List<String> func_175782_e() {
        Collection<ScoreObjective> collection = this.getScoreboard().getScoreObjectives();
        ArrayList<String> list = Lists.newArrayList();
        for (ScoreObjective scoreobjective : collection) {
            if (scoreobjective.getCriteria() != IScoreObjectiveCriteria.TRIGGER) continue;
            list.add(scoreobjective.getName());
        }
        return list;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return !args[0].equalsIgnoreCase("players") ? (args[0].equalsIgnoreCase("teams") ? index == 2 : false) : (args.length > 1 && args[1].equalsIgnoreCase("operation") ? index == 2 || index == 5 : index == 2);
    }
}

