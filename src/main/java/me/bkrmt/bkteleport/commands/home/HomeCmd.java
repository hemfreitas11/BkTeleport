package me.bkrmt.bkteleport.commands.home;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkteleport.HomeType;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.UserType;
import me.bkrmt.teleport.Teleport;
import me.bkrmt.teleport.TeleportCore;
import me.bkrmt.teleport.TeleportType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class HomeCmd extends Executor {

    public HomeCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
            if (args.length == 1 && args[0].contains(":")) {
                if (sender.hasPermission("bkteleport.spy.home")) {
                    String[] spy = args[0].split(":");
                    if (spy.length == 0) {
                        sender.sendMessage(getPlugin().getLangFile().get("error.wrong-spy-format"));
                        return true;
                    }
                    Player spied = Utils.getPlayer(spy[0]);
                    String spiedFile = null;
                    if (spied == null) {
                        for (File homesFile : new File(getPlugin().getDataFolder().getPath() + File.separator + "userdata").listFiles()) {
                            FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(homesFile);
                            if (tempConfig.getString("player") != null) {
                                if (tempConfig.getString("player").equalsIgnoreCase(spy[0])) {
                                    spiedFile = homesFile.getName();
                                    break;
                                }
                            }
                        }
                    }
                    if (spiedFile == null && spied != null) spiedFile = spied.getUniqueId().toString() + ".yml";
                    else if (spiedFile == null) {
                        sender.sendMessage(getPlugin().getLangFile().get("error.homes-not-found").replace("{player}", spy[0]));
                        return true;
                    }
                    if (spy.length == 2) {
                        if (getPlugin().getFile("userdata", spiedFile).exists()) {
                            Configuration spyConfigFile = getPlugin().getConfig("userdata", spiedFile);
                            if (spyConfigFile.get("homes." + spy[1]) != null) {
                                ((Player) sender).teleport(spyConfigFile.getLocation("homes." + spy[1]));
                            } else {
                                sender.sendMessage(getPlugin().getLangFile().get("error.unknown-home-spy").replace("{player}", spy[0]).replace("{home-name}", spy[1]));
                            }
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get("error.unknown-home-spy").replace("{player}", spy[0]).replace("{home-name}", spy[1]));
                        }
                    } else if (spy.length == 1) {
                        PluginUtils.sendHomes(UserType.Spy, HomeType.Home, getPlugin().getFile("userdata", spiedFile), sender);
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get("error.wrong-spy-format"));
                    }
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
                }
            } else if (args.length == 1) {
                if (!getPlugin().getFile("userdata", ((Player) sender).getUniqueId().toString() + ".yml").exists()) {
                    sender.sendMessage(getPlugin().getLangFile().get("error.unknown-home").replace("{home-name}", args[0]));
                } else {
                    Configuration configFile = getPlugin().getConfig("userdata", ((Player) sender).getUniqueId().toString() + ".yml");
                    if (configFile.get("homes." + args[0]) != null) {
                        if (TeleportCore.INSTANCE.getPlayersInCooldown().get(sender.getName()) == null || !TeleportCore.INSTANCE.getPlayersInCooldown().get(sender.getName())) {
                            new Teleport(getPlugin(), sender, args[0], TeleportType.Home);
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get("error.already-waiting"));
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get("error.unknown-home").replace("{home-name}", args[0]));
                    }
                }
            } else if (args.length == 0) {
                PluginUtils.sendHomes(UserType.User, HomeType.Home, getPlugin().getFile("userdata", ((Player) sender).getUniqueId().toString() + ".yml"), sender);
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }
}