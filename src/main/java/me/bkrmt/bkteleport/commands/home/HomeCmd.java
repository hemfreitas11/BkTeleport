package me.bkrmt.bkteleport.commands.home;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.config.InvalidLocationException;
import me.bkrmt.bkteleport.BkTeleport;
import me.bkrmt.bkteleport.HomeType;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.teleportable.Home;
import me.bkrmt.teleport.Teleport;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class HomeCmd extends Executor {

    public HomeCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            if (args.length == 1 && args[0].contains(":")) {
                if (sender.hasPermission("bkteleport.spy.home") || sender.hasPermission("bkteleport.admin")) {
                    String[] spy = args[0].split(":");
                    if (spy.length == 0) {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.wrong-spy-format"));
                        return true;
                    }
                    Player spied = Utils.getPlayer(spy[0]);
                    String spiedFile = null;
                    if (spied == null) {
                        File[] homeFiles = new File(getPlugin().getDataFolder().getPath() + File.separator + "userdata").listFiles();
                        if (homeFiles != null) {
                            for (File homeFile : homeFiles) {
                                Configuration tempConfig = getPlugin().getConfigManager().getConfig("userdata", homeFile.getName());
                                if (tempConfig.getString("player") != null) {
                                    if (tempConfig.getString("player").equalsIgnoreCase(spy[0])) {
                                        spiedFile = homeFile.getName();
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
                                try {
                                    ((Player) sender).teleport(spyConfigFile.getLocation("homes." + spy[1]));
                                } catch (InvalidLocationException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-home-spy").replace("{player}", spy[0]).replace("{home-name}", spy[1]));
                            }
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-home-spy").replace("{player}", spy[0]).replace("{home-name}", spy[1]));
                        }
                    } else if (spy.length == 1) {
                        PluginUtils.sendHomes(HomeType.Home, getPlugin().getFile("userdata", spiedFile), sender, true);
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.wrong-spy-format"));
                    }
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
                }
            } else if (args.length == 1) {
                String bedCommand = getPlugin().getLangFile().get("commands.home.subcommands.bed.command");
                if (args[0].equalsIgnoreCase(bedCommand)) {
                    Location bedLocation = player.getBedSpawnLocation();
                    if (bedLocation != null) {
                        String title = getPlugin().getLangFile().get(player, "info.warped.home.title");
                        String subTitle = getPlugin().getLangFile().get(player, "info.warped.home.subtitle");

                        new Teleport(getPlugin(), player, getPlugin().getConfigManager().getConfig().getBoolean("teleport-countdown.cancel-on-move"))
                                .setLocation(bedCommand, bedLocation)
                                .setTitle(Utils.translateColor(title.replace("{home-name}", Utils.capitalize(bedCommand)).replace("{owner}", player.getName()).replace("{player}", player.getName())))
                                .setSubtitle(Utils.translateColor(subTitle).replace("{home-name}", Utils.capitalize(bedCommand)).replace("{owner}", player.getName()).replace("{player}", player.getName()))
                                .setDuration(Utils.intFromPermission(player, 5, "bkteleport.countdown", new String[]{"bkteleport.countdown.0", "bkteleport.admin"}))
                                .setIsCancellable(true)
                                .startTeleport();
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get(player, "error.invalid-bed"));
                    }
                } else {
                    if (!getPlugin().getFile("userdata", ((Player) sender).getUniqueId().toString() + ".yml").exists()) {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-home").replace("{home-name}", args[0]));
                    } else {
                        Home home = BkTeleport.getInstance().getHomesManager().getHome(player.getUniqueId(), args[0]);
                        if (home != null && home.getLocation() != null) {
                            home.teleportToHome(player);
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-home").replace("{home-name}", args[0]));
                        }
                    }
                }
            } else if (args.length == 0) {
                PluginUtils.sendHomes(HomeType.Home, getPlugin().getFile("userdata", ((Player) sender).getUniqueId().toString() + ".yml"), sender, false);
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }
}