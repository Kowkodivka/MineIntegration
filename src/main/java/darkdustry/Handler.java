package darkdustry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.Location;
import org.bukkit.World;

@SuppressWarnings("unused")
public class Handler implements Listener {

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        Bot.message(String.format(":small_red_triangle: **%s** зашёл на сервер!", event.getPlayer().getName()));
        Bot.updateStatus();
    }

    @EventHandler
    void onPlayerKick(PlayerKickEvent event) {
        Bot.message(String.format("**%s** выгнан(%s)", event.getPlayer().getName(), event.getReason()));
        Bot.updateStatus();
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        Bot.message(String.format(":small_red_triangle_down: **%s** вышел с сервера", event.getPlayer().getName()));
        Bot.updateStatus();
    }

    @EventHandler
    void onMessage(AsyncPlayerChatEvent event) {
        Bot.message(String.format("<**%s**> %s", event.getPlayer().getName(), event.getMessage()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String msg = "";

            Player player = (Player) event.getEntity();
            String name = player.getDisplayName();

            if (player.getLastDamageCause() != null) {
                EntityDamageEvent lastDamageEvent = player.getLastDamageCause();
                DamageCause cause = lastDamageEvent.getCause();

                if (lastDamageEvent instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent lastDamageByEntityEvent = (EntityDamageByEntityEvent) lastDamageEvent;
                    Entity damager = lastDamageByEntityEvent.getDamager();

                    if (damager instanceof Arrow) {
                        msg = name + " застрелен";
                    } else if (cause.equals(DamageCause.ENTITY_EXPLOSION)) {
                        msg = name + " подорвался";
                    } else if (damager instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity) damager;

                        msg = name + " убит " + getNameFromLivingEntity(livingEntity);
                    } else {
                        msg = name + " умер от неизвестной энтити";
                    }
                } else if (lastDamageEvent instanceof EntityDamageByBlockEvent) {
                    EntityDamageByBlockEvent lastDamageByBlockEvent = (EntityDamageByBlockEvent) lastDamageEvent;
                    Block damager = lastDamageByBlockEvent.getDamager();

                    if (cause.equals(DamageCause.CONTACT)) {
                        if (damager.getType() == Material.CACTUS) {
                            msg = name + " понял что кактус делает больно";
                        } else {
                            msg = name + " умер от контакта с блоком";
                        }
                    } else if (cause.equals(DamageCause.LAVA)) {
                        msg = name + " хотел поплавать в лаве";
                    } else if (cause.equals(DamageCause.VOID)) {
                        msg = name + " выпал из жизни";
                    } else {
                        msg = name + " умер от блока";
                    }
                } else {
                    if (cause.equals(DamageCause.FIRE)) {
                        msg = name + " сгорел";
                    } else if (cause.equals(DamageCause.FIRE_TICK)) {
                        msg = name + " сгорел до тла";
                    } else if (cause.equals(DamageCause.SUFFOCATION)) {
                        msg = name + " похоронен в блоках";
                    } else if (cause.equals(DamageCause.DROWNING)) {
                        msg = name + " не всплыл вовремя";
                    } else if (cause.equals(DamageCause.STARVATION)) {
                        msg = name + " забыл поесть";
                    } else if (cause.equals(DamageCause.FALL)) {
                        msg = name + " ударился об землю слишком сильно";
                    } else if (cause.equals(DamageCause.MAGIC)) {
                        msg = name + " магически умер";
                    } else {
                        msg = name + " умер от самого себя";
                    }
                }
            } else {
                msg = name + " умер при неизместных обстоятельствах";
            }

            if (!msg.isEmpty()) {
                Bot.message(msg);
            }
    }

    public static String getNameFromLivingEntity(LivingEntity livingEntity) {
        String name = "";

        if (livingEntity instanceof Player) {
            name = ((Player) livingEntity).getDisplayName();
        } else {
            name = livingEntity.toString().substring(5);
        }

        return name;
    }

    @EventHandler
    void onWorldChange(PlayerPortalEvent event){
        Bot.message(String.format(":globe_with_meredians: **%s** переносится в **%s**", event.getPlayer().getDisplayName(), event.getTo().getWorld().getName()));
    }
}
