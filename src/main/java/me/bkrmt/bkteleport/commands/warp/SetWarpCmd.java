package me.bkrmt.bkteleport.commands.warp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class SetWarpCmd extends Executor {
    public SetWarpCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.admin")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            if (args.length == 0) {
                sendUsage(sender);
            } else {
                String name = Utils.joinStringArray(args);
                if (!name.isEmpty()) {
                    String warpName = name
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
                    if (!warpName.isEmpty()) {
                        if (warpName.equalsIgnoreCase("spawn")) {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.use-setspawn"));
                        } else if (warpName.equalsIgnoreCase(getPlugin().getLangFile().get((OfflinePlayer) sender, "commands.warp.subcommands.edit.command"))) {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.reserved-name"));
                        } else {
                            setWarpValues(warpName, sender);
                            getPlugin().sendTitle((Player) sender, 5, 40, 10,
                                    getPlugin().getLangFile().get((OfflinePlayer) sender, "info.warp-set.title").replace("{warp-name}", warpName),
                                    getPlugin().getLangFile().get((OfflinePlayer) sender, "info.warp-set.subtitle")
                            );
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.invalid-name"));
                    }
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.invalid-name"));
                }
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
        File essFolder = new File("plugins" + File.separatorChar + "Essentials");
        if (essFolder.exists()) {
            if (getPlugin().getConfigManager().getConfig().getBoolean("essentials.save-warps-to-essentials")) {
                File essFile = new File("plugins" + File.separatorChar + "Essentials" + File.separatorChar + "warps" + File.separatorChar + warpName.toLowerCase() + ".yml");
                if (!essFile.exists()) {
                    try {
                        if (essFile.createNewFile()) {
                            Configuration essConfig = new Configuration(getPlugin(), essFile);
                            essConfig.set("name", warpName);
                            essConfig.setLocation("", ((Player) sender).getLocation());
                            essConfig.set("lastowner", ((Player) sender).getUniqueId());
                            essConfig.saveToFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}