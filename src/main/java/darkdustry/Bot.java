package darkdustry;

import darkdustry.commands.OnlineCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import nullrefexc.gen.SlashCommandListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;

import static nullrefexc.gen.SlashCommandListener.registerSlashCommands;

public class Bot extends ListenerAdapter {

    private static final String token = "token"; // The old token has expired
    private static final long channelId = 983841073841442816L;
    private static final long guildId = 810758118442663936L;
    private static final long adminRoleId = 985118305725608006L;

    public static JDA jda;
    private static TextChannel channel;
    public static Role adminRole;
    public static final int timeoutCommand = 5;
    
    public static void init() {
        try {
            jda = JDABuilder.createDefault(token)
                    .setActivity(EntityBuilder.createActivity("Сервер запущен | IP: darkdustry.ml", null, Activity.ActivityType.WATCHING))
                    .addEventListeners(new Bot(), new SlashCommandListener(), new OnlineCommand())
                    .build()
                    .awaitReady();

            Guild guild = jda.getGuildById(guildId);
            guild.getSelfMember().modifyNickname("[/] " + jda.getSelfUser().getName()).queue();

            channel = guild.getTextChannelById(channelId);
            adminRole = guild.getRoleById(adminRoleId);

            registerSlashCommands(guild);

            AllowedMentions.setDefaultMentions(EnumSet.noneOf(Message.MentionType.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void notify(String title) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(title)
                .build();

        channel.sendMessageEmbeds(embed).queue();
    }

    public static void updateStatus() {
        jda.getPresence().setActivity(EntityBuilder.createActivity(Bukkit.getOnlinePlayers().size() + " игроков онлайн | IP: darkdustry.ml", null, Activity.ActivityType.WATCHING));
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
        String content = event.getMessage().getContentRaw();
        List<Emote> customEmoji = event.getMessage().getEmotes();
        for(Emote emoji : customEmoji) {
            content.replace((emoji.isAnimated() ? "<a:" : "<:") + emoji.getName() + ":" + emoji.getId() + ">", ":" + emoji.getName() + ":");
        } 

        if(member == null || member.isBot() || content.startsWith(".") || event.getChannel() != channel) {
            return;
        }

        if(content.length() >= 1) {
            Bukkit.broadcastMessage(String.format("%s[Discord] %s:%s%s", ChatColor.AQUA, memberName, ChatColor.RESET, content));
        }
    }
}
