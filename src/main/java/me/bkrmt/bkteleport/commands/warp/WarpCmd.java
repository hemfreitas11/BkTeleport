package me.bkrmt.bkteleport.commands.warp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.UserType;
import me.bkrmt.teleport.Teleport;
import me.bkrmt.teleport.TeleportCore;
import me.bkrmt.teleport.TeleportType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import static me.bkrmt.bkteleport.BkTeleport.plugin;

public class WarpCmd extends Executor {

    public WarpCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
            if (!(sender instanceof Player)) {
                if (args.length == 2) {
                    sendOtherToWarp(sender, args);
                } else {
                    getPlugin().getLogger().log(Level.INFO, ChatColor.RED + "Use: /warp <warp-name> <player>");
                }
            }else if (args.length == 0) {
                sendWarps((Player) sender);
            } else if (args.length == 1) {
                sendToWarp(sender, args, true);
            } else if (args.length == 2) {
                if (sender.hasPermission("bkteleport.warp.others")) {
                    sendOtherToWarp(sender, args);
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
                }
            }
            else {
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
            sender.sendMessage(getPlugin().getLangFile().get("error.player-not-found").replace("{player}", args[1]));
        }
    }

    private void sendToWarp(CommandSender sender, String[] args, boolean checkPermission) {
        if (!getPlugin().getFile("warps", args[0].toLowerCase() + ".yml").exists()) {
            sender.sendMessage(getPlugin().getLangFile().get("error.unknown-warp").replace("{warp-name}", args[0]));
        } else {
            if (!checkPermission || (sender.hasPermission("bkteleport.warp." + args[0].toLowerCase()) || sender.hasPermission("bkteleport.warp.*"))) {
                if (TeleportCore.playersInCooldown.get(sender.getName()) == null) {
                    new Teleport(getPlugin(), sender, args[0], TeleportType.Warp);
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.already-waiting"));
                }
            } else {
                sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
            }
        }
    }

    private void sendWarps(Player sender) {
        if (plugin.getConfig().getBoolean("custom-warps-list-command.enabled")) {
            plugin.getServer().getPluginManager().callEvent(
                    new PlayerCommandPreprocessEvent(
                            sender, plugin.getConfig().getString("custom-warps-list-command.command")
                    )
            );
        } else {
            File warpsFolder = new File(getPlugin().getDataFolder().getPath() + File.separator + "warps");

            if (warpsFolder.listFiles().length > 0) {
                File[] warps = warpsFolder.listFiles();
                String[] keys;

                List<String> tempList = new ArrayList<>();
                for (File warp : warps) {
                    String temp = YamlConfiguration.loadConfiguration(warp).getString("name");
                    if (sender.hasPermission("bkteleport.warp." + temp)) {
                        tempList.add(temp);
                    }
                }
                keys = tempList.toArray(new String[0]);

                int warpAmount = keys.length;
                if (warpAmount > 0) {
                    if (getPlugin().getConfig().getBoolean("warp-gui")) {
                        Inventory warpsMenu = getPlugin().getServer().createInventory
                                (
                                        null,
                                        (int) Math.ceil((double) warpAmount / 9) * 9,
                                        getPlugin().getLangFile().get("info.warp-list-title")
                                );

                        for (int c = 0; c < warpAmount; c++) {
                            warpsMenu.setItem(c, Utils.createItem(getPlugin(), ChatColor.translateAlternateColorCodes('&', "&7&l&o" + keys[c]),
                                    getPlugin().getHandler().getItemManager().getSign(),
                                    ChatColor.translateAlternateColorCodes('&', "&7&o/warp " + keys[c])));
                        }
                        sender.openInventory(warpsMenu);
                    } else {
                        TextComponent line = new TextComponent(Utils.translateColor(plugin.getLangFile().get("info.warp-list.start")));
                        String commandString = plugin.getLangFile().get("commands.warp.command");
                        List<String> allowedWarps = new ArrayList<>();
                        for (String warpName : keys) {
                            if (sender.hasPermission("bkteleport.warp." + warpName)) {
                                allowedWarps.add(warpName);
                            }
                        }

                        Iterator<String> it = allowedWarps.iterator();
                        while (it.hasNext()) {
                            String warpName = it.next();
                            line.addExtra(PluginUtils.getTextComponent(commandString, warpName, UserType.User, TeleportType.Warp));
                            if (it.hasNext()) {
                                line.addExtra(Utils.translateColor(plugin.getLangFile().get("info.home-list.separator")));
                            }
                        }
                        line.addExtra(Utils.translateColor(plugin.getLangFile().get("info.home-list.end")));

                        sender.spigot().sendMessage(line);
                    }
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.no-warps"));
                }
            } else {
                sender.sendMessage(getPlugin().getLangFile().get("error.no-warps"));
            }
        }
    }
}