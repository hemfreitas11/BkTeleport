package me.bkrmt.bkteleport.commands;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.ConfigType;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.teleport.Teleport;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class BackCmd extends Executor {
    public BackCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            File userDataFile = getPlugin().getFile("userdata", ((Player) sender).getUniqueId().toString() + ".yml");
            if (!userDataFile.exists()) {
                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.invalid-last-location"));
            } else {
                Configuration userDataConfig = new Configuration(getPlugin(), userDataFile, ConfigType.PLAYER_DATA);
                if (userDataConfig.get("lastlocation") != null) {
                    Location lastLocation = userDataConfig.getLocation("lastlocation");
                    if (lastLocation != null) {
                        new Teleport(getPlugin(), player, getPlugin().getConfigManager().getConfig().getBoolean("teleport-countdown.cancel-on-move"))
                                .setLocation("last-location", lastLocation)
                                .setTitle(getPlugin().getLangFile().get((OfflinePlayer) sender, "info.last-location.title"))
                                .setSubtitle(getPlugin().getLangFile().get((OfflinePlayer) sender, "info.last-location.subtitle"))
                                .setDuration(Utils.intFromPermission(player, 5, "bkteleport.countdown", new String[]{"bkteleport.countdown.0", "bkteleport.admin"}))
                                .setIsCancellable(true)
                                .startTeleport();
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.invalid-last-location"));
                    }
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.invalid-last-location"));
                }
            }
        }
        return true;
    }
}