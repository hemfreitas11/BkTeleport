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

public class ClickCommand extends EditOption {
    public ClickCommand(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.command", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, me.bkrmt.bkcore.bkgui.page.Page currentPage) {
        return translateName(getPlugin().getLangFile().get("warps-menu.edit-menu.set-command.name"));
    }

    @Override
    public List<String> getLore(PagedList list, me.bkrmt.bkcore.bkgui.page.Page currentPage) {
        Teleportable teleportable = getTeleportable();
        Warp warp = null;
        if (teleportable != null) warp = (Warp) teleportable;
        List<String> lore = new ArrayList<>();
        if (warp != null) {
            String command = warp.getClickCommand();
            for (String line : getPlugin().getLangFile().getStringList("warps-menu.edit-menu.set-command.lore")) {
                lore.add(
                        line
                            .replace("/", (command == null || command.isEmpty() ? "" : "/"))
                            .replace("{command}", (command == null || command.isEmpty() ? "N/A" : command))
                );
            }
            return translateLore(lore);
        } else return translateLore(getPlugin().getLangFile().getStringList("warps-menu.edit-menu.set-command.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, me.bkrmt.bkcore.bkgui.page.Page currentPage) {
        return translateItem(XMaterial.COMMAND_BLOCK.parseItem());
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
                    list.updateItem(getID(), this);
                    currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.input.canceled"), null);
                };
                currentPage.setSwitchingPages(true);
                new PlayerInput(getPlugin(), player, currentPage, input -> {
                    if (!input.equalsIgnoreCase(cancelInput)) {
                        if (input.charAt(0) == '/') input = input.replaceFirst("/", "");
                        warp.setClickCommand(input);
                        warp.saveValues();
                        currentPage.openGui(player);
                        currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.GREEN, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.command-set"), null);
                    }
                }, cancelRunnable)
                        .setTimeout(60, cancelRunnable)
                        .setCancellable(true)
                        .setTitle(getPlugin().getLangFile().get(player, "info.input.command.title"))
                        .setSubTitle(getPlugin().getLangFile().get(player, "info.input.command.subtitle"))
                        .sendInput();
            }
        };
    }
}
