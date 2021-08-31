package me.bkrmt.bkteleport;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.gui.Rows;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.request.ClickableRequest;
import me.bkrmt.bkteleport.edit.options.home.NewHome;
import me.bkrmt.bkteleport.teleportable.Home;
import me.bkrmt.bkteleport.teleportable.PagedOptions;
import me.bkrmt.bkteleport.teleportable.Warp;
import me.bkrmt.teleport.TeleportCore;
import me.bkrmt.teleport.TeleportType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

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

    public static void openWarpsGui(Player sender, List<Warp> warps, boolean editMode) {
        BkTeleport plugin = BkTeleport.getInstance();
        List<Warp> warpsList = null;
        boolean showInList = plugin.getConfigManager().getConfig().getBoolean("spawn.show-in-warps-list");
        if (warps == null) {
            File warpsFolder = plugin.getFile("warps", "");
            File[] warpFiles = warpsFolder.listFiles();
            if (warpFiles != null) {
                warpsList = new ArrayList<>();
                for (File warpFile : warpFiles) {
                    Configuration warpConfig = plugin.getConfigManager().getConfig("warps", warpFile.getName());
                    if (warpConfig != null) {
                        Warp warp = new Warp(warpConfig.getString("name"), warpConfig);
                        if (warp.getName().equals("spawn")) {
                            if (showInList && (sender.hasPermission("bkteleport.spawn") || sender.hasPermission("bkteleport.player"))) {
                                warpsList.add(warp);
                            }
                        } else {
                            if (sender.hasPermission("bkteleport.warp." + warp.getName()) || sender.hasPermission("bkteleport.warp.*") || sender.hasPermission("bkteleport.admin")) {
                                warpsList.add(warp);
                            }
                        }
                    }
                }
            }
        } else {
            warps.removeIf(warp -> (warp.getName().equalsIgnoreCase("spawn") && !showInList) || (warp.getName().equalsIgnoreCase("spawn") && !sender.hasPermission("bkteleport.spawn") && !sender.hasPermission("bkteleport.player")));
            warpsList = warps;
        }

        if (warpsList != null) {
            ArrayDeque<PagedItem> warpsDeque = new ArrayDeque<>(warpsList);

            if (editMode) {
                warpsDeque.forEach(pagedItem -> {
                    pagedItem.setIgnoreSlot(true);
                    pagedItem.setIgnorePage(true);
                });
            } else {
                warpsDeque.forEach(pagedItem -> {
                    pagedItem.setIgnoreSlot(false);
                    pagedItem.setIgnorePage(false);
                });
            }

            PagedList pagedWarps = new PagedList(BkTeleport.getInstance(), sender, "paged-warps-" + sender.getName().toLowerCase(), warpsDeque)
                    .setGuiRows(Rows.SIX)
                    .setListRows(4)
                    .setStartingSlot(11)
                    .setListRowSize(5)
//                .setButtonSlots(39, 41)
                    .setGuiTitle(plugin.getLangFile().get((OfflinePlayer) sender, "warps-menu.title").replace("{player}", sender.getName()))
                    .setCustomOptions(new PagedOptions())
                    .buildMenu();
            if (editMode) pagedWarps.setCustomOptions(new PagedOptions().setEditMode(true));

            pagedWarps.openPage(0);
        }
    }

    public static void sendToSpawn(Player player, boolean checkPermission) {
        BkPlugin plugin = BkTeleport.getInstance();
        File spawnFile = plugin.getFile("warps", "spawn.yml");
        if (!spawnFile.exists()) {
            player.sendMessage(plugin.getLangFile().get(player, "error.invalid-spawn"));
        } else {
            Configuration spawnConfig = new Configuration(plugin, spawnFile);
            Warp spawn = new Warp("spawn", spawnConfig);
            if (!checkPermission || player.hasPermission("bkteleport.spawn") || player.hasPermission("bkteleport.player")) {
                if (TeleportCore.INSTANCE.getPlayersInCooldown().get(player.getName()) == null) {
                    spawn.teleportToWarp(player);
                } else {
                    player.sendMessage(plugin.getLangFile().get(player, "error.already-waiting"));
                }
            } else {
                player.sendMessage(plugin.getLangFile().get(player, "error.no-permission"));
            }
        }
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

    public static void sendHomes(HomeType homeType, File homesFile, CommandSender commandSender, boolean editMode) {
        boolean isSpy = false;

        if (!(commandSender instanceof Player)) isSpy = true;
        else {
            Player sender = (Player) commandSender;
            if (homesFile.getName().replace(".yml", "").equalsIgnoreCase(sender.getUniqueId().toString())) isSpy = true;
        }

        String noHomeKey = isSpy ? "error.no-homes" : "error.no-home-spy";

        if (homesFile.exists()) {
            Configuration homesConfig = BkTeleport.getInstance().getConfigManager().getConfig("userdata", homesFile.getName());
            if (homesConfig.getConfigurationSection("homes") != null) {
                ConfigurationSection section = homesConfig.getConfigurationSection("homes");
                if (!isSpy) {
                    if (section.getKeys(false).size() > 0) {
                        openListMenu(true, homeType, commandSender, homesConfig, section, editMode);
                    } else {
                        commandSender.sendMessage(BkTeleport.getInstance().getLangFile().get(noHomeKey));
                    }
                } else {
                    openListMenu(isSpy, homeType, commandSender, homesConfig, section, editMode);
                }
            } else {
                commandSender.sendMessage(BkTeleport.getInstance().getLangFile().get(noHomeKey));
            }
        } else {
            commandSender.sendMessage(BkTeleport.getInstance().getLangFile().get(noHomeKey));
        }
    }

    public static void sendRequest(Player sender, Player target, String identifier) {
        BkPlugin plugin = BkTeleport.getInstance();
        String type = identifier.split("-")[0];
        List<String> lines = new ArrayList<>();
        int timeout = plugin.getConfigManager().getConfig().getInt("tp-expiration");

        for (String line : plugin.getLangFile().getStringList("teleport-requests." + type + ".message")) {
            lines.add(
                    line.replace("{player}", sender.getName())
                            .replace("{sender}", sender.getName())
                            .replace("{target}", target.getName())
                            .replace("{name}", target.getName())
                            .replace("{seconds}", String.valueOf(timeout))
            );
        }
        new ClickableRequest(plugin, identifier, sender, target)
                .setButtons(plugin.getLangFile().get(sender, "teleport-requests." + type + ".accept-button"), plugin.getLangFile().get(sender, "teleport-requests." + type + ".deny-button"))
                .setCommands(plugin.getLangFile().get(sender, "commands.tpaccept.command") + " " + sender.getName(), plugin.getLangFile().get(sender, "commands.tpdeny.command") + " " + sender.getName())
                .setHovers(plugin.getLangFile().get(sender, "teleport-requests." + type + ".accept-hover"), plugin.getLangFile().get(sender, "teleport-requests." + type + ".deny-hover"))
                .setLines(lines)
                .setTimeout(timeout, request -> {
                    request.getSender().sendMessage(plugin.getLangFile().get(sender, "error.invite-expired.others").replace("{player}", target.getName()));
                    request.getTarget().sendMessage(plugin.getLangFile().get(sender, "error.invite-expired.self").replace("{player}", sender.getName()));
                })
                .sendRequest();
        target.playSound(target.getLocation(), plugin.getHandler().getSoundManager().getPling(), 15, 1);
        sender.sendMessage(plugin.getLangFile().get(sender, "info.sent-invite").replace("{player}", target.getName()));
    }

    private static void openListMenu(boolean isSpy, HomeType homeType, CommandSender sender, Configuration config, ConfigurationSection section, boolean editMode) {
        Set<String> homeKeys = section.getKeys(false);
        int homeAmount = homeKeys.size();
        if (sender instanceof Player) {
            if (BkTeleport.getInstance().getConfigManager().getConfig().getBoolean("home-gui")) {
                openHomesGui((Player) sender, section, config, editMode);
            } else {
                sendChatWarpsList(isSpy, homeType, sender, config, homeKeys, homeAmount);
            }
        } else {
            sendChatWarpsList(isSpy, homeType, sender, config, homeKeys, homeAmount);
        }
    }

    private static void sendChatWarpsList(boolean isSpy, HomeType homeType, CommandSender commandSender, Configuration config, Set<String> homeKeys, int homeAmount) {
        Player player = (commandSender instanceof Player) ? (Player) commandSender : null;
        if (player == null) {
            BkTeleport.getInstance().sendConsoleMessage(ChatColor.RED + "You can't see warps in the console.");
        } else {
            TextComponent line = new TextComponent(Utils.translateColor(BkTeleport.getInstance().getLangFile().get(player, "info.home-list.start")));
            int sizeChecker = 0;
            String commandString = homeType.equals(HomeType.Home) ? BkTeleport.getInstance().getLangFile().get(player, "commands.home.command") : BkTeleport.getInstance().getLangFile().get(player, "commands.delhome.command");
            if (isSpy) {
                String playerName = config.getString("player");
                if (playerName == null) {
                    config.set("player", BkTeleport.getInstance().getServer().getOfflinePlayer(UUID.fromString(config.getFile().getName().replace(".yml", ""))).getName());
                    playerName = config.getString("player");
                }
                commandString += " " + playerName + ":";
            }
            for (String homeName : homeKeys) {
                line.addExtra(getTextComponent(commandString, homeName, isSpy, TeleportType.Home));
                sizeChecker++;
                if (sizeChecker != homeAmount) {
                    line.addExtra(Utils.translateColor(BkTeleport.getInstance().getLangFile().get(player, "info.home-list.separator")));
                } else {
                    line.addExtra(Utils.translateColor(BkTeleport.getInstance().getLangFile().get(player, "info.home-list.end")));
                }
            }
            player.spigot().sendMessage(line);
        }
    }

    private static void openHomesGui(Player sender, ConfigurationSection section, Configuration userdata, boolean editMode) {
        BkTeleport plugin = BkTeleport.getInstance();
        ArrayDeque<PagedItem> homes = new ArrayDeque<>();
        final boolean[] isOwner = {false};
        boolean isSpy = !sender.getUniqueId().toString().equalsIgnoreCase(userdata.getFile().getName().replace(".yml", ""));
        if (!isSpy) homes.add(new NewHome(null, sender));
        section.getKeys(false).forEach(key -> {
            Home home = new Home(key, userdata);
            if (!isOwner[0] && home.getOwnerName().equalsIgnoreCase(sender.getName())) isOwner[0] = true;
            homes.add(home);
        });

        PagedList homesList = new PagedList(BkTeleport.getInstance(), sender, "paged-homes-" + sender.getName().toLowerCase(), homes)
                .setGuiRows(isOwner[0] || isSpy ? Rows.FIVE : Rows.FOUR)
                .setListRows(2)
                .setStartingSlot(11)
                .setListRowSize(5)
//                .setButtonSlots(39, 41)
                .setGuiTitle(plugin.getLangFile().get(sender, "homes-menu.title").replace("{player}", sender.getName()))
                .setCustomOptions(new PagedOptions().setEditMode(editMode && isOwner[0]).setSpy(isSpy))
                .buildMenu();

        if (isOwner[0] || isSpy) setEditButton(sender, homesList);
        homesList.openPage(0);
    }

    private static void setEditButton(Player sender, PagedList homesList) {
        BkPlugin plugin = BkTeleport.getInstance();
        PagedOptions options = (PagedOptions) homesList.getCustomOptions();
        ItemBuilder enableEdit = new ItemBuilder(plugin.getHandler().getItemManager().getWritableBook())
                .setName(plugin.getLangFile().get(sender, "homes-menu.edit-menu.button.enable.name"))
                .setLore(plugin.getLangFile().getStringList("homes-menu.edit-menu.button.enable.lore"))
                .hideTags();
        ItemBuilder disableEdit = new ItemBuilder(plugin.getHandler().getItemManager().getWritableBook())
                .setName(plugin.getLangFile().get(sender, "homes-menu.edit-menu.button.disable.name"))
                .setLore(plugin.getLangFile().getStringList("homes-menu.edit-menu.button.disable.lore"))
                .hideTags();
        homesList.getPages().forEach(page -> {
            page.setItemOnXY(5, 5, options.isEditMode() ? disableEdit : enableEdit,
                    sender.getName().toLowerCase() + "-home-edit-button", null,
                    event -> {
                        if (options.isEditMode()) {
                            options.setEditMode(false);
                            page.displayItemMessage(event.getSlot(), 2, ChatColor.RED, plugin.getLangFile().get(sender, "info.edit-mode.disabled"), temp -> setEditButton((Player) event.getWhoClicked(), homesList));
                        } else {
                            options.setEditMode(true);
                            page.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, plugin.getLangFile().get(sender, "info.edit-mode.enabled"), temp -> setEditButton((Player) event.getWhoClicked(), homesList));
                        }
                    });
        });
    }

    public static TextComponent getTextComponent(String commandName, String buttonName, boolean isSpy, TeleportType tpType) {
        TextComponent buttonAccept;
        String hover;
        String keyword = tpType.equals(TeleportType.Home) ? "home" : "warp";
        buttonAccept = new TextComponent(Utils.translateColor(BkTeleport.getInstance().getLangFile().get(null, "info." + keyword + "-list." + keyword + "-format").replace("{" + keyword + "}", buttonName)));
        hover = Utils.translateColor(BkTeleport.getInstance().getLangFile().get(null, "info." + keyword + "-list.hover"));
        String space = isSpy ? "" : " ";
        buttonAccept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + commandName + space + buttonName));
        buttonAccept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        return buttonAccept;
    }

}