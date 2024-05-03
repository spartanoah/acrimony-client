/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.util;

import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatComponentScore;
import net.minecraft.util.ChatComponentSelector;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

public class ChatComponentProcessor {
    public static IChatComponent processComponent(ICommandSender commandSender, IChatComponent component, Entity entityIn) throws CommandException {
        IChatComponent ichatcomponent = null;
        if (component instanceof ChatComponentScore) {
            ChatComponentScore chatcomponentscore = (ChatComponentScore)component;
            String s = chatcomponentscore.getName();
            if (PlayerSelector.hasArguments(s)) {
                List<Entity> list = PlayerSelector.matchEntities(commandSender, s, Entity.class);
                if (list.size() != 1) {
                    throw new EntityNotFoundException();
                }
                s = list.get(0).getCommandSenderName();
            }
            ichatcomponent = entityIn != null && s.equals("*") ? new ChatComponentScore(entityIn.getCommandSenderName(), chatcomponentscore.getObjective()) : new ChatComponentScore(s, chatcomponentscore.getObjective());
            ((ChatComponentScore)ichatcomponent).setValue(chatcomponentscore.getUnformattedTextForChat());
        } else if (component instanceof ChatComponentSelector) {
            String s1 = ((ChatComponentSelector)component).getSelector();
            ichatcomponent = PlayerSelector.matchEntitiesToChatComponent(commandSender, s1);
            if (ichatcomponent == null) {
                ichatcomponent = new ChatComponentText("");
            }
        } else if (component instanceof ChatComponentText) {
            ichatcomponent = new ChatComponentText(((ChatComponentText)component).getChatComponentText_TextValue());
        } else {
            if (!(component instanceof ChatComponentTranslation)) {
                return component;
            }
            Object[] aobject = ((ChatComponentTranslation)component).getFormatArgs();
            for (int i = 0; i < aobject.length; ++i) {
                Object object = aobject[i];
                if (!(object instanceof IChatComponent)) continue;
                aobject[i] = ChatComponentProcessor.processComponent(commandSender, (IChatComponent)object, entityIn);
            }
            ichatcomponent = new ChatComponentTranslation(((ChatComponentTranslation)component).getKey(), aobject);
        }
        ChatStyle chatstyle = component.getChatStyle();
        if (chatstyle != null) {
            ichatcomponent.setChatStyle(chatstyle.createShallowCopy());
        }
        for (IChatComponent ichatcomponent1 : component.getSiblings()) {
            ichatcomponent.appendSibling(ChatComponentProcessor.processComponent(commandSender, ichatcomponent1, entityIn));
        }
        return ichatcomponent;
    }
}

