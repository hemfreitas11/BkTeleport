package me.bkrmt.bkteleport.commands.spawn;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkteleport.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class SpawnCmd extends Executor {

    public SpawnCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            if (!(sender instanceof Player)) {
                if (args.length == 1) {
                    Player player = Bukkit.getPlayer(args[0]);
                    PluginUtils.sendToSpawn(player, false);
                } else {
                    getPlugin().getLogger().log(Level.INFO, ChatColor.RED + "Use: /spawn <player>");
                }
            } else if (args.length == 0) {
                PluginUtils.sendToSpawn((Player) sender, true);
            } else if (args.length == 1) {
                if (sender.hasPermission("bkteleport.spawn.others") || sender.hasPermission("bkteleport.admin")) {
                    Player player = Bukkit.getPlayer(args[0]);
                    PluginUtils.sendToSpawn(player, false);
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
                }
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }
}