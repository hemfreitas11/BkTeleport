package me.bkrmt.bkteleport.edit.options;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.input.InputRunnable;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.edit.EditOption;
import me.bkrmt.bkteleport.teleportable.Teleportable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DisplayItem extends EditOption {
    public DisplayItem(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.item", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return translateName(getPlugin().getLangFile().get("homes-menu.edit-menu.set-item.name"));
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        Teleportable teleportable = (Teleportable) getCustomObject();
        List<String> lore = new ArrayList<>();
        if (teleportable != null) {
            for (String line : getPlugin().getLangFile().getStringList("homes-menu.edit-menu.set-item.lore")) {
                lore.add(
                        line.replace("{item}", teleportable.getDisplayItem().getType().toString())
                );
            }
            return translateLore(lore);
        } else return translateLore(getPlugin().getLangFile().getStringList("homes-menu.edit-menu.set-item.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return translateItem(XMaterial.ITEM_FRAME.parseItem());
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        if (!hasPermission()) return event -> {};

        Teleportable finalTeleportable = getTeleportable();
        return event -> {
            if (finalTeleportable == null) {
                currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get(null, "error.option-error"), null);
            } else {
                Player player = (Player) event.getWhoClicked();
                String cancelInput = getPlugin().getConfigManager().getConfig().getString("cancel-input");
                InputRunnable cancelRunnable = input -> {
                    currentPage.openGui(player);
                    currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get(null, "error.input.canceled"), null);
                };
                currentPage.setSwitchingPages(true);
                new PlayerInput(getPlugin(), player, currentPage, input -> {
                    try {
                        finalTeleportable.setDisplayItem(XMaterial.valueOf(input.toUpperCase().replace(" ", "_").replace("-", "_")).parseItem());
                        finalTeleportable.saveValues();
                        currentPage.openGui(player);
                        list.updateItem(getID(), this);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, getPlugin().getLangFile().get(null, "info.item-set"), null);
                    } catch (Exception ignored) {
                        currentPage.openGui(player);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get(null, "error.input.invalid-item"), null);
                    }
                }, cancelRunnable)
                        .setTimeout(60, cancelRunnable)
                        .setCancellable(true)
                        .setTitle(getPlugin().getLangFile().get(player, "info.input.new-item"))
                        .setSubTitle(getPlugin().getLangFile().get(player, "info.input.cancel").replace("{cancel-input}", cancelInput))
                        .sendInput();
            }
        };
    }
}
