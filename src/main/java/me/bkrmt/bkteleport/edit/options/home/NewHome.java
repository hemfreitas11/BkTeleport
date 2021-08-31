package me.bkrmt.bkteleport.edit.options.home;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.bkgui.page.PageUtils;
import me.bkrmt.bkcore.input.InputRunnable;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkteleport.BkTeleport;
import me.bkrmt.bkteleport.HomeType;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.edit.EditOption;
import me.bkrmt.bkteleport.teleportable.Home;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewHome extends EditOption {
    public NewHome(Object customObject, Player player) {
        super(customObject, player, new String[]{"bkteleport.home", "bkteleport.player"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return getPlugin().getLangFile().get("homes-menu.new-home.name");
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        List<String> lore = new ArrayList<>();
        int maxHomes = Utils.intFromPermission(getPlayer(), 3, "bkteleport.maxhomes", new String[]{"bkteleport.maxhomes.*", "bkteleport.admin"});
        int homesSize = 0;
        File homesFile = getPlugin().getFile("userdata", getPlayer().getUniqueId().toString() + ".yml");
        if (homesFile.exists()) {
            ConfigurationSection homes = YamlConfiguration.loadConfiguration(homesFile).getConfigurationSection("homes");
            if (homes != null) homesSize = homes.getKeys(false).size();
        }
        for (String line : getPlugin().getLangFile().getStringList("homes-menu.new-home.description")) {
            lore.add(
                line
                    .replace("{homes}", String.valueOf(homesSize))
                    .replace("{max-homes}", (maxHomes < 0 ? "99+" : String.valueOf(maxHomes)))
            );
        }
        return lore;
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return new ItemStack(Material.NETHER_STAR);
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        if (!hasPermission()) return event -> {
        };

        return event -> {
            Player player = (Player) event.getWhoClicked();

            int maxHomes = Utils.intFromPermission(player, 3, "bkteleport.maxhomes", new String[]{"bkteleport.maxhomes.*", "bkteleport.admin"});

            ConfigurationSection homes = YamlConfiguration.loadConfiguration(getPlugin().getFile("userdata", player.getUniqueId().toString() + ".yml")).getConfigurationSection("homes");

            if (!(maxHomes < 0) && homes != null && homes.getKeys(false).size() >= maxHomes) {
                currentPage.displayItemMessage(
                        event.getSlot(), 3, ChatColor.RED,
                        getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.home-limit")
                            .replace("{max-homes}", String.valueOf(maxHomes)), null
                );
            } else {
                String cancelInput = getPlugin().getConfigManager().getConfig().getString("cancel-input");
                InputRunnable cancelRunnable = input -> {
                    currentPage.openGui(player);
                    currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.input.canceled"), null);
                };
                currentPage.setSwitchingPages(true);
                new PlayerInput(getPlugin(), player, currentPage, input -> {
                    if (!input.equalsIgnoreCase(cancelInput)) {
                        String homeName = input
                                .replace("\"", "")
                                .replace("'", "")
                                .replace("\\", "")
                                .replace("/", "")
                                .replace("{", "")
                                .replace("}", "")
                                .replace("(", "")
                                .replace(")", "")
                                .replace("*", "")
                                .replace(":", "")
                                .replace("[", "")
                                .replace(" ", "_")
                                .replace("]", "").toLowerCase();
                        if (!homeName.isEmpty()) {
                            Home home = BkTeleport.getInstance().getHomesManager().getHome(player.getUniqueId(), homeName);
                            if (home != null) {
                                home.setLocation(player.getLocation());
                                home.saveValues();
                            }
                            PageUtils.wipeLinkedPages(currentPage);
                            PluginUtils.sendHomes(HomeType.Home, home.getConfig().getFile(), player, false);
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get(player, "error.invalid-name"));
                        }
                    }
                }, cancelRunnable)
                        .setTimeout(60, cancelRunnable)
                        .setCancellable(true)
                        .setTitle(getPlugin().getLangFile().get(player, "info.new-home"))
                        .setSubTitle(getPlugin().getLangFile().get(player, "info.input.cancel").replace("{cancel-input}", cancelInput))
                        .sendInput();
            }
        };
    }
}

