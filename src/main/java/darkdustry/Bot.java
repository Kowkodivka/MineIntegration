package darkdustry;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class Bot extends ListenerAdapter {

    private static final String token = "OTc1MDg4NzEwMDEyMDAyMzk3.GtdA4H.aE7PtsdRZsAhlrVXEMT5APGb2DFYh6xqmkUa_s";
    private static final long channelId = 983841073841442816L;
    private static final long guildId = 810758118442663936L;

    public static JDA jda;
    private static Guild guild;
    private static TextChannel channel;
    
    public static void init() {
        try {
            jda = JDABuilder.createDefault(token)
                    .setActivity(EntityBuilder.createActivity("Сервер запущен | IP: darkdustry.ml", null, Activity.ActivityType.WATCHING))
                    .addEventListeners(new Bot())
                    .build()
                    .awaitReady();

            guild = jda.getGuildById(guildId);
            guild.getSelfMember().modifyNickname("[/] " + jda.getSelfUser().getName()).queue();

            channel = guild.getTextChannelById(channelId);

            AllowedMentions.setDefaultMentions(EnumSet.noneOf(Message.MentionType.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateStatus() {
        jda.getPresence().setActivity(EntityBuilder.createActivity(Bukkit.getOnlinePlayers().size() + " игроков онлайн | IP: darkdustry.ml", null, Activity.ActivityType.WATCHING));
    }

    public static void notify(String title, int color) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .build();

        channel.sendMessageEmbeds(embed).queue();
    }

    public static void message(String text) {
        Message msg = new MessageBuilder()
                .append(text)
                .build();

        channel.sendMessage(msg).queue();
    }

    public static void shutdown() {
        jda.shutdown();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User member = event.getMessage().getAuthor();
        String memberName = String.format("%s#%s", member.getName(), member.getDiscriminator());
        String message = event.getMessage().getContentRaw();

        if(member == null || member.isBot() || message.startsWith(".") || event.getChannel() != channel) {
            return;
        }

        if(message.length() >= 1) {
            Bukkit.broadcastMessage(String.format("%s[Discord] %s:%s%s", ChatColor.AQUA, memberName, ChatColor.RESET, message));
        }
    }
}