package me.bkrmt.bkteleport.teleportable;

import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.edit.options.warp.ClickCommand;
import me.bkrmt.bkteleport.edit.options.warp.DeleteWarp;
import me.bkrmt.bkteleport.edit.options.warp.Slot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Warp extends Teleportable {
    private String clickCommand;

    public Warp(String warpName, Configuration config) {
        super("warps", "", XMaterial.RED_BED.parseMaterial(), warpName, config);
        if (getConfig().get("click-command") != null) clickCommand = getConfig().getString("click-command");
        if (getConfig().get("description") == null)
            setLore(getPlugin().getLangFile().getStringList("warps-menu.default-warp-lore"));
        else setLore(getConfig().getStringList("description"));
    }

    @Override
    public void saveValues() {
        super.saveValues();
        if (clickCommand != null) getConfig().set("click-command", clickCommand);
        getConfig().saveToFile();
    }

    public void deleteWarp() {
        Configuration config = getConfig();
        getPlugin().getConfigManager().removeConfig("warps", config.getFile().getName());
        if (config.getFile().delete()) {
            getPlugin().getConfigManager().removeConfig("warps", config.getFile().getName());
        }
    }

    public void teleportToWarp(Player player) {
        String title = getPlugin().getLangFile().get(player, "info.warped.warp.title");
        String subTitle = getPlugin().getLangFile().get(player, "info.warped.warp.subtitle");

        if (title == null) title = "§cError!";
        if (subTitle == null) subTitle = "§cError!";

        if (getClickCommand() != null && !getClickCommand().isEmpty()) {
            Location playerLocation = player.getLocation();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    clickCommand
                            .replace("{player}", player.getName())
                            .replace("{x}", String.valueOf(playerLocation.getX()))
                            .replace("{y}", String.valueOf(playerLocation.getY()))
                            .replace("{z}", String.valueOf(playerLocation.getZ()))
            );
        }

        super.teleport(
                player,
                Utils.translateColor(title.replace("{warp-name}", Utils.capitalize(getName().replace("_", " "))).replace("{player}", player.getName())),
                Utils.translateColor(subTitle).replace("{warp-name}", Utils.capitalize(getName().replace("_", " "))).replace("{player}", player.getName())
        );
    }

    public PagedList buildEditMenu(Player player, Page previousMenu) {
        List<PagedItem> extraOptions = new ArrayList<>();
        extraOptions.add(new ClickCommand(this, player));
        extraOptions.add(new me.bkrmt.bkteleport.edit.options.warp.Page(this, player));
        extraOptions.add(new Slot(this, player));
        extraOptions.add(new DeleteWarp(this, player));
        return super.buildEditMenu(
                player,
                getPlugin().getLangFile().get(null, "warps-menu.edit-menu.title").replace("{warp-name}", getName()),
                previousMenu,
                event -> {
                    PluginUtils.openWarpsGui(player, null, true);
                },
                extraOptions
        );
    }

    public String getClickCommand() {
        return clickCommand;
    }

    public void setClickCommand(String clickCommand) {
        this.clickCommand = clickCommand;
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return getDisplayName() == null ? getPlugin().getLangFile().get(null, "warps-menu.default-warp-name").replace("{warp-name}", getName().replace("_", " ")) : getDisplayName().replace("_", " ");
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        return event -> {
            if (((PagedOptions) list.getCustomOptions()).isEditMode()) {
                currentPage.setSwitchingPages(true);
                buildEditMenu((Player) event.getWhoClicked(), currentPage).openPage(0);
            } else {
                event.getWhoClicked().closeInventory();
                ((Player) event.getWhoClicked()).performCommand(getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "commands.warp.command") + " " + getName().toLowerCase());
            }
        };
    }
}
