package me.bkrmt.bkteleport.commands.warp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.teleportable.Warp;
import me.bkrmt.teleport.TeleportCore;
import me.bkrmt.teleport.TeleportType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class WarpCmd extends Executor {

    public WarpCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            if (!(sender instanceof Player)) {
                if (args.length == 2) {
                    sendOtherToWarp(sender, args);
                } else {
                    getPlugin().getLogger().log(Level.INFO, ChatColor.RED + "Use: /warp <warp-name> <player>");
                }
            } else if (args.length == 0) {
                sendWarps((Player) sender);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase(getPlugin().getLangFile().get((OfflinePlayer) sender, "commands.warp.subcommands.edit.command"))) {
                    if (sender.hasPermission("bkteleport.admin")) {
                        File[] warps = getPlugin().getFile("warps", "").listFiles();
                        if (warps != null && warps.length > 0) {
                            PluginUtils.openWarpsGui((Player) sender, null, true);
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.warps-empty"));
                        }
                    } else
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
                } else sendToWarp(sender, args, true);
            } else if (args.length == 2) {
                if (sender.hasPermission("bkteleport.warp.others") || sender.hasPermission("bkteleport.admin")) {
                    sendOtherToWarp(sender, args);
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
                }
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }

    private void sendOtherToWarp(CommandSender sender, String[] args) {
        Player target = Utils.getPlayer(args[1]);
        if (target != null) {
            sendToWarp(target, args, false);
        } else {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.player-not-found").replace("{player}", args[1]));
        }
    }

    private void sendToWarp(CommandSender sender, String[] args, boolean checkPermission) {
        File warpFile = getPlugin().getFile("warps", args[0].toLowerCase() + ".yml");
        if (!warpFile.exists()) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.unknown-warp").replace("{warp-name}", args[0]));
        } else {
            Configuration warpConfig = new Configuration(getPlugin(), warpFile);
            Warp warp = new Warp(warpConfig.getString("name"), warpConfig);
            if (!checkPermission || (sender.hasPermission("bkteleport.warp." + warp.getName()) || sender.hasPermission("bkteleport.warp.*") || sender.hasPermission("bkteleport.admin"))) {
                if (TeleportCore.INSTANCE.getPlayersInCooldown().get(sender.getName()) == null) {
                    warp.teleportToWarp((Player) sender);
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.already-waiting"));
                }
            } else {
                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
            }
        }
    }

    private void sendWarps(Player sender) {
        if (getPlugin().getConfigManager().getConfig().getBoolean("custom-warps-list-command.enabled")) {
            getPlugin().getServer().getPluginManager().callEvent(
                    new PlayerCommandPreprocessEvent(
                            sender, getPlugin().getConfigManager().getConfig().getString("custom-warps-list-command.command")
                    )
            );
        } else {
            File warpsFolder = getPlugin().getFile("warps", "");
            if (warpsFolder.exists()) {
                File[] warpFiles = warpsFolder.listFiles();
                if (warpFiles != null && warpFiles.length > 0) {
                    List<Warp> warpsList = new ArrayList<>();
                    for (File warpFile : warpFiles) {
                        Configuration warpConfig = getPlugin().getConfigManager().getConfig("warps", warpFile.getName());
                        if (warpConfig != null) {
                            Warp warp = new Warp(warpConfig.getString("name"), warpConfig);
                            if (sender.hasPermission("bkteleport.warp." + warp.getName()) || sender.hasPermission("bkteleport.warp.*") || sender.hasPermission("bkteleport.admin")) {
                                warpsList.add(warp);
                            }
                        }
                    }
                    if (warpsList.size() > 0) {
                        if (getPlugin().getConfigManager().getConfig().getBoolean("warp-gui")) {
                            PluginUtils.openWarpsGui(sender, warpsList, false);
                        } else {
                            TextComponent line = new TextComponent(Utils.translateColor(getPlugin().getLangFile().get(sender, "info.warp-list.start")));
                            String commandString = getPlugin().getLangFile().get(sender, "commands.warp.command");
                            List<String> allowedWarps = new ArrayList<>();
                            for (Warp warp : warpsList) {
                                if (sender.hasPermission("bkteleport.warp." + warp.getName()) || sender.hasPermission("bkteleport.warp.*") || sender.hasPermission("bkteleport.admin")) {
                                    allowedWarps.add(warp.getName());
                                }
                            }

                            Iterator<String> iterator = allowedWarps.iterator();
                            while (iterator.hasNext()) {
                                String warpName = iterator.next();
                                line.addExtra(PluginUtils.getTextComponent(commandString, warpName, false, TeleportType.Warp));
                                if (iterator.hasNext()) {
                                    line.addExtra(Utils.translateColor(getPlugin().getLangFile().get(sender, "info.warp-list.separator")));
                                }
                            }
                            line.addExtra(Utils.translateColor(getPlugin().getLangFile().get(sender, "info.warp-list.end")));

                            sender.spigot().sendMessage(line);
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get(sender, "error.warps-empty"));
                    }
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get(sender, "error.warps-empty"));
                }
            } else {
                sender.sendMessage(getPlugin().getLangFile().get(sender, "error.warps-empty"));
            }
        }
    }
}