package me.bkrmt.bkteleport.edit.options.warp;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.gui.GUI;
import me.bkrmt.bkcore.bkgui.gui.Rows;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.edit.EditOption;
import me.bkrmt.bkteleport.teleportable.Teleportable;
import me.bkrmt.bkteleport.teleportable.Warp;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Slot extends EditOption {
    public Slot(Object customObject, Player player) {
        super(customObject, player, new String[]{"bkteleport.edit.slot", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return translateName(getPlugin().getLangFile().get("warps-menu.edit-menu.set-slot.name"));
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        Warp teleportable = (Warp) ((Warp) getCustomObject());
        List<String> lore = new ArrayList<>();
        if (teleportable != null) {
            int slot = teleportable.getConfig().get("slot") == null ? -1 : teleportable.getConfig().getInt("slot");
            for (String line : getPlugin().getLangFile().getStringList("warps-menu.edit-menu.set-slot.lore")) {
                lore.add(
                        line.replace(
                                "{slot}",
                                (slot < 0 ? getPlugin().getLangFile().get(null, "warps-menu.edit-menu.set-slot.no-slot") : String.valueOf(slot))
                        )
                );
            }
            return translateLore(lore);
        } else return translateLore(getPlugin().getLangFile().getStringList("warps-menu.edit-menu.set-slot.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return translateItem(XMaterial.RED_DYE.parseItem());
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        if (!hasPermission()) return event -> {};
        return event -> {
            Teleportable teleportable = getTeleportable();
            if (teleportable == null) {
                currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get(null, "error.option-error"), null);
            } else {
                Warp warp = (Warp) teleportable;
                Player player = (Player) event.getWhoClicked();

                Page page = new Page(getPlugin(), getPlugin().getAnimatorManager(), new GUI("Click a slot to select it", Rows.SIX), 1);
                page.getGuiSettings().setEmptyClickResponse(event1 -> {
                    warp.setSlot(event1.getSlot());
                    warp.saveValues();
                    page.setWipeOnlySelf(true);
                    currentPage.openGui(player);
                    list.updateItem(getID(), this);
                    currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, getPlugin().getLangFile().get(null, "info.slot-set"), null);
                });
                currentPage.setSwitchingPages(true);
                page.addPreviousMenu(currentPage);
                page.openGui(player);

                /*new PlayerInput(getPlugin(), player, currentPage, input -> {
                    if (!input.equalsIgnoreCase(cancelInput)) {
                        try {
                            warp.setSlot(Integer.parseInt(input));
                            warp.saveValues();
                            currentPage.openGui(player);
                            currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, getPlugin().getLangFile().get(null, "info.slot-set"), null);
                        } catch (Exception ignored) {
                            currentPage.openGui(player);
                            currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get(null, "error.input.no-number"), null);
                        }
                    }
                }, cancelRunnable)
                        .setTimeout(60, cancelRunnable)
                        .setCancellable(true)
                        .setTitle(getPlugin().getLangFile().get(player, "info.input.number"))
                        .setSubTitle(getPlugin().getLangFile().get(player, "info.input.cancel").replace("{cancel-input}", cancelInput))
                        .sendInput();*/
            }
        };
    }
}
