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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DisplayName extends EditOption {
    public DisplayName(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.name", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return translateName(getPlugin().getLangFile().get("homes-menu.edit-menu.set-name.name"));
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        return translateLore(getPlugin().getLangFile().getStringList("homes-menu.edit-menu.set-name.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return translateItem(XMaterial.OAK_SIGN.parseItem());
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        if (!hasPermission()) return event -> {};

        Teleportable finalTeleportable = (Teleportable) getCustomObject();
        return event -> {
            if (finalTeleportable == null) {
                currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.option-error"), null);
            } else {
                Player player = (Player) event.getWhoClicked();
                String cancelInput = getPlugin().getConfigManager().getConfig().getString("cancel-input");
                InputRunnable cancelRunnable = input -> {
                    currentPage.openGui(player);
                    currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.input.canceled"), null);
                };
                currentPage.setSwitchingPages(true);
                new PlayerInput(getPlugin(), player, currentPage, input -> {
                    if (!input.equalsIgnoreCase(cancelInput)) {
                        finalTeleportable.setDisplayName(input);
                        finalTeleportable.saveValues();
                        currentPage.openGui(player);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.name-set"), null);
                    }
                }, cancelRunnable)
                        .setTimeout(60, cancelRunnable)
                        .setCancellable(true)
                        .setTitle(getPlugin().getLangFile().get(player, "info.input.new-name"))
                        .setSubTitle(getPlugin().getLangFile().get(player, "info.input.cancel").replace("{cancel-input}", cancelInput))
                        .sendInput();
            }
        };
    }
}
