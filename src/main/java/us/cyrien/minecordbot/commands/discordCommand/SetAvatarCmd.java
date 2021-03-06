package us.cyrien.minecordbot.commands.discordCommand;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.entities.Icon;
import us.cyrien.minecordbot.Bot;
import us.cyrien.minecordbot.Minecordbot;
import us.cyrien.minecordbot.commands.MCBCommand;
import us.cyrien.minecordbot.localization.Locale;
import us.cyrien.minecordbot.utils.OtherUtil;

import java.io.IOException;
import java.io.InputStream;

public class SetAvatarCmd extends MCBCommand {

    public SetAvatarCmd(Minecordbot minecordbot) {
        super(minecordbot);
        this.name = "setavatar";
        this.aliases = new String[]{"sa"};
        this.arguments = "<url>";
        this.help = Locale.getCommandsMessage("setavatar.description").finish();
        this.category = Bot.OWNER;
        this.ownerCommand = true;
        this.type = Type.EMBED;
    }

    @Override
    protected void doCommand(CommandEvent event) {
        String url;
        if (event.getArgs().isEmpty())
            if (!event.getMessage().getAttachments().isEmpty() && event.getMessage().getAttachments().get(0).isImage())
                url = event.getMessage().getAttachments().get(0).getUrl();
            else
                url = null;
        else
            url = event.getArgs();
        InputStream s = OtherUtil.imageFromUrl(url);
        if (s == null) {
            respond(event, event.getClient().getError() +
                " " + Locale.getCommandsMessage("setavatar.invalid").finish());
        } else {
            try {
                event.getSelfUser().getManager().setAvatar(Icon.from(s)).queue(
                        v -> respond(event, event.getClient().getSuccess() + " " +
                                Locale.getCommandsMessage("setavatar.changed").finish()),
                        t -> respond(event,event.getClient().getError() + " " +
                                Locale.getCommandsMessage("setavatar.failed").finish()));
            } catch (IOException e) {
                respond(event, event.getClient().getError() + " " +
                        Locale.getCommandsMessage("setavatar.couldNotLoad").finish());
            }
        }
    }
}
