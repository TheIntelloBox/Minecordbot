package us.cyrien.minecordbot.commands.discordCommand;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import us.cyrien.mcutils.logger.Logger;
import us.cyrien.minecordbot.Bot;
import us.cyrien.minecordbot.Minecordbot;
import us.cyrien.minecordbot.commands.MCBCommand;
import us.cyrien.minecordbot.localization.Locale;
import us.cyrien.minecordbot.utils.FinderUtil;

import java.util.ArrayList;

public class TextChannelCmd extends MCBCommand {

    public TextChannelCmd(Minecordbot minecordbot) {
        super(minecordbot);
        this.name = "textchannel";
        this.aliases = new String[]{"tchannel", "tc"};
        this.arguments = "<list | add | remove> [sub command args]...";
        this.help = Locale.getCommandsMessage("textchannel.description").finish();
        this.category = Bot.ADMIN;
        this.children = new Command[]{new Add(minecordbot), new List(minecordbot), new Remove(minecordbot)};
        this.type = Type.EMBED;
    }

    @Override
    protected void doCommand(CommandEvent e) {

    }

    private class List extends MCBCommand {

        public List(Minecordbot minecordbot) {
            super(minecordbot);
            this.name = "list";
            this.help = Locale.getCommandsMessage("textchannel.subcommand.list.description").finish();
            this.category = Bot.MISC;
        }

        @Override
        protected void doCommand(CommandEvent e) {
            MessageEmbed embed = generateListEmbed(e);
            respond(e, embed, ResponseLevel.DEFAULT);
        }

        private MessageEmbed generateListEmbed(CommandEvent e) {
            String path = "textchannel.list.";
            EmbedBuilder eb = new EmbedBuilder();
            eb.setDescription(Locale.getCommandsMessage((path + "header")).finish());
            eb.setColor(e.getGuild().getMember(e.getJDA().getSelfUser()).getColor());
            java.util.List<TextChannel> tcArray = mcb.getRelayChannels();
            tcArray.forEach((tc) -> {
                String gName = tc.getGuild().getName();
                String tcName = tc.getName();
                String str = Locale.getCommandsMessage(path + "guild_name").finish() + ": " + gName + "\n";
                str += Locale.getCommandsMessage(path + "channel_name").finish() + ": " + tcName;
                eb.addField("[" + tc + "]" + ": ", str, false);
            });
            return eb.build();
        }
    }

    private class Add extends MCBCommand {

        public Add(Minecordbot minecordbot) {
            super(minecordbot);
            this.name = "add";
            this.arguments = "<text channel id or channel name>";
            this.help = Locale.getCommandsMessage("textchannel.subcommand.add.description").finish();
            this.category = Bot.ADMIN;
        }

        @Override
        protected void doCommand(CommandEvent e) {
            addTextChannel(e);
        }

        private void addTextChannel(CommandEvent e) {
            String arg = e.getArgs();
            java.util.List<TextChannel> tcArray = mcb.getRelayChannels();
            String response;
            boolean withID = StringUtils.isNumeric(arg) && e.getJDA().getTextChannelById(arg) != null;
            TextChannel tc;
            if (withID) {
                tc = e.getJDA().getTextChannelById(arg);
            } else {
                java.util.List<TextChannel> result = FinderUtil.findTextChannel(arg, e.getGuild());
                tc = result.size() > 0 ? result.get(0) : null;
            }
            if (tc != null) {
                tcArray.add(tc);
                configsManager.getChatConfig().set("Relay_Channels", asStringList(tcArray));
                configsManager.getChatConfig().saveConfig();
            } else {
                response = Locale.getCommandsMessage("textchannel.invalid-tc").finish();
                respond(e, String.format(response, arg), ResponseLevel.LEVEL_3);
                return;
            }
            response = Locale.getCommandsMessage("textchannel.added-tc").finish();
            respond(e, String.format(response, arg), ResponseLevel.LEVEL_1);
            Logger.info("Added text channel " + arg);
        }

        private java.util.List<String> asStringList(java.util.List<TextChannel> tcs) {
            java.util.List<String> strs = new ArrayList<>();
            for(TextChannel c : tcs) {
                strs.add(c.getId());
            }
            return strs;
        }
    }

    private class Remove extends MCBCommand {

        public Remove(Minecordbot minecordbot) {
            super(minecordbot);
            this.name = "remove";
            this.arguments = "<text channel id or channel name>";
            this.help = Locale.getCommandsMessage("textchannel.subcommand.remove.description").finish();
            this.category = Bot.ADMIN;
        }

        @Override
        protected void doCommand(CommandEvent e) {
            removeTextChannel(e);
        }

        private void removeTextChannel(CommandEvent e) {
            String arg = e.getArgs();
            String response;
            boolean withID = StringUtils.isNumeric(arg);
            java.util.List<TextChannel> tcArray = mcb.getRelayChannels();
            String tcID = withID ? arg : FinderUtil.findTextChannel(arg, e.getGuild()).get(0).getId();
            if (!containsID(tcID)) {
                response = Locale.getCommandsMessage("textchannel.tc-not-bound").finish();
                respond(e, String.format(response, arg), ResponseLevel.LEVEL_2);
                return;
            }
            if (tcArray.size() == 1) {
                response = Locale.getCommandsMessage("textchannel.last-tc").finish();
                respond(e, String.format(response, arg), ResponseLevel.LEVEL_2);
                return;
            }
            tcArray.remove(mcb.getBot().getJda().getTextChannelById(tcID));
            configsManager.getChatConfig().set("Relay_Channels", tcArray);
            configsManager.getChatConfig().saveConfig();
            response = Locale.getCommandsMessage("textchannel.removed-tc").finish();
            respond(e, String.format(response, arg), ResponseLevel.LEVEL_1);
            Logger.info("Removed text channel " + arg);
        }

        private boolean containsID(String textChannelID) {
            java.util.List<TextChannel> textChannels = mcb.getRelayChannels();
            for (TextChannel tc : textChannels)
                if (tc.getName().equalsIgnoreCase(textChannelID))
                    return true;
            return false;
        }
    }
}
