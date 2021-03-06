package us.cyrien.minecordbot.chat.listeners.mcListeners;

import de.myzelyam.api.vanish.VanishAPI;
import net.dv8tion.jda.core.EmbedBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import us.cyrien.minecordbot.HookContainer;
import us.cyrien.minecordbot.Minecordbot;
import us.cyrien.minecordbot.hooks.SuperVanishHook;

import java.awt.*;

public class UserQuitJoinListener extends MCBListener {

    private static final Color JOIN_COLOR = new Color(92, 184, 92);
    private static final Color LEAVE_COLOR = new Color(243, 119, 54);

    public UserQuitJoinListener(Minecordbot mcb) {
        super(mcb);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent e) {
        System.out.println("Quit");
        mcb.getBot().getUpdatables().get("list").update();
        SuperVanishHook svHook = HookContainer.getSuperVanishHook();
        String msg = ChatColor.stripColor(e.getQuitMessage());
        boolean isLeaveBroadcast = configsManager.getBroadcastConfig().getBoolean("See_Player_Quit");
        boolean seeQuit = configsManager.getModChannelConfig().getBoolean("See_Player_Quit");
        System.out.println("seeQuit = " + seeQuit);
        if (seeQuit) {
            String m = msg;
            if (svHook != null) {
                boolean seeSV = configsManager.getModChannelConfig().getBoolean("See_SV");
                if (VanishAPI.isInvisible(e.getPlayer()) || e.getQuitMessage().equals("Fake") && seeSV)
                    m = "(Vanish) " + m;
            }
            messenger.sendMessageEmbedToAllModChannel(new EmbedBuilder().setColor(LEAVE_COLOR)
                    .setTitle(m, null).build());
            System.out.println("Message sent to discord");
        }
        System.out.println("isLeaveBroadcast = " + isLeaveBroadcast);
        if (isLeaveBroadcast) {
            if (e.getQuitMessage().equals("Fake")) {
                messenger.sendMessageEmbedToAllBoundChannel(new EmbedBuilder().setColor(LEAVE_COLOR)
                        .setTitle(msg, null).build());
                e.setQuitMessage("");
            } else if (check(e)) {
                messenger.sendMessageEmbedToAllBoundChannel(new EmbedBuilder().setColor(LEAVE_COLOR)
                        .setTitle(msg, null).build());
                System.out.println("Message sent to discord");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent e) {
        System.out.println("Join");
        mcb.getBot().getUpdatables().get("list").update();
        SuperVanishHook svHook = HookContainer.getSuperVanishHook();
        String msg = ChatColor.stripColor(e.getJoinMessage());
        boolean isJoinBroadCast = configsManager.getBroadcastConfig().getBoolean("See_Player_Join");
        boolean seeJoin = configsManager.getModChannelConfig().getBoolean("See_Player_Join");
        if (seeJoin) {
            String m = msg;
            if (svHook != null) {
                boolean seeSV = configsManager.getModChannelConfig().getBoolean("See_SV");
                if (VanishAPI.isInvisible(e.getPlayer()) || e.getJoinMessage().equals("Fake") && seeSV)
                    m = "(Vanish) " + m;
            }
            messenger.sendMessageEmbedToAllModChannel(new EmbedBuilder().setColor(JOIN_COLOR)
                    .setTitle(m, null).build());
        }
        if (isJoinBroadCast) {
            if (e.getJoinMessage().equals("Fake")) {
                messenger.sendMessageEmbedToAllBoundChannel(new EmbedBuilder().setColor(JOIN_COLOR)
                        .setTitle(msg, null).build());
                e.setJoinMessage("");
            } else if (check(e)) {
                messenger.sendMessageEmbedToAllBoundChannel(new EmbedBuilder().setColor(JOIN_COLOR)
                        .setTitle(msg, null).build());
            }
        }
    }

    private boolean check(PlayerEvent e) {
        boolean allowIncog = configsManager.getBroadcastConfig().getBoolean("Hide_Incognito_Player");
        if (isVanished(e.getPlayer())) {
            return false;
        } else if (allowIncog) {
            if (e.getPlayer().hasPermission("minecordbot.incognito")) {
                return false;
            }
        }
        return true;
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

}
