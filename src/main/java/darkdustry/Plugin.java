package darkdustry;

import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Logger logger = PluginLogger.getLogger("Bot");
        try {
            Bot.init();
            getServer().getPluginManager().registerEvents(new Handler(), this);
            getServer().getPluginManager().registerEvents(new Achievements(), this);
            logger.info(format("Successfully started bot.", logger.getName(), "32;1"));

            Bot.message(":pager: **Сервер запущен!**");
        } catch (Exception e) {
            logger.severe(format("Unable to start bot.", logger.getName(), "31;1"));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        Bot.message(":pager: **Сервер был остановлен.**");
        Bot.shutdown();
    }

    public static String format(String text, String loggerName, String style) {
        return String.format("[\033[%sm%s\033[0m] %s", style, loggerName, text);
    }
}
