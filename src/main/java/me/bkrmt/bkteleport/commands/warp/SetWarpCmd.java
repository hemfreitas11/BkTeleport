package me.bkrmt.bkteleport.commands.warp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class SetWarpCmd extends Executor {


    public SetWarpCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
            if (args.length == 1) {
                if (new File(getPlugin().getDataFolder().getPath() + File.separator + "warps").listFiles().length < 54) {
                    setWarpValues(args[0], sender);
                    getPlugin().sendTitle((Player) sender, 5, 40, 10, getPlugin().getLangFile().get("info.warp-set.title").replace("{warp-name}", args[0]),
                            getPlugin().getLangFile().get("info.warp-set.subtitle"));
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.max-warps-reached"));
                }
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }

    private void setWarpValues(String warpName, CommandSender sender) {
        Configuration config = new Configuration(getPlugin(), getPlugin().getFile("warps", warpName.toLowerCase() + ".yml"));
        config.set("name", warpName);
        config.setLocation("", ((Player) sender).getLocation());
        config.saveToFile();
        getPlugin().getConfigManager().addConfig(config);
    }
}