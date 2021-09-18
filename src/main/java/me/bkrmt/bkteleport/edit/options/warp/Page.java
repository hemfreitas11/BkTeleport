package me.bkrmt.bkteleport.edit.options.warp;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.input.InputRunnable;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.edit.EditOption;
import me.bkrmt.bkteleport.teleportable.Teleportable;
import me.bkrmt.bkteleport.teleportable.Warp;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Page extends EditOption {
    public Page(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.page", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, me.bkrmt.bkcore.bkgui.page.Page currentPage) {
        return translateName(getPlugin().getLangFile().get("warps-menu.edit-menu.set-page.name"));
    }

    @Override
    public List<String> getLore(PagedList list, me.bkrmt.bkcore.bkgui.page.Page currentPage) {
        Teleportable teleportable = getTeleportable();
        Warp warp = null;
        if (teleportable != null) warp = (Warp) teleportable;
        List<String> lore = new ArrayList<>();
        if (warp != null) {
            int page = warp.getConfig().get("page") == null ? -1 : warp.getConfig().getInt("page");
            for (String line : getPlugin().getLangFile().getStringList("warps-menu.edit-menu.set-page.lore")) {
                lore.add(
                        line.replace(
                            "{page}",
                            (page < 0 ? getPlugin().getLangFile().get(null, "warps-menu.edit-menu.set-page.no-page") : String.valueOf(page))
                        )
                );
            }
            return translateLore(lore);
        } else return translateLore(getPlugin().getLangFile().getStringList("warps-menu.edit-menu.set-page.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, me.bkrmt.bkcore.bkgui.page.Page currentPage) {
        return translateItem(XMaterial.CYAN_DYE.parseItem());
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, me.bkrmt.bkcore.bkgui.page.Page currentPage) {
        if (!hasPermission()) return event -> {};

        Teleportable teleportable = getTeleportable();
        return event -> {
            if (teleportable == null) {
                currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.option-error"), null);
            } else {
                Warp warp = (Warp) teleportable;
                Player player = (Player) event.getWhoClicked();
                String cancelInput = getPlugin().getConfigManager().getConfig().getString("cancel-input");
                InputRunnable cancelRunnable = input -> {
                    currentPage.openGui(player);
                    currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.input.canceled"), null);
                };
                currentPage.setSwitchingPages(true);
                new PlayerInput(getPlugin(), player, currentPage, input -> {
                    if (!input.equalsIgnoreCase(cancelInput)) {
                        try {
                            warp.setPage(Integer.parseInt(input));
                            warp.saveValues();
                            currentPage.openGui(player);
                            list.updateItem(getID(), this);
                            currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.page-set"), null);
                        } catch (Exception ignored) {
                            currentPage.openGui(player);
                            currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.input.no-number"), null);
                        }
                    }
                }, cancelRunnable)
                        .setTimeout(60, cancelRunnable)
                        .setCancellable(true)
                        .setTitle(getPlugin().getLangFile().get(player, "info.input.number"))
                        .setSubTitle(getPlugin().getLangFile().get(player, "info.input.cancel").replace("{cancel-input}", cancelInput))
                        .sendInput();
            }
        };
    }
}
