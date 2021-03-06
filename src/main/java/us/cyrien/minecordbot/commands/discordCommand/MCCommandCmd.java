package us.cyrien.minecordbot.commands.discordCommand;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.cyrien.minecordbot.Bot;
import us.cyrien.minecordbot.Minecordbot;
import us.cyrien.minecordbot.chat.entity.DiscordCommandSender;
import us.cyrien.minecordbot.chat.entity.DiscordConsoleCommandSender;
import us.cyrien.minecordbot.chat.entity.DiscordPlayerCommandSender;
import us.cyrien.minecordbot.commands.MCBCommand;
import us.cyrien.minecordbot.localization.Locale;
import us.cyrien.minecordbot.utils.FinderUtil;

public class MCCommandCmd extends MCBCommand {

    public MCCommandCmd(Minecordbot minecordbot) {
        super(minecordbot);
        this.name = "minecraftcommand";
        this.aliases = new String[]{"mcmd"};
        this.arguments = "<Command> [argument]...";
        this.help = Locale.getCommandsMessage("mcmd.description").finish();
        this.category = Bot.MISC;
    }

    @Override
    protected void doCommand(CommandEvent e) {
        if (e.getArgs().isEmpty()) {
            respond(e, getHelpCard(e, this));
            return;
        }
        String arg = e.getArgs();
        String[] args = arg.split("\\s");
        Player p = FinderUtil.findPlayerInDatabase(e.getMember().getUser().getId());
        CommandSender commandSender;
        boolean allowed = checkRoleBasedPerm(e.getMember())|| (PermissionUtil.checkPermission(e.getTextChannel(), e.getMember(), Permission.ADMINISTRATOR) ||
                e.getAuthor().getId().equals(e.getClient().getOwnerId()));
        if (p == null && allowed) {
            if (args[0].equalsIgnoreCase("help"))
                commandSender = new DiscordCommandSender(e);
            else
                commandSender = new DiscordConsoleCommandSender(e);
            Bukkit.getScheduler().runTaskLater(mcb,() -> Bukkit.getServer().dispatchCommand(commandSender, arg), 1L);
        } else if (p != null) {
            DiscordPlayerCommandSender dcs = new DiscordPlayerCommandSender(p, e);
            Bukkit.getScheduler().runTaskLater(mcb,() -> Bukkit.getServer().dispatchCommand(dcs, arg), 1L);
        } else {
            respond(e, noPermissionMessageEmbed());
        }
    }
}
