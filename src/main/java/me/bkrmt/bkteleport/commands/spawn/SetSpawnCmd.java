package me.bkrmt.bkteleport.commands.spawn;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCmd extends Executor {
    public SetSpawnCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.admin")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            setWarpValues(sender);
            getPlugin().sendTitle((Player) sender, 5, 40, 10,
                    getPlugin().getLangFile().get((OfflinePlayer) sender, "info.spawn-set"),
                    ""
            );
        }
        return true;
    }

    private void setWarpValues(CommandSender sender) {
        Configuration config = new Configuration(getPlugin(), getPlugin().getFile("warps", "spawn.yml"));
        config.set("name", "spawn");
        config.setLocation("", ((Player) sender).getLocation());
        config.saveToFile();
        getPlugin().getConfigManager().addConfig(config);
    }
}