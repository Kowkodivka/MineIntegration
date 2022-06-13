package darkdustry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Properties;

public class Achievements implements Listener {

    // TODO, позже я сделаю локализированные на русский достижения

    Properties bundles = new Properties();

    @EventHandler
    void onAchievement(PlayerAdvancementDoneEvent event) {
        String achievementName = event.getAdvancement().getKey().getKey();

        if (achievementName.split("/")[0].contains("recipes")) {
            return;
        }

        Bot.message(String.format("**%s** получил достижение **%s**", event.getPlayer().getName(), achievementName));
    }
}
