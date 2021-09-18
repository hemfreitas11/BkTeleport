package me.bkrmt.bkteleport.edit.options.home;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.menus.ConfirmationMenu;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.bkgui.page.PageItem;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.HomeType;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.edit.EditOption;
import me.bkrmt.bkteleport.teleportable.Home;
import me.bkrmt.bkteleport.teleportable.Teleportable;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DeleteHome extends EditOption {
    private Page confirmMenu = null;

    public DeleteHome(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.delete", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return translateName(getPlugin().getLangFile().get("homes-menu.edit-menu.delete.name"));
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        return translateLore(getPlugin().getLangFile().getStringList("homes-menu.edit-menu.delete.lore"));
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
                Home home = (Home) teleportable;
                Player player = (Player) event.getWhoClicked();
                PageItem confirmButton = new PageItem(
                    new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).setName(getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.accept")),
                    event.getWhoClicked().getName().toLowerCase() + "-" + home.getConfigKey() + "-confirm-button",
                    false,
                    null,
                    confirmEvent -> {
                        home.deleteHome();
                        PluginUtils.sendHomes(HomeType.Home, home.getConfig().getFile(), player, true);
                    }
                );
                PageItem declineButton = new PageItem(
                    new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).setName(getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.decline")),
                    event.getWhoClicked().getName().toLowerCase() + "-" + home.getConfigKey() + "-decline-button",
                    false,
                    null,
                    declineEvent -> {
                        confirmMenu.setWipeOnlySelf(true);
                        currentPage.openGui(player);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.cancelled"), null);
                    }
                );
                PageItem infoButton = new PageItem(
                    new ItemBuilder(XMaterial.WRITABLE_BOOK).setName(getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.confirm.delete")),
                    event.getWhoClicked().getName().toLowerCase() + "-" + home.getConfigKey() + "-info-button",
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
