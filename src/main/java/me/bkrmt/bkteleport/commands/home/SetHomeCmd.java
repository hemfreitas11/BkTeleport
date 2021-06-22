package me.bkrmt.bkteleport.commands.home;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SetHomeCmd extends Executor {
    public SetHomeCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
            int maxHomes = getMaxHomes(sender, "bkteleport.maxhomes");
            Configuration configFile = getPlugin().getConfigManager().getConfig("userdata", ((Player) sender).getUniqueId().toString() + ".yml");
            int homeSize = configFile.get("homes") == null ? 0 : configFile.getConfigurationSection("homes").getKeys(false).size();
            if (homeSize < maxHomes) {
                if (args.length == 0) {
                    String homeCmd = getPlugin().getLangFile().get("commands.home.command");
                    setHomeValues(homeCmd, configFile, sender);
                    getPlugin().sendTitle((Player) sender, 5, 40, 10, getPlugin().getLangFile().get("info.home-set").replace("{home-name}", homeCmd), "");
                } else if (args.length == 1) {
                    setHomeValues(args[0], configFile, sender);
                    getPlugin().sendTitle((Player) sender, 5, 40, 10, getPlugin().getLangFile().get("info.home-set").replace("{home-name}", args[0]), "");

                } else {
                    sendUsage(sender);
                }
            } else {
                sender.sendMessage(getPlugin().getLangFile().get("error.home-limit").replace("{max-homes}", String.valueOf(maxHomes)));
            }
        }
        return true;
    }

    private int getMaxHomes(CommandSender cmdSender, String permission) {
        int returnValue = 5;
        for (int c = 99; c > 0; c--) {
            if (cmdSender.hasPermission(permission + "." + c)) {
                returnValue = c;
                break;
            }
        }
        return returnValue;
    }

    private void setHomeValues(String homeName, Configuration configFile, CommandSender sender) {
        configFile.set("player", sender.getName());
        configFile.setLocation("homes." + homeName, ((Player) sender).getLocation());
        configFile.saveToFile();
    }
}
