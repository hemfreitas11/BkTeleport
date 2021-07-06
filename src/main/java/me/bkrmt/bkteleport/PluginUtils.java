package me.bkrmt.bkteleport;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.request.ClickableRequest;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Rows;
import me.bkrmt.teleport.Teleport;
import me.bkrmt.teleport.TeleportType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.UUID;

public class PluginUtils {

    public static String[] getHomes(Player player) {
        String[] returnValue = new String[]{""};

        if (BkTeleport.getInstance().getFile("userdata", player.getUniqueId().toString() + ".yml").exists()) {
            Configuration playerConfig = BkTeleport.getInstance().getConfigManager().getConfig("userdata", player.getUniqueId().toString() + ".yml");
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
        File warpsFolder = new File(BkTeleport.getInstance().getDataFolder().getPath() + File.separator + "warps");

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
            Configuration sendHomes = BkTeleport.getInstance().getConfigManager().getConfig("userdata", sendHomesFile.getName());
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
                                new Teleport(BkTeleport.getInstance(), sender, key, TeleportType.Home);
                            }
                            return;
                        }
                    }
                } else {
                    sender.sendMessage(BkTeleport.getInstance().getLangFile().get(noHomeKey));
                }
            } else {
                sender.sendMessage(BkTeleport.getInstance().getLangFile().get(noHomeKey));
            }
        } else {
            sender.sendMessage(BkTeleport.getInstance().getLangFile().get(noHomeKey));
        }
    }

    public static void sendRequest(Player sender, Player target, String identifier) {
        BkPlugin plugin = BkTeleport.getInstance();
        String type = identifier.split("-")[0];
        new ClickableRequest(plugin, identifier, sender, target)
                .setButtons(plugin.getLangFile().get("teleport-requests." + type + ".accept-button"), plugin.getLangFile().get("teleport-requests." + type + ".deny-button"))
                .setCommands(plugin.getLangFile().get("commands." + type + "ccept.command") + " " + sender.getName(), plugin.getLangFile().get("commands.tpdeny.command") + " " + sender.getName())
                .setHovers(plugin.getLangFile().get("teleport-requests." + type + ".accept-hover"), plugin.getLangFile().get("teleport-requests." + type + ".deny-hover"))
                .setLines(plugin.getLangFile().getStringList("teleport-requests." + type + ".message"))
                .setTimeout(plugin.getConfig().getInt("tp-expiration"), request -> {
                    request.getSender().sendMessage(plugin.getLangFile().get("error.invite-expired.self"));
                    request.getTarget().sendMessage(plugin.getLangFile().get("error.invite-expired.others"));
                })
                .sendRequest();
        target.playSound(target.getLocation(), plugin.getHandler().getSoundManager().getPling(), 15, 1);
        sender.sendMessage(plugin.getLangFile().get("info.sent-invite").replace("{player}", target.getName()));
    }

    private static void openListMenu(UserType userType, HomeType homeType, Player sender, Configuration config, ConfigurationSection section) {
        Set<String> homeKeys = section.getKeys(false);
        int homeAmount = homeKeys.size();

        BkPlugin plugin = BkTeleport.getInstance();

        if (plugin.getConfigManager().getConfig().getBoolean("home-gui")) {
            openHomesGui(sender, section, plugin, false);
        } else {
            TextComponent line = new TextComponent(Utils.translateColor(BkTeleport.getInstance().getLangFile().get("info.home-list.start")));
            int sizeChecker = 0;
            String commandString = homeType.equals(HomeType.Home) ? BkTeleport.getInstance().getLangFile().get("commands.home.command") : BkTeleport.getInstance().getLangFile().get("commands.delhome.command");
            if (userType.equals(UserType.Spy)) {
                String playerName = config.getString("player");
                if (playerName == null) {
                    config.set("player", BkTeleport.getInstance().getServer().getOfflinePlayer(UUID.fromString(config.getFile().getName().replace(".yml", ""))).getName());
                    playerName = config.getString("player");
                }
                commandString += " " + playerName + ":";
            }
            for (String homeName : homeKeys) {
                line.addExtra(getTextComponent(commandString, homeName, userType, TeleportType.Home));
                sizeChecker++;
                if (sizeChecker != homeAmount) {
                    line.addExtra(Utils.translateColor(BkTeleport.getInstance().getLangFile().get("info.home-list.separator")));
                } else {
                    line.addExtra(Utils.translateColor(BkTeleport.getInstance().getLangFile().get("info.home-list.end")));
                }
            }
            sender.spigot().sendMessage(line);
        }
    }

    private static void openHomesGui(Player sender, ConfigurationSection section, BkPlugin plugin, boolean editMode) {
        ArrayDeque<PagedItem> homes = new ArrayDeque<>();
        section.getKeys(false).forEach(key -> homes.add(new Home(
                key,
                new ItemStack(section.get("display-item") == null ? plugin.getHandler().getItemManager().getBed() : Material.valueOf(section.getString("display-item"))),
                section.get("display-name") == null ? null : section.getString("display-name"),
                section.get("description") == null ? null : section.getStringList("description")
        )));

        PagedList homesList = new PagedList(BkTeleport.getInstance(), sender, "paged-homes-" + sender.getName().toLowerCase(), homes)
                .setGuiRows(Rows.FIVE)
                .setListRows(2)
                .setButtonSlots(39, 41)
                .setMenuTitle(plugin.getLangFile().get("homes-menu.title").replace("{player}", sender.getName()))
                .buildMenu();
        homesList.getPages().forEach(page -> page.setItemOnXY(5, 5,
                new ItemBuilder(plugin.getHandler().getItemManager().getWritableBook()), "teste-teste", null,
                event -> {
//                        Page editMenu = new Page(plugin, plugin.getAnimatorManager(), )
                }));
        homesList.openPage(0);
    }

    public static TextComponent getTextComponent(String commandName, String buttonName, UserType userType, TeleportType tpType) {
        TextComponent buttonAccept;
        String hover;
        String keyword = tpType.equals(TeleportType.Home) ? "home" : "warp";
        buttonAccept = new TextComponent(Utils.translateColor(BkTeleport.getInstance().getLangFile().get("info." + keyword + "-list." + keyword + "-format").replace("{" + keyword + "}", buttonName)));
        hover = Utils.translateColor(BkTeleport.getInstance().getLangFile().get("info." + keyword + "-list.hover"));
        String space = userType.equals(UserType.Spy) ? "" : " ";
        buttonAccept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + commandName + space + buttonName));
        buttonAccept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        return buttonAccept;
    }

}