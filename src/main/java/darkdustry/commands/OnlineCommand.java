package darkdustry.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nullrefexc.slashcommands.annotations.SlashCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

import static darkdustry.Bot.timeoutCommand;

public class OnlineCommand extends ListenerAdapter {

    @SlashCommand(name = "online", description = "Показывает список игроков на сервере.", timeout = timeoutCommand, unit = TimeUnit.SECONDS)
    public static void online(SlashCommandInteractionEvent event) {
        StringBuilder players = new StringBuilder();

        for (Player p : Bukkit.getOnlinePlayers()) {
            players.append("**").append(p.getDisplayName()).append("**").append("\n");
        }

        if (players.toString().trim().length() < 1) {
            event.reply("Сервер пуст.").setEphemeral(true).queue();
        } else {
            event.reply(String.format("Игроки: \n%s", players.toString().trim())).queue();
        }
    }
}
