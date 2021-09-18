package me.bkrmt.bkteleport.edit.options;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.menus.ConfirmationMenu;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.bkgui.page.PageItem;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.edit.EditOption;
import me.bkrmt.bkteleport.teleportable.Teleportable;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Location extends EditOption {
    private Page confirmMenu = null;

    public Location(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.location", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return translateName(getPlugin().getLangFile().get("homes-menu.edit-menu.set-location.name"));
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        Teleportable teleportable = (Teleportable) getCustomObject();
        List<String> lore = new ArrayList<>();
        if (teleportable != null) {
            org.bukkit.Location location = teleportable.getLocation();
            if (location != null) {
                for (String line : getPlugin().getLangFile().getStringList("homes-menu.edit-menu.set-location.lore")) {
                    lore.add(
                            line
                                .replace("{world}", location.getWorld().getName())
                                .replace("{x}", String.valueOf(location.getX()))
                                .replace("{y}", String.valueOf(location.getY()))
                                .replace("{z}", String.valueOf(location.getZ()))
                    );
                }
            }
            return translateLore(lore);
        } else return translateLore(getPlugin().getLangFile().getStringList("homes-menu.edit-menu.set-location.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return translateItem(XMaterial.OAK_FENCE.parseItem());
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        if (!hasPermission()) return event -> {};

        Teleportable finalTeleportable = getTeleportable();
        return event -> {
            if (finalTeleportable == null) {
                currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.option-error"), null);
            } else {
                Player player = (Player) event.getWhoClicked();
                PageItem confirmButton = new PageItem(
                    new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).setName(getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.accept")),
                    event.getWhoClicked().getName().toLowerCase() + "-" + finalTeleportable.getConfigKey() + "-confirm-button",
                    false,
                    null,
                    confirmEvent -> {
                        confirmMenu.setWipeOnlySelf(true);
                        finalTeleportable.setLocation(player.getLocation());
                        finalTeleportable.saveValues();
                        currentPage.openGui(player);
                        list.updateItem(getID(), this);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.location-set"), null);
                    }
                );
                PageItem declineButton = new PageItem(
                    new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).setName(getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.decline")),
                    event.getWhoClicked().getName().toLowerCase() + "-" + finalTeleportable.getConfigKey() + "-decline-button",
                    false,
                    null,
                    declineEvent -> {
                        confirmMenu.setWipeOnlySelf(true);
                        currentPage.openGui(player);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.cancelled"), null);
                    }
                );
                PageItem infoButton = new PageItem(
                    new ItemBuilder(XMaterial.WRITABLE_BOOK).setName(getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.location")),
                    event.getWhoClicked().getName().toLowerCase() + "-" + finalTeleportable.getConfigKey() + "-info-button",
                    false,
                    null,
                    infoEvent -> {
                    }
                );
                confirmMenu = new ConfirmationMenu(
                    getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.title"),
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
