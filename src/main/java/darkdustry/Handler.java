package darkdustry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

@SuppressWarnings("unused")
public class Handler implements Listener {

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        Bot.message(String.format(":small_red_triangle: **%s** заскакивает на сервер!", event.getPlayer().getName()));
        Bot.updateStatus();
    }

    @EventHandler
    void onPlayerKick(PlayerKickEvent event) {
        Bot.message(String.format("**%s** был выгнан по причине: \"%s\"", event.getPlayer().getName(), event.getReason()));
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        Bot.message(String.format(":small_red_triangle_down: **%s** выходит с сервера", event.getPlayer().getName()));
        Bot.updateStatus();
    }

    @EventHandler
    void onMessage(AsyncPlayerChatEvent event) {
        Bot.message(String.format("**%s**: %s", event.getPlayer().getName(), event.getMessage()));
    }

    @EventHandler
    void onDeath(PlayerDeathEvent event) {
        Bot.message(String.format("**%s**", event.getDeathMessage()));
    }
}
