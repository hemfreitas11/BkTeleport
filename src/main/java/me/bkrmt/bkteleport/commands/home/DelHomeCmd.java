package me.bkrmt.bkteleport.commands.home;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkteleport.HomeType;
import me.bkrmt.bkteleport.PluginUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class DelHomeCmd extends Executor {

    public DelHomeCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            if (args.length == 1 && args[0].contains(":")) {
                String[] spy = args[0].split(":");
                if (spy.length == 2) {
                    if (sender.hasPermission("bkteleport.spy.delhome") || sender.hasPermission("bkteleport.admin")) {
                        Player spied = Utils.getPlayer(spy[0]);
                        String spiedFile = null;
                        if (spied == null) {
                            File[] homeFiles = new File(getPlugin().getDataFolder().getPath() + File.separator + "userdata").listFiles();
                            if (homeFiles != null) {
                                for (File homesFile : homeFiles) {
                                    FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(homesFile);
                                    if (tempConfig.getString("player") != null) {
                                        if (tempConfig.getString("player").equalsIgnoreCase(spy[0])) {
                                            spiedFile = homesFile.getName();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (spiedFile == null && spied != null) spiedFile = spied.getUniqueId().toString() + ".yml";
                        else if (spiedFile == null) {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.homes-not-found").replace("{player}", spy[0]));
                            return true;
                        }
                        if (spy.length == 2) {
                            if (getPlugin().getFile("userdata", spiedFile).exists()) {
                                Configuration spyConfigFile = getPlugin().getConfigManager().getConfig("userdata", spiedFile);
                                if (spyConfigFile.get("homes." + spy[1]) != null) {
                                    deleteHome(getPlugin(), sender, spyConfigFile, spy[1]);
                                } else {
                                    sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-home").replace("{home-name}", spy[1]));
                                }
                            } else {
                                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-home-spy").replace("{player}", spy[0]).replace("{home-name}", spy[1]));
                            }
                        } else if (spy.length == 1) {
                            PluginUtils.sendHomes(HomeType.DelHome, getPlugin().getFile("userdata", spiedFile), sender, false);
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.wrong-spy-format"));
                        }

                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
                    }
                } else {
                    delete(sender, args);
                }
            } else if (args.length == 1) {
                delete(sender, args);
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }

    private void delete(CommandSender sender, String[] args) {
        if (getPlugin().getFile("userdata", ((Player) sender).getUniqueId().toString() + ".yml").exists()) {
            Configuration configFile = getPlugin().getConfigManager().getConfig("userdata", ((Player) sender).getUniqueId().toString() + ".yml");
            if (configFile.get("homes." + args[0]) == null) {
                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-home").replace("{home-name}", args[0]));
            } else {
                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "info.home-deleted"));
                configFile.set("player", sender.getName());
                configFile.set("homes." + args[0], null);
                configFile.saveToFile();
            }
        } else {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-home").replace("{home-name}", args[0]));
        }
    }

    public static void deleteHome(BkPlugin plugin, CommandSender sender, Configuration spyConfigFile, String home) {
        sender.sendMessage(plugin.getLangFile().get((OfflinePlayer) sender, "info.home-deleted"));
        spyConfigFile.set("homes." + home, null);
        spyConfigFile.saveToFile();
    }
}
