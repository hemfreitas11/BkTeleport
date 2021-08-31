package me.bkrmt.bkteleport.commands.home;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.ConfigType;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkteleport.BkTeleport;
import me.bkrmt.bkteleport.teleportable.Home;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;


public class SetHomeCmd extends Executor {
    public SetHomeCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            int maxHomes = Utils.intFromPermission(player, 3, "bkteleport.maxhomes", new String[]{"bkteleport.maxhomes.*", "bkteleport.admin"});

            File homesFile = getPlugin().getFile("userdata", ((Player) sender).getUniqueId().toString() + ".yml");

            Configuration configFile;

            if (homesFile.exists()) {
                configFile = getPlugin().getConfigManager().getConfig("userdata", ((Player) sender).getUniqueId().toString() + ".yml");
            } else {
                configFile = new Configuration(getPlugin(), homesFile, ConfigType.PLAYER_DATA);
                configFile.saveToFile();
                getPlugin().getConfigManager().addConfig(configFile);
            }

            int homeSize = configFile == null || configFile.get("homes") == null ? 0 : configFile.getConfigurationSection("homes").getKeys(false).size();
            if (maxHomes == -1 || homeSize < maxHomes) {
                if (args.length == 0) {
                    String homeCmd = getPlugin().getLangFile().get((OfflinePlayer) sender, "commands.home.command");
                    setHomeValues(player, homeCmd);
                    getPlugin().sendTitle((Player) sender, 5, 40, 10,
                            getPlugin().getLangFile().get((OfflinePlayer) sender, "info.home-set").replace("{home-name}", homeCmd), "");
                } else {
                    String name = Utils.joinStringArray(args);
                    if (!name.isEmpty()) {
                        String homeName = name
                            .replace("\"", "")
                            .replace("'", "")
                            .replace("\\", "")
                            .replace("/", "")
                            .replace("{", "")
                            .replace("}", "")
                            .replace("[", "")
                            .replace(":", "")
                            .replace(" ", "_")
                            .replace("]", "").toLowerCase();
                        if (!homeName.isEmpty()) {
                            setHomeValues(player, homeName);
                            getPlugin().sendTitle(
                                    (Player) sender,
                                    5, 40, 10,
                                    getPlugin().getLangFile().get((OfflinePlayer) sender, "info.home-set").replace("{home-name}", homeName),
                                    ""
                            );
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.invalid-name"));
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.invalid-name"));
                    }
                }
            } else {
                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.home-limit").replace("{max-homes}", String.valueOf(maxHomes)));
            }
        }
        return true;
    }

    private void setHomeValues(Player player, String homeName) {
        Home home = BkTeleport.getInstance().getHomesManager().getHome(player.getUniqueId(), homeName);
        if (home != null) {
            home.setLocation(player.getLocation());
            home.saveValues();
        }
    }
}
