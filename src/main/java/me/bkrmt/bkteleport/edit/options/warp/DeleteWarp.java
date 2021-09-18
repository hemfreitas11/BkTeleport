package me.bkrmt.bkteleport.edit.options.warp;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.menus.ConfirmationMenu;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.bkgui.page.PageItem;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.edit.EditOption;
import me.bkrmt.bkteleport.teleportable.Teleportable;
import me.bkrmt.bkteleport.teleportable.Warp;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class DeleteWarp extends EditOption {
    private Page confirmMenu = null;

    public DeleteWarp(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.delete", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return translateName(getPlugin().getLangFile().get("warps-menu.edit-menu.delete.name"));
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        return translateLore(getPlugin().getLangFile().getStringList("warps-menu.edit-menu.delete.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return translateItem(XMaterial.TNT.parseItem());
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        if (!hasPermission()) return event -> {};

        Teleportable teleportable = getTeleportable();
        return event -> {
            if (teleportable == null) {
                currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.option-error"), null);
            } else {
                Warp warp = (Warp) teleportable;
                Player player = (Player) event.getWhoClicked();
                PageItem confirmButton = new PageItem(
                    new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).setName(getPlugin().getLangFile().get(player, "info.confirm.accept")),
                    player.getName().toLowerCase() + "-" + warp.getConfigKey() + "-confirm-button",
                    false,
                    null,
                    confirmEvent -> {
                        warp.deleteWarp();
                        confirmEvent.getWhoClicked().closeInventory();
                        File[] warps = getPlugin().getFile("warps", "").listFiles();
                        if (warps != null && warps.length > 0) {
                            PluginUtils.openWarpsGui(player, null, true);
                        }
                    }
                );
                PageItem declineButton = new PageItem(
                    new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).setName(getPlugin().getLangFile().get(player, "info.confirm.decline")),
                    player.getName().toLowerCase() + "-" + warp.getConfigKey() + "-decline-button",
                    false,
                    null,
                    declineEvent -> {
                        confirmMenu.setWipeOnlySelf(true);
                        currentPage.openGui(player);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get(player, "info.confirm.cancelled"), null);
                    }
                );
                PageItem infoButton = new PageItem(
                    new ItemBuilder(XMaterial.WRITABLE_BOOK).setName(getPlugin().getLangFile().get(player, "info.confirm.delete")),
                    player.getName().toLowerCase() + "-" + warp.getConfigKey() + "-info-button",
                    false,
                    null,
                    infoEvent -> {
                    }
                );
                confirmMenu = new ConfirmationMenu(
                    getPlugin().getLangFile().get(player, "info.confirm.title"),
                    infoButton,
                    confirmButton,
                    declineButton
                ).getMenu();
                currentPage.setSwitchingPages(true);
                confirmMenu.addPreviousMenu(currentPage);
                confirmMenu.openGui(player);
            }
        };
    }
}
