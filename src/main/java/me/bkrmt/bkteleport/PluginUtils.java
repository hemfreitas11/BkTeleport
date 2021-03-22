package me.bkrmt.bkteleport;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.teleport.Teleport;
import me.bkrmt.teleport.TeleportType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.bkrmt.bkteleport.BkTeleport.plugin;

public class PluginUtils {

    public static List<String> getList(String name) {
        String[] temp = Utils.objectToString(plugin.getConfig().getStringList(name).toArray());
        List<String> returnValue = new ArrayList<>();
        for (String temp2 : temp) {
            returnValue.add(temp2.replace("-", ":"));
        }
        return returnValue;
    }

    public static String[] getHomes(Player player) {
        String[] returnValue = new String[]{""};

        if (plugin.getFile("userdata", player.getUniqueId().toString() + ".yml").exists()) {
            Configuration playerConfig = plugin.getConfig("userdata", player.getUniqueId().toString() + ".yml");
            if (playerConfig.getConfigurationSection("homes") != null) {
                ConfigurationSection section = playerConfig.getConfigurationSection("homes");
                if (section.getKeys(false).size() > 0) {
                    returnValue = Utils.objectToString(section.getKeys(false).toArray());
                }
            }
        }
        return returnValue;
    }

    public static String[] getWarps() {
        String[] returnValue = new String[]{""};
        File warpsFolder = new File(plugin.getDataFolder().getPath() + File.separator + "warps");

        if (warpsFolder.listFiles().length > 0) {
            File[] warps = warpsFolder.listFiles();
            if (warps.length > 0) {
                returnValue = new String[warps.length];
                for (int c = 0; c < warps.length; c++) {
                    returnValue[c] = warps[c].getName().replace(".yml", "");
                }
            }
        }
        return returnValue;
    }

    public static void sendHomes(UserType type, HomeType homeType, File sendHomesFile, CommandSender sender) {
        String noHomeKey = type.equals(UserType.User) ? "error.no-homes" : "error.no-home-spy";

        if (sendHomesFile.exists()) {
            Configuration sendHomes = plugin.getConfig("userdata", sendHomesFile.getName());
            if (sendHomes.getConfigurationSection("homes") != null) {
                ConfigurationSection section = sendHomes.getConfigurationSection("homes");
                if (section.getKeys(false).size() > 0 && section.getKeys(false).size() != 1) {
                    openListMenu(type, homeType, (Player) sender, sendHomes, section);
                } else if (section.getKeys(false).size() == 1) {
                    if (homeType.equals(HomeType.DelHome)) {
                        openListMenu(type, homeType, (Player) sender, sendHomes, section);
                    } else {
                        String[] keys = Utils.objectToString(section.getKeys(false).toArray());
                        for (String key : keys) {
                            if (type.equals(UserType.Spy)) {
                                ((Player) sender).teleport(sendHomes.getLocation("homes." + key));
                            } else {
                                new Teleport(plugin, sender, key, TeleportType.Home);
                            }
                            return;
                        }
                    }
                } else {
                    sender.sendMessage(plugin.getLangFile().get(noHomeKey));
                }
            } else {
                sender.sendMessage(plugin.getLangFile().get(noHomeKey));
            }
        } else {
            sender.sendMessage(plugin.getLangFile().get(noHomeKey));
        }
    }

    private static void openListMenu(UserType userType, HomeType homeType, Player sender, Configuration config, ConfigurationSection section) {
        String[] keys = Utils.objectToString(section.getKeys(false).toArray());
        int homeAmount = keys.length;

        if (plugin.getConfig().getBoolean("home-gui")) {
            Inventory homesMenu = plugin.getServer().createInventory
                    (
                            null,
                            (int) Math.ceil((double) homeAmount / 9) * 9,
                            plugin.getLangFile().get("info.home-list-title")
                    );

            for (int c = 0; c < homeAmount; c++) {
                ArrayList<String> lore = new ArrayList<>();

                if (userType.equals(UserType.User)) {
                    lore.add("/" + plugin.getLangFile().get("commands.home.command") + " " + keys[c]);
                } else {
                    String playerName = config.getString("player");
                    if (playerName == null) {
                        config.set("player", plugin.getServer().getOfflinePlayer(UUID.fromString(config.getFile().getName().replace(".yml", ""))).getName());
                        playerName = config.getString("player");
                    }
                    String commandString = homeType.equals(HomeType.Home) ? "commands.home.command" : "commands.delhome.command";
                    lore.add(Utils.translateColor("&7&o/" + plugin.getLangFile().get(commandString) + " " + playerName + ":" + keys[c]));
                    lore.add(Utils.translateColor(plugin.getLangFile().get("info.spying").replace("{player}", playerName)));
                }

                homesMenu.setItem(c, Utils.createItem(plugin, ChatColor.translateAlternateColorCodes('&', "&7&o" + keys[c]),
                        plugin.getHandler().getItemManager().getBed(), lore));
            }

            sender.openInventory(homesMenu);
        } else {
            TextComponent line = new TextComponent(Utils.translateColor(plugin.getLangFile().get("info.home-list.start")));
            int sizeChecker = 0;
            String commandString = homeType.equals(HomeType.Home) ? plugin.getLangFile().get("commands.home.command") : plugin.getLangFile().get("commands.delhome.command");
            if (userType.equals(UserType.Spy)) {
                String playerName = config.getString("player");
                if (playerName == null) {
                    config.set("player", plugin.getServer().getOfflinePlayer(UUID.fromString(config.getFile().getName().replace(".yml", ""))).getName());
                    playerName = config.getString("player");
                }
                commandString += " " + playerName + ":";
            }
            for (String homeName : keys) {
                line.addExtra(getTextComponent(commandString, homeName, userType, TeleportType.Home));
                sizeChecker++;
                if (sizeChecker != homeAmount) {
                    line.addExtra(Utils.translateColor(plugin.getLangFile().get("info.home-list.separator")));
                } else {
                    line.addExtra(Utils.translateColor(plugin.getLangFile().get("info.home-list.end")));
                }
            }
            sender.spigot().sendMessage(line);

        }
    }

    public static TextComponent getTextComponent(String commandName, String buttonName, UserType userType, TeleportType tpType) {
        TextComponent buttonAccept;
        String hover;
        String keyword = tpType.equals(TeleportType.Home) ? "home" : "warp";
        buttonAccept = new TextComponent(Utils.translateColor(plugin.getLangFile().get("info." + keyword + "-list." + keyword + "-format").replace("{" + keyword + "}", buttonName)));
        hover = Utils.translateColor(plugin.getLangFile().get("info." + keyword + "-list.hover"));
        String space = userType.equals(UserType.Spy) ? "" : " ";
        buttonAccept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + commandName + space + buttonName));
        buttonAccept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        return buttonAccept;
    }

}