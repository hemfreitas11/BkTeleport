package me.bkrmt.bkteleport.edit.options;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.Utils;
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

import java.util.ArrayList;
import java.util.List;

public class Lore extends EditOption {
    public Lore(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.lore", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return translateName(getPlugin().getLangFile().get("homes-menu.edit-menu.set-lore.name"));
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        return translateLore(getPlugin().getLangFile().getStringList("homes-menu.edit-menu.set-lore.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return translateItem(XMaterial.BOOKSHELF.parseItem());
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
                        List<String> lore = new ArrayList<>();

                        if (input.contains("#")) {
                            String[] parts = input.split("#");
                            for (String part : parts) {
                                if (part != null) {
                                    lore.add(Utils.translateColor("§7" + part));
                                }
                            }
                        } else {
                            lore.add(Utils.translateColor("§7" + input));
                        }
                        finalTeleportable.setLore(lore);
                        finalTeleportable.saveValues();
                        currentPage.openGui(player);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.lore-set"), null);
                    }
                }, cancelRunnable)
                        .setTimeout(60, cancelRunnable)
                        .setCancellable(true)
                        .setTitle(getPlugin().getLangFile().get(player, "info.input.new-lore.title"))
                        .setSubTitle(getPlugin().getLangFile().get(player, "info.input.new-lore.subtitle").replace("{cancel-input}", cancelInput))
                        .sendInput();
            }
        };
    }
}
