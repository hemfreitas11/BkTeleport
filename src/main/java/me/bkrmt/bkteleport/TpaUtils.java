package me.bkrmt.bkteleport;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Hashtable;

import static me.bkrmt.bkteleport.BkTeleport.plugin;

public class TpaUtils {
    public static Hashtable<String, BukkitTask> playerExpiredChecker = new Hashtable<>();

    public static void checkExpired(Player playerSender, Player playerTarget, RequestType type) {
        if (playerExpiredChecker.get(playerSender.getName().toLowerCase() + "-" + playerTarget.getName().toLowerCase() + "-" + type.toString()) != null)
            playerExpiredChecker.get(playerSender.getName().toLowerCase() + "-" + playerTarget.getName().toLowerCase() + "-" + type.toString()).cancel();
        BukkitTask checker = new BukkitRunnable() {
            @Override
            public void run() {
                if (playerExpiredChecker.containsKey(playerSender.getName().toLowerCase() + "-" + playerTarget.getName().toLowerCase() + "-" + type.toString())) {
                    playerTarget.sendMessage(plugin.getLangFile().get("error.invite-expired").replace("{player}", playerSender.getName()));
                    playerSender.sendMessage(plugin.getLangFile().get("error.invite-expired-self").replace("{player}", playerSender.getName()));
                    playerExpiredChecker.remove(playerSender.getName().toLowerCase() + "-" + playerTarget.getName().toLowerCase() + "-" + type.toString());
                }
                this.cancel();
            }
        }.runTaskLater(plugin, 20 * plugin.getConfig().getInt("tpa-expiration"));
        playerExpiredChecker.put(playerSender.getName().toLowerCase() + "-" + playerTarget.getName().toLowerCase() + "-" + type.toString(), checker);
    }

    public static RequestType isExpiring(String senderPlayer, String targetPlayer) {
        RequestType value = null;
        if (playerExpiredChecker.containsKey(senderPlayer + "-" + targetPlayer + "-" + RequestType.Tpa.toString())) {
            value = RequestType.Tpa;
        } else if (playerExpiredChecker.containsKey(senderPlayer + "-" + targetPlayer + "-" + RequestType.TpaHere.toString())) {
            value = RequestType.TpaHere;
        }
        return value;
    }
}
